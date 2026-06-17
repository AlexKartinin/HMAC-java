package ru.yandex.practicum.dto;

public class SignResponse {
    private final String signature;

    public SignResponse(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }
}
