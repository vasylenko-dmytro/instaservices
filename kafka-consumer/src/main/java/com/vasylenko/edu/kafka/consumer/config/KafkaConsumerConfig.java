package com.vasylenko.edu.kafka.consumer.config;

import com.vasylenko.edu.config.KafkaConfigData;
import com.vasylenko.edu.config.KafkaConsumerConfigData;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.io.Serializable;
import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.FETCH_MAX_BYTES_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;


@EnableKafka
@Configuration
public class KafkaConsumerConfig<K extends Serializable, V extends SpecificRecordBase> {
    private final KafkaConfigData kafkaConfigData;
    private final KafkaConsumerConfigData kafkaConsumerConfigData;
    public KafkaConsumerConfig(KafkaConfigData kafkaConfigData,
                                KafkaConsumerConfigData kafkaConsumerConfigData) {
        this.kafkaConfigData = kafkaConfigData;
        this.kafkaConsumerConfigData = kafkaConsumerConfigData;
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<K, V>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<K, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(kafkaConsumerConfigData.getBatchListener());
        factory.setConcurrency(kafkaConsumerConfigData.getConcurrencyLevel());
        factory.setAutoStartup(kafkaConsumerConfigData.getAutoStartup());
        factory.getContainerProperties().setPollTimeout(kafkaConsumerConfigData.getPollTimeoutMs());

        return factory;
    }
    @Bean
    public ConsumerFactory<K, V> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        return Map.ofEntries(
                Map.entry(BOOTSTRAP_SERVERS_CONFIG, kafkaConfigData.getBootstrapServers()),
                Map.entry(KEY_DESERIALIZER_CLASS_CONFIG, kafkaConsumerConfigData.getKeyDeserializer()),
                Map.entry(VALUE_DESERIALIZER_CLASS_CONFIG, kafkaConsumerConfigData.getValueDeserializer()),
                Map.entry(GROUP_ID_CONFIG, kafkaConsumerConfigData.getConsumerGroupId()),
                Map.entry(AUTO_OFFSET_RESET_CONFIG, kafkaConsumerConfigData.getAutoOffsetReset()),
                Map.entry(kafkaConfigData.getSchemaRegistryUrlKey(), kafkaConfigData.getSchemaRegistryUrl()),
                Map.entry(kafkaConsumerConfigData.getSpecificAvroReaderKey(), kafkaConsumerConfigData.getSpecificAvroReader()),
                Map.entry(SESSION_TIMEOUT_MS_CONFIG, kafkaConsumerConfigData.getSessionTimeoutMs()),
                Map.entry(HEARTBEAT_INTERVAL_MS_CONFIG, kafkaConsumerConfigData.getHeartbeatIntervalMs()),
                Map.entry(MAX_POLL_INTERVAL_MS_CONFIG, kafkaConsumerConfigData.getMaxPollIntervalMs()),
                Map.entry(MAX_POLL_RECORDS_CONFIG, kafkaConsumerConfigData.getMaxPollRecords()),
                Map.entry(FETCH_MAX_BYTES_CONFIG, kafkaConsumerConfigData.getFetchMaxBytes()));
    }
}
