package com.vasylenko.edu.ig.to.kafka.service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasylenko.edu.config.IGToKafkaServiceConfigData;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads hashtags from application configuration, resolves their Instagram Graph API IDs
 * (using the ig_hashtag_search endpoint) and writes a properties-style file with lines
 * HASHTAG=ID to the application's working directory (file name: hashtag-ids.properties).
 *
 * This component runs once on application startup.
 */
@Component
public class HashtagIdsUpdater implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(HashtagIdsUpdater.class);

    private final IGToKafkaServiceConfigData configData;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public HashtagIdsUpdater(IGToKafkaServiceConfigData configData) {
        this.configData = configData;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // TODO: Design a separately runner
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // updateHashtagIds();
    }

    public void updateHashtagIds() {
        List<String> hashtags = configData.getInstagramKeywords();
        if (hashtags == null || hashtags.isEmpty()) {
            LOGGER.warn("No instagram keywords configured - nothing to update");
            return;
        }

        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String accessToken = dotenv.get("ACCESS_TOKEN");
        String igUserId = dotenv.get("IG_USER_ID");

        if (accessToken == null || accessToken.isEmpty() || igUserId == null || igUserId.isEmpty()) {
            LOGGER.warn("ACCESS_TOKEN or IG_USER_ID not provided in environment - cannot query Instagram API");
            return;
        }

        List<String> lines = new ArrayList<>();

        for (String hashtag : hashtags) {
            try {
                String url = buildHashtagSearchUrl(igUserId, hashtag, accessToken);
                LOGGER.info("Fetching hashtag id for '{}' from URL: {}", hashtag, url);
                String resp = restTemplate.getForObject(url, String.class);
                if (resp == null) {
                    LOGGER.warn("Empty response for hashtag '{}', skipping", hashtag);
                    continue;
                }

                JsonNode root = objectMapper.readTree(resp);
                // Check for data array
                if (root.has("data") && root.get("data").isArray() && !root.get("data").isEmpty()) {
                    JsonNode first = root.get("data").get(0);
                    if (first.has("id")) {
                        String id = first.get("id").asText();
                        lines.add(hashtag + "=" + id);
                        LOGGER.info("Resolved hashtag '{}' -> id={}", hashtag, id);
                    } else {
                        LOGGER.warn("No 'id' field in first data element for hashtag '{}', response: {}", hashtag, resp);
                    }
                } else if (root.has("error")) {
                    LOGGER.warn("API returned error for hashtag '{}': {}", hashtag, root.get("error").toString());
                } else {
                    LOGGER.warn("No data found for hashtag '{}', response: {}", hashtag, resp);
                }

            } catch (RestClientException | IOException e) {
                LOGGER.error("Failed to fetch id for hashtag '{}' - {}", hashtag, e.getMessage());
            }
        }

        // Write results to file in working directory
        Path out = Path.of("instagram-to-kafka-service/src/main/resources/hashtag-ids.properties");
        try (BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            LOGGER.info("Wrote {} hashtag id mappings to {}", lines.size(), out.toAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Failed to write hashtag ids file {}: {}", out.toAbsolutePath(), e.getMessage());
        }
    }

    private String buildHashtagSearchUrl(String igUserId, String hashtag, String accessToken) {
        return UriComponentsBuilder
                .fromUriString("https://graph.facebook.com/v24.0/ig_hashtag_search")
                .queryParam("user_id", igUserId)
                .queryParam("q", hashtag)
                .queryParam("access_token", accessToken)
                .toUriString();
    }
}

