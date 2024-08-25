package com.demokafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@EnableKafka
@Configuration
public class KafkaConfig {

  @Bean
  public NewTopic voucherTopic() {
    return TopicBuilder.name("voucher-subscription-topic")
        .partitions(3)
        .replicas(3)
        .build();
  }

}
