package com.vasylenko.edu.kafka.to.elastic.service.consumer.impl;

import com.vasylenko.edu.avro.model.IGAvroModel;
import com.vasylenko.edu.config.KafkaConfigData;
import com.vasylenko.edu.kafka.admin.client.KafkaAdminClient;
import com.vasylenko.edu.kafka.to.elastic.service.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.List;
import java.util.Objects;

public class IGKafkaConsumer implements KafkaConsumer<Long, IGAvroModel> {
    public static final Logger LOGGER = LoggerFactory.getLogger(IGKafkaConsumer.class);

    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    private final KafkaAdminClient kafkaAdminClient;
    private final KafkaConfigData kafkaConfigData;

    public IGKafkaConsumer(KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry, KafkaAdminClient kafkaAdminClient, KafkaConfigData kafkaConfigData) {
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
        this.kafkaAdminClient = kafkaAdminClient;
        this.kafkaConfigData = kafkaConfigData;
    }

    @KafkaListener
    public void onAppStarted(ApplicationStartedEvent event) {
        kafkaAdminClient.checkTopicsCreated();
        LOGGER.info("Topics with name {} is ready for operations!", kafkaConfigData.getTopicNamesToCreate().toArray());
        Objects.requireNonNull(kafkaListenerEndpointRegistry.getListenerContainer("igTopicListener")).start();
    }

    @Override
    @KafkaListener(id = "igTopicListener", topics = "${kafka-config.topic-name}")
    public void receive(
            @Payload List<IGAvroModel> messages,
            @Header(KafkaHeaders.RECEIVED_KEY) List<Integer> keys,
            @Header(KafkaHeaders.RECEIVED_PARTITION)List<Integer> partitions,
            @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        LOGGER.info("{} number of messages received with keys {}, partitions {} and offsets {}, " +
                "sending it to elastic: Thread id {}.",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString(),
                Thread.currentThread().threadId());


    }
}
