package ru.yandex.practicum.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.exception.ConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Component
public class AppConfig implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    private static final String FIELD_SECRET = "secret";
    private static final String FIELD_HMAC_ALG = "hmacAlg";
    private static final String FIELD_LISTEN_PORT = "listenPort";
    private static final String FIELD_MAX_MSG_SIZE = "maxMsgSizeBytes";
    private static final long DEFAULT_MAX_MSG_SIZE_BYTES = 1_048_576L;
    private static final int DEFAULT_LISTEN_PORT = 8080;
    private static final String DEFAULT_HMAC_ALG = "SHA256";

    @Value("${app.config.path:config.json}")
    private String configPath;

    private byte[] secretKey;
    private long maxMsgSizeBytes = DEFAULT_MAX_MSG_SIZE_BYTES;
    private int listenPort = DEFAULT_LISTEN_PORT;
    private String hmacAlgorithm = "Hmac" + DEFAULT_HMAC_ALG;

    @PostConstruct
    public void load() {
        Path path = Path.of(configPath);
        if (!Files.exists(path)) {
            throw new ConfigException("config.json not found at: " + configPath);
        }
        try (InputStream in = Files.newInputStream(path)) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(in);

            JsonNode secretNode = root.get(FIELD_SECRET);
            if (secretNode == null || secretNode.isNull() || secretNode.asText().isBlank()) {
                throw new ConfigException("'secret' field is missing or empty in config.json");
            }
            try {
                secretKey = Base64.getDecoder().decode(secretNode.asText());
            } catch (IllegalArgumentException e) {
                throw new ConfigException("'secret' in config.json is not valid base64");
            }
            if (secretKey.length == 0) {
                throw new ConfigException("'secret' decoded to empty byte array");
            }

            JsonNode hmacAlgNode = root.get(FIELD_HMAC_ALG);
            if (hmacAlgNode != null && !hmacAlgNode.isNull() && !hmacAlgNode.asText().isBlank()) {
                hmacAlgorithm = "Hmac" + hmacAlgNode.asText();
            }

            JsonNode portNode = root.get(FIELD_LISTEN_PORT);
            if (portNode != null && !portNode.isNull()) {
                int port = portNode.asInt(DEFAULT_LISTEN_PORT);
                if (port > 0 && port <= 65535) {
                    listenPort = port;
                }
            }

            JsonNode maxMsgNode = root.get(FIELD_MAX_MSG_SIZE);
            if (maxMsgNode != null && !maxMsgNode.isNull()) {
                maxMsgSizeBytes = maxMsgNode.asLong(DEFAULT_MAX_MSG_SIZE_BYTES);
            }

            log.info("Config loaded: hmacAlg={}, listenPort={}, maxMsgSizeBytes={}",
                    hmacAlgorithm, listenPort, maxMsgSizeBytes);
        } catch (ConfigException e) {
            throw e;
        } catch (IOException e) {
            throw new ConfigException("Failed to read config.json: " + e.getMessage());
        }
    }

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        factory.setPort(listenPort);
    }

    public byte[] getSecretKey() {
        return secretKey;
    }

    public String getHmacAlgorithm() {
        return hmacAlgorithm;
    }

    public int getListenPort() {
        return listenPort;
    }

    public long getMaxMsgSizeBytes() {
        return maxMsgSizeBytes;
    }
}
