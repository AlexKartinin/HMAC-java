package ru.yandex.practicum.service;

public interface HmacService {
    String sign(String msg);
    boolean verify(String msg, String signature);
}
