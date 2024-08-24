package com.demokafka.producer;

import com.demokafka.model.Coupon;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CouponProducer {

  private static final String TOPIC = "coupon-issuance";

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  @Autowired
  ObjectMapper objectMapper;

  public void sendCoupon(String couponCode, String userId) throws JsonProcessingException {

    Coupon coupon = Coupon.builder()
        .couponCode(couponCode)
        .userId(userId)
        .status("PENDING")
        .couponName("DISCOUNT " + couponCode)
        .build();

    String key = "coupon-issued-request-" + couponCode;
    String value = objectMapper.writeValueAsString(coupon);

    log.info("Sending Coupon : {} ", coupon);
    kafkaTemplate.send(TOPIC, key, value);
  }

}
