package ru.yandex.practicum.codec;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.exception.InvalidSignatureFormatException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class Base64UrlCodecTest {

    @Test
    void encodeDecodeRoundTrip() {
        byte[] original = "hello world".getBytes(StandardCharsets.UTF_8);
        String encoded = Base64UrlCodec.encode(original);
        byte[] decoded = Base64UrlCodec.decode(encoded);
        assertArrayEquals(original, decoded);
    }

    @Test
    void encodeHasNoPadding() {
        String encoded = Base64UrlCodec.encode("test".getBytes(StandardCharsets.UTF_8));
        assertFalse(encoded.contains("="));
    }

    @Test
    void encodeUsesUrlSafeAlphabet() {
        byte[] data = new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) 0xFD};
        String encoded = Base64UrlCodec.encode(data);
        assertFalse(encoded.contains("+"));
        assertFalse(encoded.contains("/"));
    }

    @Test
    void decodeThrowsOnInvalidCharacters() {
        assertThrows(InvalidSignatureFormatException.class, () -> Base64UrlCodec.decode("@@@"));
    }

    @Test
    void decodeThrowsOnPaddedInput() {
        assertThrows(InvalidSignatureFormatException.class, () -> Base64UrlCodec.decode("abc="));
    }

    @Test
    void decodeThrowsOnStandardBase64WithPlusSlash() {
        assertThrows(InvalidSignatureFormatException.class, () -> Base64UrlCodec.decode("abc+def"));
        assertThrows(InvalidSignatureFormatException.class, () -> Base64UrlCodec.decode("abc/def"));
    }

    @Test
    void isValidBase64UrlAcceptsValidInput() {
        assertTrue(Base64UrlCodec.isValidBase64Url("aGVsbG8"));
        assertTrue(Base64UrlCodec.isValidBase64Url("abc-def_GHI123"));
    }

    @Test
    void isValidBase64UrlRejectsInvalidInput() {
        assertFalse(Base64UrlCodec.isValidBase64Url("@@@"));
        assertFalse(Base64UrlCodec.isValidBase64Url("abc="));
        assertFalse(Base64UrlCodec.isValidBase64Url("abc+def"));
        assertFalse(Base64UrlCodec.isValidBase64Url(""));
        assertFalse(Base64UrlCodec.isValidBase64Url(null));
    }
}
