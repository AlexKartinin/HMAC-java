package ru.yandex.practicum.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.dto.ErrorResponse;
import ru.yandex.practicum.exception.ConfigException;
import ru.yandex.practicum.exception.InvalidMsgException;
import ru.yandex.practicum.exception.InvalidSignatureFormatException;
import ru.yandex.practicum.exception.PayloadTooLargeException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidMsgException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleInvalidMsg(InvalidMsgException e) {
        log.warn("invalid request: invalid_msg");
        return new ErrorResponse("invalid_msg");
    }

    @ExceptionHandler(InvalidSignatureFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleInvalidSignature(InvalidSignatureFormatException e) {
        log.warn("invalid request: invalid_signature_format");
        return new ErrorResponse("invalid_signature_format");
    }

    @ExceptionHandler(PayloadTooLargeException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ResponseBody
    public ErrorResponse handlePayloadTooLarge(PayloadTooLargeException e) {
        log.warn("invalid request: payload_too_large");
        return new ErrorResponse("payload_too_large");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ResponseBody
    public ErrorResponse handleUnsupportedMediaType(HttpMediaTypeNotSupportedException e) {
        log.warn("invalid request: unsupported_media_type");
        return new ErrorResponse("unsupported_media_type");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleInvalidJson(HttpMessageNotReadableException e) {
        log.warn("invalid request: invalid_json");
        return new ErrorResponse("invalid_json");
    }

    @ExceptionHandler(ConfigException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleConfigException(ConfigException e) {
        log.error("Config error: {}", e.getMessage());
        return new ErrorResponse("config_error");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleGeneral(Exception e) {
        log.error("Internal error", e);
        return new ErrorResponse("internal");
    }
}
