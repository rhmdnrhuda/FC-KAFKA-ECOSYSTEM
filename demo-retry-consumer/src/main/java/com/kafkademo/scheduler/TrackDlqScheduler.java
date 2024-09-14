package com.kafkademo.scheduler;

import com.kafkademo.repository.DlqRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrackDlqScheduler {

  @Autowired
  private DlqRepository dlqRepository;

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  @Value("${topics.tracking-topic}")
  private String trackingTopic;


  @Scheduled(fixedRate = 10000) // 10 seconds
  public void trackDlq() {
    log.info("Tracking DLQ");

    dlqRepository.findTop10ByOrderByIdAsc().forEach(dlqData -> {
      log.info("Tracking message: {}", dlqData);

      kafkaTemplate.send(trackingTopic, dlqData.getKey(), dlqData.getValue());

      dlqRepository.delete(dlqData);
    });
  }

}
