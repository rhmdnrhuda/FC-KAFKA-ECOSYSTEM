package com.demokafka.controller;

import com.demokafka.producer.CouponProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
* Command to test the endpoint:
* curl --location --request POST 'http://localhost:8099/api/coupons/issued?couponCode=BLALA&userId=user2'
* */
@RestController
@RequestMapping("/api/coupons")
public class CouponController {

  @Autowired
  private CouponProducer couponProducer;

  @PostMapping("/issued")
  public ResponseEntity<Object> issuedCoupon(@RequestParam("couponCode") String couponCode, @RequestParam("userId") String userId)
      throws JsonProcessingException {
    couponProducer.sendCoupon(couponCode, userId);
    return ResponseEntity.ok().build();
  }

}
