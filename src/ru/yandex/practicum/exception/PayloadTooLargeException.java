package ru.yandex.practicum.exception;

public class PayloadTooLargeException extends RuntimeException {
    public PayloadTooLargeException(String message) {
        super(message);
    }
}
