package com.vasylenko.edu.kafka.producer.config.service.impl;

import com.vasylenko.edu.avro.model.IGAvroModel;
import com.vasylenko.edu.kafka.producer.config.service.KafkaProducer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class IGKafkaProducer implements KafkaProducer<Long, IGAvroModel>, DisposableBean {

    public static final Logger LOGGER = LoggerFactory.getLogger(IGKafkaProducer.class);

    private KafkaTemplate<Long, IGAvroModel> kafkaTemplate;

    public IGKafkaProducer(KafkaTemplate<Long, IGAvroModel> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(String topicName, Long key, IGAvroModel message) {
        LOGGER.info("Sending message '{}' to topic '{}'", message, topicName);
        CompletableFuture<SendResult<Long, IGAvroModel>> kafkaResultFuture =
                kafkaTemplate.send(topicName, key, message);
        addCallBack(topicName, message, kafkaResultFuture);
    }

    @Override
    public void destroy() {
        if (kafkaTemplate != null) {
            LOGGER.info("Closing kafka producer!");
            kafkaTemplate.destroy();
        }
    }

    private void addCallBack(String topicName, IGAvroModel message, CompletableFuture<SendResult<Long, IGAvroModel>> kafkaResultFuture) {
        kafkaResultFuture.whenComplete((result, ex) -> {
            if (ex == null) {
                RecordMetadata metadata = result.getRecordMetadata();
                LOGGER.info("Received new metadata. Topic: {}. Partition: {}. Offset: {}. Timestamp: {}, at time {}",
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                        metadata.timestamp(),
                        System.nanoTime());
            } else {
                LOGGER.error("Error sending message {} to topic {}", message.toString(), topicName, ex);
            }
        });
    }
}
