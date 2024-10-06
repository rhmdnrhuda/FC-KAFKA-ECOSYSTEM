package com.dummy.transaction.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;


@Configuration
@EnableKafka
public class KafkaConfig {

  @Value("${topic.dummy-transaction-topic}")
  private String dummyTransactionTopic;


  @Bean
  public NewTopic dummyTransactionTopic() {
    return TopicBuilder.name(dummyTransactionTopic)
        .partitions(3)
        .replicas(3)
        .build();
  }

}
