package ru.yandex.practicum.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.codec.Base64UrlCodec;
import ru.yandex.practicum.config.AppConfig;
import ru.yandex.practicum.dto.SignRequest;
import ru.yandex.practicum.dto.SignResponse;
import ru.yandex.practicum.dto.VerifyRequest;
import ru.yandex.practicum.dto.VerifyResponse;
import ru.yandex.practicum.exception.InvalidMsgException;
import ru.yandex.practicum.exception.InvalidSignatureFormatException;
import ru.yandex.practicum.exception.PayloadTooLargeException;
import ru.yandex.practicum.service.HmacService;

import java.nio.charset.StandardCharsets;

@RestController
public class HmacController {
    private static final Logger log = LoggerFactory.getLogger(HmacController.class);

    private final HmacService hmacService;
    private final AppConfig appConfig;

    public HmacController(HmacService hmacService, AppConfig appConfig) {
        this.hmacService = hmacService;
        this.appConfig = appConfig;
    }

    @PostMapping(value = "/sign", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SignResponse sign(@RequestBody SignRequest request) {
        validateMsg(request.getMsg());
        String signature = hmacService.sign(request.getMsg());
        log.info("sign OK, msgLen={}", request.getMsg().getBytes(StandardCharsets.UTF_8).length);
        return new SignResponse(signature);
    }

    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public VerifyResponse verify(@RequestBody VerifyRequest request) {
        validateMsg(request.getMsg());
        String signature = request.getSignature();
        if (signature == null || signature.isBlank()) {
            throw new InvalidSignatureFormatException("signature is missing or empty");
        }
        if (signature.getBytes(StandardCharsets.UTF_8).length > appConfig.getMaxMsgSizeBytes()) {
            throw new PayloadTooLargeException("signature size exceeds maxMsgSizeBytes");
        }
        if (!Base64UrlCodec.isValidBase64Url(signature)) {
            throw new InvalidSignatureFormatException("signature is not valid base64url");
        }
        boolean ok = hmacService.verify(request.getMsg(), signature);
        log.info("verify OK={}, msgLen={}", ok, request.getMsg().getBytes(StandardCharsets.UTF_8).length);
        return new VerifyResponse(ok);
    }

    private void validateMsg(String msg) {
        if (msg == null || msg.isEmpty()) {
            throw new InvalidMsgException("msg is missing or empty");
        }
        long msgByteLen = msg.getBytes(StandardCharsets.UTF_8).length;
        if (msgByteLen > appConfig.getMaxMsgSizeBytes()) {
            throw new PayloadTooLargeException("msg size " + msgByteLen + " exceeds maxMsgSizeBytes");
        }
    }
}
