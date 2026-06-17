# HMAC-SHA256 Signing Service

REST API сервис для подписи и проверки целостности сообщений по алгоритму HMAC-SHA256.

## Требования

- Java 17+
- Maven 3.6+

## Генерация секрета

```bash
openssl rand -base64 32
```

Скопируйте вывод в поле `secret` файла `config.json`.

## Формат config.json

```json
{
  "hmacAlg": "SHA256",
  "secret": "<base64-encoded-secret>",
  "listenPort": 8080,
  "maxMsgSizeBytes": 1048576
}
```

| Поле | Описание |
|------|----------|
| `hmacAlg` | Алгоритм (всегда `SHA256`) |
| `secret` | Секретный ключ в формате base64 (минимум 32 байта) |
| `listenPort` | Порт сервера (по умолчанию 8080) |
| `maxMsgSizeBytes` | Максимальный размер сообщения в байтах (по умолчанию 1 МБ) |

> **Безопасность:** ограничьте доступ к файлу: `chmod 600 config.json`

## Запуск

```bash
# Сборка и запуск через Maven
mvn spring-boot:run

# Сборка JAR и запуск
mvn package
java -jar target/hmac-service-0.0.1-SNAPSHOT.jar
```

## Запуск тестов

```bash
mvn test
```

## Примеры curl

**Подписать сообщение:**

```bash
curl -sS -X POST http://localhost:8080/sign \
  -H 'Content-Type: application/json' \
  -d '{"msg":"hello"}'
```

Ответ:

```json
{"signature":"<base64url>"}
```

**Проверить подпись:**

```bash
curl -sS -X POST http://localhost:8080/verify \
  -H 'Content-Type: application/json' \
  -d '{"msg":"hello","signature":"<из /sign>"}'
```

Ответ:

```json
{"ok":true}
```

## Ротация секрета

```bash
mvn compile
mvn exec:java -Dexec.mainClass="ru.yandex.practicum.cli.RotateSecretCli"
```

Утилита генерирует новый 32-байтовый секрет и обновляет `config.json`. После ротации необходим перезапуск сервера.

## API

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/sign` | Подписать сообщение |
| POST | `/verify` | Проверить подпись |

### Коды ошибок

| HTTP | `error` | Причина |
|------|---------|---------|
| 400 | `invalid_msg` | Поле `msg` отсутствует или пустое |
| 400 | `invalid_signature_format` | Поле `signature` не является корректным base64url |
| 400 | `invalid_json` | Тело запроса не является корректным JSON |
| 413 | `payload_too_large` | Размер `msg` превышает `maxMsgSizeBytes` |
| 415 | `unsupported_media_type` | Отсутствует заголовок `Content-Type: application/json` |
| 500 | `config_error` | Ошибка конфигурации |
| 500 | `internal` | Непредвиденная ошибка |

## Ограничения реализации

- **HMAC ≠ асимметричная электронная подпись.** Используется симметричный MAC: обе стороны должны знать один общий секрет.
- Нет сертификатов, меток времени и цепочек доверия — невозможно доказать безотказность.
- Нет шифрования: содержимое сообщения передаётся открыто.
- Один секрет для всех операций.
- Безопасность полностью зависит от секретности общего ключа и его своевременной ротации.
