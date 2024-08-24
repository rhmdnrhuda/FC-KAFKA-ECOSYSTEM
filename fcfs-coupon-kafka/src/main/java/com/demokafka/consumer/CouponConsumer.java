package com.demokafka.consumer;

import com.demokafka.model.Coupon;
import com.demokafka.repository.CouponRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CouponConsumer {

  @Autowired
  private CouponRepository couponRepository;

  @Autowired
  ObjectMapper objectMapper;

  @KafkaListener(topics = "coupon-issuance", groupId = "coupon-consumer-group")
  public void consume(String string) throws JsonProcessingException {
    // Implement the logic to consume the message from the Kafka topic

    Coupon coupon = objectMapper.readValue(string, Coupon.class);

    couponRepository.findByCouponCode(coupon.getCouponCode()).ifPresentOrElse(existingCoupon -> {
      if ("PENDING".equals(existingCoupon.getStatus())) {
        existingCoupon.setStatus("ISSUED");
        couponRepository.save(existingCoupon);
        log.info("Coupon issued to user: {}", existingCoupon.getUserId());
      } else {
        log.info("Coupon already issued to user: {}", existingCoupon.getUserId());
      }
    }, () -> {
      coupon.setStatus("ISSUED");
      couponRepository.save(coupon);
      log.info("Coupon issued to user: {}", coupon.getUserId());
    });

  }

}
