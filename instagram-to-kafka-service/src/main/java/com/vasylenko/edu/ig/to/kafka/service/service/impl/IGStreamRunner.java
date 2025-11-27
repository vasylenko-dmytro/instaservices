package com.vasylenko.edu.ig.to.kafka.service.service.impl;

import com.vasylenko.edu.avro.model.IGAvroModel;
import com.vasylenko.edu.config.IGToKafkaServiceConfigData;
import com.vasylenko.edu.config.KafkaConfigData;
import com.vasylenko.edu.ig.to.kafka.service.listener.HashtagListener;
import com.vasylenko.edu.ig.to.kafka.service.service.StreamRunner;
import com.vasylenko.edu.ig.to.kafka.service.transformer.IGPostToAvroTransformer;
import com.vasylenko.edu.kafka.producer.config.service.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IGStreamRunner implements StreamRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(IGStreamRunner.class);

    private final IGToKafkaServiceConfigData igToKafkaServiceConfigData;
    private final KafkaConfigData kafkaConfigData;
    private final IGPostToAvroTransformer igPostToAvroTransformer;
    private final KafkaProducer<Long, IGAvroModel> kafkaProducer;


    public IGStreamRunner(IGToKafkaServiceConfigData igToKafkaServiceConfigData, KafkaConfigData kafkaConfigData,
                          IGPostToAvroTransformer igPostToAvroTransformer,
                          KafkaProducer<Long, IGAvroModel> kafkaProducer) {
        this.igToKafkaServiceConfigData = igToKafkaServiceConfigData;
        this.kafkaConfigData = kafkaConfigData;
        this.igPostToAvroTransformer = igPostToAvroTransformer;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void start() {
        HashtagListener listener = new HashtagListener(igToKafkaServiceConfigData);
        listener.addListener(post -> {
            LOGGER.info("New post detected: {}", post);

            IGAvroModel avroModel = igPostToAvroTransformer.getAvroModelFromIGPost(post);
            kafkaProducer.send(kafkaConfigData.getTopicName(), Long.valueOf(avroModel.getId()), avroModel);

            LOGGER.info("Posted Avro model to topic {}: {}", kafkaConfigData.getTopicName(), avroModel);
        });
        listener.start();
    }
}
