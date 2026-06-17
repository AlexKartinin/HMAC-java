package ru.yandex.practicum.dto;

public class VerifyResponse {
    private final boolean ok;

    public VerifyResponse(boolean ok) {
        this.ok = ok;
    }

    public boolean isOk() {
        return ok;
    }
}
