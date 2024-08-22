package com.kafkademo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafkademo.domain.MusicEvent;
import com.kafkademo.producer.MusicEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class MusicEventController {

  @Autowired
  MusicEventProducer musicEventProducer;


  @PostMapping("/v1/music-event")
  public ResponseEntity<MusicEvent> postMusicEvent(
      @RequestBody MusicEvent musicEvent
  ) throws JsonProcessingException {

    log.info("Received music event request: {}", musicEvent);

    musicEventProducer.sendMusicEvent(musicEvent);

    log.info("Returning response: {}", musicEvent);
    return ResponseEntity.status(HttpStatus.CREATED).body(musicEvent);
  }
}
