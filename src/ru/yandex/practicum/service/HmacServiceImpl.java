package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.codec.Base64UrlCodec;
import ru.yandex.practicum.config.AppConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class HmacServiceImpl implements HmacService {
    private final AppConfig appConfig;

    public HmacServiceImpl(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Override
    public String sign(String msg) {
        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
        byte[] sigBytes = computeHmac(msgBytes);
        return Base64UrlCodec.encode(sigBytes);
    }

    @Override
    public boolean verify(String msg, String signature) {
        byte[] expectedBytes = computeHmac(msg.getBytes(StandardCharsets.UTF_8));
        byte[] actualBytes = Base64UrlCodec.decode(signature);
        return MessageDigest.isEqual(expectedBytes, actualBytes);
    }

    private byte[] computeHmac(byte[] msgBytes) {
        String algorithm = appConfig.getHmacAlgorithm();
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(appConfig.getSecretKey(), algorithm));
            return mac.doFinal(msgBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }
}
