package com.demokafka.consumer;

import com.demokafka.model.Voucher;
import com.demokafka.model.VoucherSubscription;
import com.demokafka.service.ActivityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ActivityTrackerConsumer {

  @Autowired
  ActivityService activityService;

  @Autowired
  ObjectMapper objectMapper;

  @KafkaListener(topics = "voucher-subscription-topic", groupId = "voucher-consumer-elastic-group")
  public void consume(String string) throws JsonProcessingException {
    log.info("Consuming voucher subscription: {}", string);

    VoucherSubscription voucherSubscription = objectMapper.readValue(string, VoucherSubscription.class);

    activityService.trackActivityVoucherSubscription(voucherSubscription);
  }

  @KafkaListener(topics = "voucher-topic", groupId = "voucher-consumer-elastic-group")
  public void consumeVoucher(String string) throws JsonProcessingException {
    log.info("Consuming voucher: {}", string);

    Voucher voucher = objectMapper.readValue(string, Voucher.class);

    activityService.trackActivityVoucherCreated(voucher);
  }

}
