package com.kafkademo.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkademo.domain.MusicEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MusicEventConsumer {

  @Autowired
  ObjectMapper objectMapper;

  @KafkaListener(topics = "music-events")
  private void onMessage(ConsumerRecord<String, String> consumerRecord)  throws JsonProcessingException {
    processMusicEvent(consumerRecord);
  }

  private void processMusicEvent(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
    MusicEvent musicEvent = objectMapper.readValue(consumerRecord.value(), MusicEvent.class);
    log.info("MusicEvent : {} ", musicEvent);

    // Business Logic
    switch (musicEvent.musicEventType()) {
      case CREATE:
        log.info("Music Event Created");
        break;
      case UPDATE:
        log.info("Music Event Updated");
        break;
      default:
        log.info("Invalid Music Event Type");
    }
  }


}
