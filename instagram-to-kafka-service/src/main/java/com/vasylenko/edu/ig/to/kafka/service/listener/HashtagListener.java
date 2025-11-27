package com.vasylenko.edu.ig.to.kafka.service.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasylenko.edu.avro.model.IGAvroModel;
import com.vasylenko.edu.config.IGToKafkaServiceConfigData;
import com.vasylenko.edu.config.KafkaConfigData;
import com.vasylenko.edu.ig.to.kafka.service.model.IGPost;
import com.vasylenko.edu.ig.to.kafka.service.transformer.IGPostToAvroTransformer;
import com.vasylenko.edu.kafka.producer.config.service.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Component
public class HashtagListener {

    public static final Logger LOGGER = LoggerFactory.getLogger(HashtagListener.class);

    private final IGToKafkaServiceConfigData igToKafkaServiceConfigData;

    private final Map<String, String> hashtagIds = new HashMap<>();
    private final Set<String> seenPosts = new HashSet<>();
    private final List<IGHashtagListener> listeners = new ArrayList<>();
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public HashtagListener(
            IGToKafkaServiceConfigData igToKafkaServiceConfigData) {
        this.igToKafkaServiceConfigData = igToKafkaServiceConfigData;
        loadHashtagIds("/hashtag-ids.properties");
    }

    public void addListener(IGHashtagListener listener) {
        listeners.add(listener);
    }

    public void start() {
        int pollIntervalSeconds = Integer.parseInt(igToKafkaServiceConfigData.getPollIntervalSeconds());
        new Thread(() -> {
            while (true) {
                try {
                    for (Map.Entry<String, String> entry : hashtagIds.entrySet()) {
                        String hashtag = entry.getKey();
                        String hashtagId = entry.getValue();
                        pollHashtag(hashtag, hashtagId);
                    }
                    Thread.sleep(pollIntervalSeconds * 1000L);
                } catch (InterruptedException ie) {
                    LOGGER.error("Polling interrupted: {}", ie.getMessage());
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    private void pollHashtag(String hashtag, String hashtagId) {
        try {
            String url = buildRecentMediaUrl(hashtagId);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());
            JsonNode data = root.path("data");

            for (JsonNode postNode : data) {
                String id = postNode.get("id").asText();
                if (!seenPosts.contains(id)) {
                    String permalink = postNode.get("permalink").asText();
                    String caption = postNode.has("caption") ? postNode.get("caption").asText() : "";
                    String timestamp = postNode.get("timestamp").asText();
                    IGPost post = new IGPost(id, permalink, caption, timestamp);
                    seenPosts.add(id);

                    // Notify all listeners
                    for (IGHashtagListener listener : listeners) {
                        listener.onNewPost(post);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Error polling hashtag {}: {}", hashtag, e.getMessage());
        }
    }

    private String buildRecentMediaUrl(String hashtagId) {
        return UriComponentsBuilder
                .fromUriString("https://graph.facebook.com/v24.0/{hashtagId}/recent_media")
                .queryParam("user_id", igToKafkaServiceConfigData.getIgUserId())
                .queryParam("fields", "id,permalink,caption,timestamp")
                .queryParam("access_token", igToKafkaServiceConfigData.getIgAccessToken())
                .buildAndExpand(hashtagId)
                .toUriString();
    }

    public void loadHashtagIds(String resourcePath) {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                LOGGER.error("Properties not found: {}", resourcePath);
                return;
            }
            Properties props = new Properties();
            props.load(in);
            for (String name : props.stringPropertyNames()) {
                String value = props.getProperty(name);
                if (value != null) {
                    hashtagIds.put(name.trim(), value.trim());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load hashtag ids: {}", e.getMessage());
        }
    }
}
