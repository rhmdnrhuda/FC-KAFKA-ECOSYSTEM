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

  /*
{"threadID":50,"partition":0,"key":"music-event-msg-3"}
{"threadID":52,"partition":1,"key":"music-event-msg-5"}
{"threadID":54,"partition":2,"key":"music-event-msg-1"}
{"threadID":54,"partition":2,"key":"music-event-msg-2"}
{"threadID":52,"partition":1,"key":"music-event-msg-6"}
{"threadID":52,"partition":1,"key":"music-event-msg-12"}
{"threadID":54,"partition":2,"key":"music-event-msg-7"}
{"threadID":50,"partition":0,"key":"music-event-msg-4"}
{"threadID":54,"partition":2,"key":"music-event-msg-8"}
{"threadID":54,"partition":2,"key":"music-event-msg-11"}
{"threadID":54,"partition":2,"key":"music-event-msg-14"}
{"threadID":54,"partition":2,"key":"music-event-msg-15"}
{"threadID":54,"partition":2,"key":"music-event-msg-17"}
{"threadID":52,"partition":1,"key":"music-event-msg-13"}
{"threadID":54,"partition":2,"key":"music-event-msg-19"}
{"threadID":54,"partition":2,"key":"music-event-msg-21"}
{"threadID":52,"partition":1,"key":"music-event-msg-18"}
{"threadID":50,"partition":0,"key":"music-event-msg-9"}
{"threadID":50,"partition":0,"key":"music-event-msg-10"}
{"threadID":50,"partition":0,"key":"music-event-msg-16"}
{"threadID":50,"partition":0,"key":"music-event-msg-22"}
{"threadID":50,"partition":0,"key":"music-event-msg-24"}
{"threadID":50,"partition":0,"key":"music-event-msg-27"}
{"threadID":50,"partition":0,"key":"music-event-msg-30"}
{"threadID":50,"partition":0,"key":"music-event-msg-40"}
{"threadID":54,"partition":2,"key":"music-event-msg-23"}
{"threadID":54,"partition":2,"key":"music-event-msg-25"}
{"threadID":54,"partition":2,"key":"music-event-msg-28"}
{"threadID":54,"partition":2,"key":"music-event-msg-32"}
{"threadID":54,"partition":2,"key":"music-event-msg-34"}
{"threadID":54,"partition":2,"key":"music-event-msg-52"}
{"threadID":54,"partition":2,"key":"music-event-msg-53"}
{"threadID":52,"partition":1,"key":"music-event-msg-20"}
{"threadID":52,"partition":1,"key":"music-event-msg-26"}
{"threadID":52,"partition":1,"key":"music-event-msg-29"}
{"threadID":52,"partition":1,"key":"music-event-msg-31"}
{"threadID":52,"partition":1,"key":"music-event-msg-33"}
{"threadID":52,"partition":1,"key":"music-event-msg-35"}
{"threadID":52,"partition":1,"key":"music-event-msg-36"}
{"threadID":52,"partition":1,"key":"music-event-msg-37"}
{"threadID":52,"partition":1,"key":"music-event-msg-38"}
{"threadID":52,"partition":1,"key":"music-event-msg-39"}
{"threadID":50,"partition":0,"key":"music-event-msg-42"}
{"threadID":50,"partition":0,"key":"music-event-msg-43"}
{"threadID":50,"partition":0,"key":"music-event-msg-45"}
{"threadID":50,"partition":0,"key":"music-event-msg-47"}
{"threadID":50,"partition":0,"key":"music-event-msg-48"}
{"threadID":50,"partition":0,"key":"music-event-msg-51"}
{"threadID":52,"partition":1,"key":"music-event-msg-41"}
{"threadID":52,"partition":1,"key":"music-event-msg-44"}
{"threadID":54,"partition":2,"key":"music-event-msg-55"}
{"threadID":52,"partition":1,"key":"music-event-msg-46"}
{"threadID":52,"partition":1,"key":"music-event-msg-49"}
{"threadID":54,"partition":2,"key":"music-event-msg-56"}
{"threadID":52,"partition":1,"key":"music-event-msg-50"}
{"threadID":54,"partition":2,"key":"music-event-msg-58"}
{"threadID":52,"partition":1,"key":"music-event-msg-54"}
{"threadID":54,"partition":2,"key":"music-event-msg-59"}
{"threadID":52,"partition":1,"key":"music-event-msg-60"}
{"threadID":54,"partition":2,"key":"music-event-msg-67"}
{"threadID":52,"partition":1,"key":"music-event-msg-61"}
{"threadID":54,"partition":2,"key":"music-event-msg-70"}
{"threadID":52,"partition":1,"key":"music-event-msg-64"}
{"threadID":54,"partition":2,"key":"music-event-msg-79"}
{"threadID":52,"partition":1,"key":"music-event-msg-66"}
{"threadID":54,"partition":2,"key":"music-event-msg-80"}
{"threadID":54,"partition":2,"key":"music-event-msg-83"}
{"threadID":54,"partition":2,"key":"music-event-msg-88"}
{"threadID":50,"partition":0,"key":"music-event-msg-57"}
{"threadID":50,"partition":0,"key":"music-event-msg-62"}
{"threadID":50,"partition":0,"key":"music-event-msg-63"}
{"threadID":52,"partition":1,"key":"music-event-msg-68"}
{"threadID":54,"partition":2,"key":"music-event-msg-89"}
{"threadID":52,"partition":1,"key":"music-event-msg-69"}
{"threadID":54,"partition":2,"key":"music-event-msg-92"}
{"threadID":52,"partition":1,"key":"music-event-msg-71"}
{"threadID":52,"partition":1,"key":"music-event-msg-73"}
{"threadID":52,"partition":1,"key":"music-event-msg-74"}
{"threadID":54,"partition":2,"key":"music-event-msg-98"}
{"threadID":52,"partition":1,"key":"music-event-msg-76"}
{"threadID":52,"partition":1,"key":"music-event-msg-78"}
{"threadID":52,"partition":1,"key":"music-event-msg-84"}
{"threadID":54,"partition":2,"key":"music-event-msg-100"}
{"threadID":52,"partition":1,"key":"music-event-msg-85"}
{"threadID":52,"partition":1,"key":"music-event-msg-86"}
{"threadID":50,"partition":0,"key":"music-event-msg-65"}
{"threadID":52,"partition":1,"key":"music-event-msg-90"}
{"threadID":52,"partition":1,"key":"music-event-msg-95"}
{"threadID":52,"partition":1,"key":"music-event-msg-97"}
{"threadID":50,"partition":0,"key":"music-event-msg-72"}
{"threadID":50,"partition":0,"key":"music-event-msg-75"}
{"threadID":50,"partition":0,"key":"music-event-msg-77"}
{"threadID":50,"partition":0,"key":"music-event-msg-81"}
{"threadID":50,"partition":0,"key":"music-event-msg-82"}
{"threadID":50,"partition":0,"key":"music-event-msg-87"}
{"threadID":50,"partition":0,"key":"music-event-msg-91"}
{"threadID":50,"partition":0,"key":"music-event-msg-93"}
{"threadID":50,"partition":0,"key":"music-event-msg-94"}
{"threadID":50,"partition":0,"key":"music-event-msg-96"}
{"threadID":50,"partition":0,"key":"music-event-msg-99"}

  * */




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
