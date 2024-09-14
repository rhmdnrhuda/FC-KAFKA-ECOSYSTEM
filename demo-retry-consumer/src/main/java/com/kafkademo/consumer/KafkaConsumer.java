package com.kafkademo.consumer;

import com.kafkademo.model.DlqData;
import com.kafkademo.repository.DlqRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumer {

  @Autowired
  DlqRepository dlqRepository;

  @KafkaListener(topics = "kafka_example")
  public void consume(String message) throws Exception {
    // create consumer to test retry in KafkaConfig.java
    try {
      switch (message) {
        case "error" -> throw new Exception("Error");
        case "illegal" -> throw new IllegalArgumentException("Illegal Argument");
        case "null" -> throw new NullPointerException("Null Pointer");
        case "retry" -> throw new RecoverableDataAccessException("Retry");
      }
      log.info("Consumed message: {}", message);
    } catch (Exception e) {
      log.error("Error consuming message: {}", message);
      throw e;
    }
  }

  @KafkaListener(topics = "${topics.retry-topic}", groupId = "${groupId.retry-consumer-group}")
  public void consumeRetry(ConsumerRecord<String, String> consumerRecord) {

    consumerRecord.headers()
        .forEach(header -> log.info("Header from DLT: key:{}, value:{}", header.key(), new String(header.value())));

    log.info("Consumed message from retry topic: {}", consumerRecord.value());
  }

  @KafkaListener(topics = "${topics.retry-topic-dlt}", groupId = "${groupId.retry-consumer-group}")
  public void consumeDlt(ConsumerRecord<String, String> consumerRecord) {

    // insert dlt to database

    String originalTopic = Optional.ofNullable(consumerRecord.headers().lastHeader("x-original-topic"))
        .map(header -> new String(header.value()))
        .orElse("unknown");

    DlqData dlqData = DlqData.builder()
        .topic(consumerRecord.topic())
        .key(consumerRecord.key())
        .value(consumerRecord.value())
        .originalTopic(originalTopic)
        .build();

    dlqRepository.save(dlqData);

    log.info("Consumed message from DLT topic: {}", consumerRecord.value());
  }

}
