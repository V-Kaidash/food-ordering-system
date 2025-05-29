package com.food.ordering.system.kafka.consumer.config;

import com.food.ordering.system.kafka.config.data.KafkaConfigData;
import com.food.ordering.system.kafka.config.data.KafkaConsumerConfigData;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaConsumerConfig<K extends Serializable, V extends SpecificRecordBase> {

  private final KafkaConfigData kafkaConfigData;
  private final KafkaConsumerConfigData kafkaConsumerConfigData;

  public KafkaConsumerConfig(KafkaConfigData kafkaConfigData, KafkaConsumerConfigData kafkaConsumerConfigData) {
    this.kafkaConfigData = kafkaConfigData;
    this.kafkaConsumerConfigData = kafkaConsumerConfigData;
  }

  @Bean
  public Map<String, Object> consumerConfig() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigData.getBootstrapServers());
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaConsumerConfigData.getKeyDeserializer());
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaConsumerConfigData.getValueDeserializer());
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConsumerConfigData.getAutoOffsetReset());
    config.put(kafkaConfigData.getSchemaRegistryUrlKey(), kafkaConfigData.getSchemaRegistryUrl());
    config.put(kafkaConsumerConfigData.getSpecificAvroReaderKey(), kafkaConsumerConfigData.getSpecificAvroReader());
    config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaConsumerConfigData.getSessionTimeoutMs());
    config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, kafkaConsumerConfigData.getHeartbeatIntervalMs());
    config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, kafkaConsumerConfigData.getMaxPollIntervalMs());
    config.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG,
        kafkaConsumerConfigData.getMaxPartitionFetchBytesDefault() *
            kafkaConsumerConfigData.getMaxPartitionFetchBytesBoostFactor());
    config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaConsumerConfigData.getMaxPollRecords());
    return config;
  }

  @Bean
  public ConsumerFactory<K, V> consumerFactory(Map<String, Object> consumerConfig) {
    return new DefaultKafkaConsumerFactory<>(consumerConfig);
  }

  @Bean
  public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<K, V>> kafkaListenerContainerFactory(ConsumerFactory<K, V> consumerFactory) {
    ConcurrentKafkaListenerContainerFactory<K, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    factory.setBatchListener(kafkaConsumerConfigData.getBatchListener());
    factory.setConcurrency(kafkaConsumerConfigData.getConcurrencyLevel());
    factory.setAutoStartup(kafkaConsumerConfigData.getAutoStartup());
    factory.getContainerProperties().setPollTimeout(kafkaConsumerConfigData.getPollTimeoutMs());
    return factory;
  }
}
