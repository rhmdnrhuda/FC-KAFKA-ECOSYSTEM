package com.kafkademo.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.util.backoff.FixedBackOff;

@EnableKafka
@Configuration
@Slf4j
public class KafkaConfig {

  @Autowired
  KafkaTemplate<String, String> kafkaTemplate;

  @Value("${topics.retry-topic}")
  private String retryTopic;

  @Value("${topics.retry-topic-dlt}")
  private String errorTopic;

  @Value("${groupId.retry-consumer-group}")
  private String retryConsumerGroup;

  public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer() {
    return new DeadLetterPublishingRecoverer(kafkaTemplate, (r, e) -> {
      if (e.getCause() instanceof RecoverableDataAccessException) {
        return new TopicPartition(retryTopic, 0);
      } else {
        return new TopicPartition(errorTopic, 0);
      }
    });
  }

  public DefaultErrorHandler errorHandler() {

    FixedBackOff backOff = new FixedBackOff();
    backOff.setInterval(1000); // 1 second
    backOff.setMaxAttempts(1); // 3 retries

    ExponentialBackOff exponentialBackOff = new ExponentialBackOff();
    exponentialBackOff.setInitialInterval(1000); // 1 second
    exponentialBackOff.setMultiplier(2); // 2x
    exponentialBackOff.setMaxInterval(10000); // 10 seconds
    exponentialBackOff.setMaxAttempts(5);

//    var exceptionToIgnoreList = List.of(
//        IllegalArgumentException.class,
//        NullPointerException.class
//    );

    var exceptionToRetryList = List.of(
        RecoverableDataAccessException.class
    );

    DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(
        deadLetterPublishingRecoverer(),
        backOff);

    defaultErrorHandler.setRetryListeners(((record, ex, deliveryAttempt) -> {
      log.error("Error consuming message: {}, deliveryAttempt: {}", record.value(), deliveryAttempt, ex);
      // trigger some action after retry
      // e.g. send an email, notify a monitoring system, etc.
    }));

//    exceptionToIgnoreList.forEach(defaultErrorHandler::addNotRetryableExceptions);
    exceptionToRetryList.forEach(defaultErrorHandler::addRetryableExceptions);

    return defaultErrorHandler;
  }

  @Bean
  public ConsumerFactory<String, String> consumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092, localhost:9093, localhost:9094");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, retryConsumerGroup);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    factory.setCommonErrorHandler(errorHandler());
    return factory;
  }

}
