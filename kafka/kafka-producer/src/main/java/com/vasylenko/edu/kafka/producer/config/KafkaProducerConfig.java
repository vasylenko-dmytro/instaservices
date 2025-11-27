package com.vasylenko.edu.kafka.producer.config;

import com.vasylenko.edu.config.KafkaConfigData;
import com.vasylenko.edu.config.KafkaProducerConfigData;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;

@Configuration
public class KafkaProducerConfig<K extends Serializable, V extends SpecificRecordBase> {

    private final KafkaConfigData kafkaConfigData;
    private final KafkaProducerConfigData kafkaProducerConfigData;

    public KafkaProducerConfig(KafkaConfigData kafkaConfigData, KafkaProducerConfigData kafkaProducerConfigData) {
        this.kafkaConfigData = kafkaConfigData;
        this.kafkaProducerConfigData = kafkaProducerConfigData;
    }


}
