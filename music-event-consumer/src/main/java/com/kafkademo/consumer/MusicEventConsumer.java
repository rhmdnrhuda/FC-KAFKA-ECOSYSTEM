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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MusicEventConsumer {

  @Autowired
  ObjectMapper objectMapper;

  Integer count = 0;

  @KafkaListener(topics = "music-events-2" , containerFactory = "kafkaListenerContainerFactory")
  private void onMessage(ConsumerRecord<String, String> record,
                         Consumer<String, String> consumer,
                         Acknowledgment acknowledgment)  throws JsonProcessingException {
    log.info("Consumer Record : {} ", record);
    processMusicEvent(record);

    if (count % 2 == 0) { // simulasi -> error dari processMusicEvent -> retry
      consumer.commitSync(); // commit offset
//      acknowledgment.acknowledge();
    } else {
      log.info("Message not acknowledged : {} ", record);
    }
    count++;
  }

//  @KafkaListener(topics = "music-events", containerFactory = "kafkaListenerContainerFactory")
//  public void onMessage(ConsumerRecord<String, String> record,
//                        Consumer<String, String> consumer) throws JsonProcessingException {
//    System.out.println("Processing message: " + record.value());
//    processMusicEvent(record);
//    // Create a TopicPartition instance
//    TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
//
//    // Create OffsetAndMetadata instance for committing the offset
//    OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(record.offset() + 1);
//
//    // Commit the offset manually
//    consumer.commitSync(Collections.singletonMap(topicPartition, offsetAndMetadata));
//  }

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
