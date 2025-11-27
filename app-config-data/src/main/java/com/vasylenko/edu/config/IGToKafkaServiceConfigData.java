package com.vasylenko.edu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "instagram-to-kafka-service")
public class IGToKafkaServiceConfigData {
    private List<String> instagramKeywords;
    private String welcomeMessage;
    private String igAccessToken;
    private String igUserId;
    private String pollIntervalSeconds;
}
