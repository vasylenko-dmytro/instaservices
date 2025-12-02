package com.vasylenko.edu.config.server;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

import java.util.stream.Stream;

@EnableConfigServer
@SpringBootApplication
public class ConfigServer {
    public static final Logger LOGGER = LoggerFactory.getLogger(ConfigServer.class);

    static void main(String[] args) {
        loadEnvProperties();
        SpringApplication.run(ConfigServer.class, args);
    }

    private static void loadEnvProperties() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String username = dotenv.get("CLOUD_USERNAME");
        String password = dotenv.get("CLOUD_PASSWORD");
        String ghUsername = dotenv.get("GHL_USER");
        String ghPassword = dotenv.get("GHP_TOKEN");
        String encryptKey = dotenv.get("SECRET_KEY");
        if (Stream.of(username, password, ghUsername, ghPassword, encryptKey)
                .anyMatch(s -> s != null && !s.isBlank())) {
            System.setProperty("CLOUD_USERNAME", username);
            System.setProperty("CLOUD_PASSWORD", password);
            System.setProperty("GHL_USER", ghUsername);
            System.setProperty("GHP_TOKEN", ghPassword);
            System.setProperty("SECRET_KEY", encryptKey);
            LOGGER.info(".env variables loaded successfully.");
        } else {
            LOGGER.warn("Environments properties missing or blank.");
        }
    }
}
