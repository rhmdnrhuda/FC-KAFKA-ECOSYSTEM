package com.kafkademo.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkademo.domain.MusicEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.kafka.common.utils.Utils.sleep;

@Component
@Slf4j
public class MusicEventConsumer {

  @Autowired
  ObjectMapper objectMapper;

  @KafkaListener(topics = "music-events" , containerFactory = "kafkaListenerContainerFactory")
  private void onMessage(ConsumerRecord<String, String> record,
                         Acknowledgment acknowledgment)  throws JsonProcessingException {
    processMusicEvent(record);
    acknowledgment.acknowledge();

    Long threadName = Thread.currentThread().getId(); // Get thread ID
    Map<String, Object> logData = new HashMap<>();
    logData.put("key", record.key());
    logData.put("threadID", threadName);
    logData.put("partition", record.partition());

    // Convert map to JSON string
    String logJson = objectMapper.writeValueAsString(logData);

    // Print JSON string
    System.out.println(logJson);
  }


  private void processMusicEvent(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
    MusicEvent musicEvent = objectMapper.readValue(consumerRecord.value(), MusicEvent.class);
//    log.info("MusicEvent : {} ", musicEvent);
//    sleep(1000);

    // Business Logic
    switch (musicEvent.musicEventType()) {
      case CREATE:
//        log.info("Music Event Created");
        break;
      case UPDATE:
        log.info("Music Event Updated");
        break;
      default:
        log.info("Invalid Music Event Type");
    }
  }


}
