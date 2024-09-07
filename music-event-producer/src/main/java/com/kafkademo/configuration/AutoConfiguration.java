package com.kafkademo.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class AutoConfiguration {

  @Value("${spring.kafka.topic}")
  private String topic;

  @Bean
  public NewTopic musicEvent(){
    return TopicBuilder
        .name(topic)
        .partitions(3)
        .replicas(3)
        .build();
  }

  @Bean
  public ProducerFactory<String, String> producerFactory() {
    Map<String, Object> props = new HashMap<>();

    // Set the bootstrap server address
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094");

    // Set the key and value serializers
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    // Configure retries and backoff
    props.put(ProducerConfig.RETRIES_CONFIG, 3); // Retry up to 3 times
    props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 10); // Backoff delay (10 milliseconds) in milliseconds

    // Other configurations (optional)
    props.put(ProducerConfig.ACKS_CONFIG, "all"); // Ensure all replicas acknowledge
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "my-producer"); // Set a unique client ID

    return new DefaultKafkaProducerFactory<>(props);
  }

  @Bean
  public KafkaTemplate<String, String> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

}
