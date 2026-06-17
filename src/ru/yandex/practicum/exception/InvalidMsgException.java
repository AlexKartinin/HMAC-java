package ru.yandex.practicum.exception;

public class InvalidMsgException extends RuntimeException {
    public InvalidMsgException(String message) {
        super(message);
    }
}
