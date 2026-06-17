package ru.yandex.practicum.codec;

import ru.yandex.practicum.exception.InvalidSignatureFormatException;

import java.util.Base64;
import java.util.regex.Pattern;

public class Base64UrlCodec {
    private static final Pattern BASE64URL_PATTERN = Pattern.compile("^[A-Za-z0-9\\-_]+$");

    private Base64UrlCodec() {}

    public static String encode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    public static byte[] decode(String base64url) {
        if (!isValidBase64Url(base64url)) {
            throw new InvalidSignatureFormatException("Invalid base64url format");
        }
        try {
            return Base64.getUrlDecoder().decode(base64url);
        } catch (IllegalArgumentException e) {
            throw new InvalidSignatureFormatException("Failed to decode base64url");
        }
    }

    public static boolean isValidBase64Url(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        return BASE64URL_PATTERN.matcher(s).matches();
    }
}
