package com.food.ordering.system.kafka.producer.service.impl;

import com.food.ordering.system.kafka.producer.exception.KafkaProducerException;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Slf4j
@Component
public class KafkaProducerImpl<K extends Serializable, V extends SpecificRecordBase> implements KafkaProducer<K, V> {
  private final KafkaTemplate<K, V> kafkaTemplate;

  public KafkaProducerImpl(KafkaTemplate<K, V> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public void send(String topicName, K key, V message, BiConsumer<SendResult<K, V>, Throwable> callback) {
    log.info("Sending message {} to topic: {}", message, topicName);
    try {
      CompletableFuture<SendResult<K, V>> kafkaResultFuture = kafkaTemplate.send(topicName, key, message);
      kafkaResultFuture.whenComplete(callback);
    } catch (KafkaException e) {
      log.error("Error on kafka producer sending message {} to topic: {}. Exception: {}", message, topicName, e.getMessage());
      throw new KafkaProducerException("Error on kafka producer sending message " + message + " to topic: " + topicName + ". Exception: " + e.getMessage());
    }
  }

  @PreDestroy
  public void close() {
    if (kafkaTemplate != null) {
      log.info("Closing kafka producer");
      kafkaTemplate.destroy();
    }
  }
}
