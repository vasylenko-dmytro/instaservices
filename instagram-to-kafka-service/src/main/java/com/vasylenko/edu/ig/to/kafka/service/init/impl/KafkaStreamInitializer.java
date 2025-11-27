package com.vasylenko.edu.ig.to.kafka.service.init.impl;

import com.vasylenko.edu.config.KafkaConfigData;
import com.vasylenko.edu.ig.to.kafka.service.init.StreamInitializer;
import com.vasylenko.edu.kafka.admin.client.KafkaAdminClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KafkaStreamInitializer implements StreamInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamInitializer.class);

    private final KafkaConfigData kafkaConfigData;
    private final KafkaAdminClient kafkaAdminClient;

    public KafkaStreamInitializer(KafkaConfigData kafkaConfigData, KafkaAdminClient kafkaAdminClient) {
        this.kafkaConfigData = kafkaConfigData;
        this.kafkaAdminClient = kafkaAdminClient;
    }

    @Override
    public void init() {
        kafkaAdminClient.createTopics();
        kafkaAdminClient.checkSchemaRegistry();
        LOGGER.info("Topics with name {} are ready for operations!", kafkaConfigData.getTopicNamesToCreate().toArray());
    }
}
