package com.kafkademo.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkademo.domain.MusicEvent;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.protocol.types.Field.Str;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MusicEventProducer {

  KafkaTemplate<String, String>  kafkaTemplate;
  ObjectMapper objectMapper;

  @Value("${spring.kafka.topic}")
  private String topic;

  public MusicEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
  }

  public CompletableFuture<SendResult<String, String>> sendMusicEvent(MusicEvent musicEvent) throws JsonProcessingException {

    String value = objectMapper.writeValueAsString(musicEvent);

    String key = "music-event-" +  "msg-1"  ;
    return
    kafkaTemplate.send(buildProducerRecord(key, value, topic))
            .whenComplete((sendResult, throwable) -> {
              if (throwable != null) {
                handleFailure(key, value, throwable);
              } else {
                handleSuccess(key, value, sendResult);
              }
            });
  }

  private ProducerRecord<String, String> buildProducerRecord(String key, String value, String topic) {

    RecordHeader headerEventResource = new RecordHeader("event-source", "API".getBytes());
    RecordHeader headerUser = new RecordHeader("user", "user".getBytes());
    RecordHeader headerMusicLabelResource = new RecordHeader("music-label", "music-label".getBytes());
    RecordHeader headerTimestamp = new RecordHeader("timestamp", String.valueOf(System.currentTimeMillis()).getBytes());

    List<Header> headers = List.of(headerEventResource, headerUser, headerMusicLabelResource, headerTimestamp);

    return new ProducerRecord<>(topic, null, key, value, headers);
  }

  private void handleFailure(String key, String value, Throwable throwable) {
    log.error("Error sending message: {}", throwable.getMessage());
  }

  private void handleSuccess(String key, String value, SendResult<String, String> sendResult) {
    log.info("Message sent successfully: key={}, value={}, partition={}, offset={}",
        key, value, sendResult.getRecordMetadata().partition(), sendResult.getRecordMetadata().offset());
  }
}
