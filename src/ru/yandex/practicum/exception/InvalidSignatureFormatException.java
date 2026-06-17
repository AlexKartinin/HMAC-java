package ru.yandex.practicum.exception;

public class InvalidSignatureFormatException extends RuntimeException {
    public InvalidSignatureFormatException(String message) {
        super(message);
    }
}
