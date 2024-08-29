package com.kafkademo.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkademo.domain.MusicDB;
import com.kafkademo.domain.MusicEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class MusicEventConsumer {

  @Autowired
  ObjectMapper objectMapper;


  List<MusicDB> musics = new ArrayList<>();
  // kafka -> key dan value

  Set<String> keys = new HashSet<>();

  @KafkaListener(topics = "music-events", containerFactory = "kafkaListenerContainerFactory")
  public void onMessage(ConsumerRecord<String, String> record,
                        Consumer<String, String> consumer) throws JsonProcessingException {

    System.out.println("Processing message: " + record.value());

    if (keys.contains(record.key())) {
      log.info("Key already exist: {}", record.key());
      //      update music data sesuai id yang dikirim
      return;
    }

    keys.add(record.key());

    processMusicEvent(record);
    // Create a TopicPartition instance
    TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());

    // Create OffsetAndMetadata instance for committing the offset
    OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(record.offset() + 1);

    // Commit the offset manually
    consumer.commitSync(Collections.singletonMap(topicPartition, offsetAndMetadata));
  }

  private void processMusicEvent(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
    MusicEvent musicEvent = objectMapper.readValue(consumerRecord.value(), MusicEvent.class);
    log.info("MusicEvent : {} ", musicEvent);

    // Business Logic
    switch (musicEvent.musicEventType()) {
      case CREATE:
        log.info("Music Event Created");
        MusicDB music = new MusicDB(musicEvent.music().musicId(), musicEvent.music().musicName(), musicEvent.music().musicAuthor());
        musics.add(music); // simlasi DB insert

        break;
      case UPDATE:
        log.info("Music Event Updated");
        break;
      default:
        log.info("Invalid Music Event Type");
    }
  }

  public boolean isExist(Integer id) {
    for (MusicDB music : musics) {
      if (music.getId().equals(id)) {
        return true;
      }
    }
    return false;
  }


}
