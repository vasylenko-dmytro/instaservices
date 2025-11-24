package com.vasylenko;

import com.facebook.ads.sdk.APIException;
import com.vasylenko.config.InstagramToKafkaServiceConfigData;
import com.vasylenko.listener.HashtagListener;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InstagramToKafkaServiceApplication implements CommandLineRunner {

    public static final Logger LOGGER = LoggerFactory.getLogger(InstagramToKafkaServiceApplication.class);

    private final InstagramToKafkaServiceConfigData instagramToKafkaServiceConfigData;

    public InstagramToKafkaServiceApplication(InstagramToKafkaServiceConfigData instagramToKafkaServiceConfigData) {
        this.instagramToKafkaServiceConfigData = instagramToKafkaServiceConfigData;
    }
    public static void main(String[] args) throws APIException {
        SpringApplication.run(InstagramToKafkaServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String accessToken = dotenv.get("ACCESS_TOKEN");
        String igUserId = dotenv.get("IG_USER_ID");

        LOGGER.info("Instagram keywords: {}", instagramToKafkaServiceConfigData.getInstagramKeywords());
        LOGGER.info("Welcome message: {}", instagramToKafkaServiceConfigData.getWelcomeMessage());

        HashtagListener listener = new HashtagListener(accessToken, igUserId, 60);
        listener.addListener(post -> LOGGER.info("New post detected: {}", post));

        listener.start();
    }
}