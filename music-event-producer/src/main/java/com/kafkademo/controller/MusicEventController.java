package com.kafkademo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafkademo.domain.MusicEvent;
import com.kafkademo.producer.MusicEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
public class MusicEventController {

  @Autowired
  MusicEventProducer musicEventProducer;


  @PostMapping("/v1/music-event")
  @Retryable(value = {JsonProcessingException.class}, maxAttempts = 5)
  public ResponseEntity<MusicEvent> postMusicEvent(
      @RequestBody MusicEvent musicEvent
  ) throws JsonProcessingException {

    log.info("Received music event request: {}", musicEvent);

    musicEventProducer.sendMusicEvent(musicEvent);

    log.info("Returning response: {}", musicEvent);
    return ResponseEntity.status(HttpStatus.CREATED).body(musicEvent);
  }

  @Recover
  public ResponseEntity<MusicEvent> recover(JsonProcessingException e, MusicEvent musicEvent) {
    log.error("Failed to process music event after retries: {}", musicEvent, e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(musicEvent);
  }
}
