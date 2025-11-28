package com.vasylenko.edu.ig.to.kafka.service;

import com.vasylenko.edu.ig.to.kafka.service.init.StreamInitializer;
import com.vasylenko.edu.ig.to.kafka.service.util.StreamRunner;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.stream.Stream;

@SpringBootApplication
@ComponentScan(basePackages = "com.vasylenko.edu")
public class InstagramToKafkaServiceApplication implements CommandLineRunner {

    public static final Logger LOGGER = LoggerFactory.getLogger(InstagramToKafkaServiceApplication.class);

    private final StreamInitializer streamInitializer;
    private final StreamRunner streamRunner;
    public InstagramToKafkaServiceApplication(StreamInitializer streamInitializer1,
                                              StreamRunner streamRunner) {
        this.streamInitializer = streamInitializer1;
        this.streamRunner = streamRunner;
    }
    public static void main(String[] args) {
        loadEnvProperties();
        SpringApplication.run(InstagramToKafkaServiceApplication.class, args);
    }

    private static void loadEnvProperties() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String token = dotenv.get("ACCESS_TOKEN");
        String userId = dotenv.get("IG_USER_ID");
        if (Stream.of(token, userId).anyMatch(s -> s != null && !s.isBlank())) {
            System.setProperty("ACCESS_TOKEN", token);
            System.setProperty("IG_USER_ID", userId);
            LOGGER.info(".env variables loaded successfully.");
        } else {
            LOGGER.warn("Environments properties missing or blank.");
        }
    }

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("App starts...");
        streamInitializer.init();
        streamRunner.start();
    }
}