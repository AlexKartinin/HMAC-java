package ru.yandex.practicum.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;

public class RotateSecretCli {
    private static final int SECRET_BYTE_LENGTH = 32;
    private static final String DEFAULT_CONFIG_PATH = "config.json";
    private static final String FIELD_SECRET = "secret";

    public static void main(String[] args) throws IOException {
        String configPath = args.length > 0 ? args[0] : DEFAULT_CONFIG_PATH;
        Path path = Path.of(configPath);

        byte[] newSecret = new byte[SECRET_BYTE_LENGTH];
        new SecureRandom().nextBytes(newSecret);
        String encodedSecret = Base64.getEncoder().encodeToString(newSecret);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try (InputStream in = Files.newInputStream(path)) {
            root = mapper.readTree(in);
        }

        ((ObjectNode) root).put(FIELD_SECRET, encodedSecret);

        try (OutputStream out = Files.newOutputStream(path)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(out, root);
        }

        System.out.println("Secret rotated successfully");
    }
}
