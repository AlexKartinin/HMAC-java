package ru.yandex.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.codec.Base64UrlCodec;
import ru.yandex.practicum.config.AppConfig;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HmacServiceTest {
    private static final byte[] TEST_KEY = "this-is-test-secret-key-123456!".getBytes(StandardCharsets.UTF_8);

    @Mock
    private AppConfig appConfig;

    private HmacServiceImpl hmacService;

    @BeforeEach
    void setUp() {
        when(appConfig.getSecretKey()).thenReturn(TEST_KEY);
        when(appConfig.getHmacAlgorithm()).thenReturn("HmacSHA256");
        hmacService = new HmacServiceImpl(appConfig);
    }

    @Test
    void signIsDeterministic() {
        String sig1 = hmacService.sign("hello");
        String sig2 = hmacService.sign("hello");
        assertEquals(sig1, sig2);
    }

    @Test
    void verifySucceedsForMatchingSignature() {
        String signature = hmacService.sign("hello");
        assertTrue(hmacService.verify("hello", signature));
    }

    @Test
    void verifyFailsForTamperedSignature() {
        String signature = hmacService.sign("hello");
        char[] chars = signature.toCharArray();
        chars[0] = (chars[0] == 'A') ? 'B' : 'A';
        assertFalse(hmacService.verify("hello", new String(chars)));
    }

    @Test
    void verifyFailsForDifferentMessage() {
        String signature = hmacService.sign("hello");
        assertFalse(hmacService.verify("hello!", signature));
    }

    @Test
    void signatureIsValidBase64UrlWithoutPadding() {
        String signature = hmacService.sign("test message");
        assertTrue(Base64UrlCodec.isValidBase64Url(signature));
        assertFalse(signature.contains("="));
        assertFalse(signature.contains("+"));
        assertFalse(signature.contains("/"));
    }

    @Test
    void signatureHasFixedLength() {
        String sig1 = hmacService.sign("a");
        String sig2 = hmacService.sign("very long message that is much longer than the first one");
        assertEquals(sig1.length(), sig2.length());
    }
}
