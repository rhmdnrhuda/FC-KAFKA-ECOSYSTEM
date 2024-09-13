package com.kafkademo.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AutoConfiguration {

  @Value("${spring.kafka.topic}")
  private String topic;

  @Bean
  public NewTopic musicEvent() {
    return org.springframework.kafka.config.TopicBuilder
            .name("topic-test-ya")
            .partitions(3)
            .replicas(3)
//            .config("min.insync.replicas", "2") // Setting min.insync.replicas
            .build();
  }

  @Bean
  public ProducerFactory<String, String> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.ACKS_CONFIG, "all");
    configProps.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, 1000); // Lower metadata max age

    configProps.put(ProducerConfig.RETRIES_CONFIG, 2); // Set retries
    configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100); // Set retry backoff
    configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 100); // Set request timeout

    configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 60000);

    configProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 60000); // in ms

    configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);

    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean
  public KafkaTemplate<String, String> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

}
