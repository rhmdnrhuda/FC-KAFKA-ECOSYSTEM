package com.demokafka.controller;

import com.demokafka.model.VoucherSubscription;
import com.demokafka.service.VoucherSubscriptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscribe")
public class VoucherSubscriptionController {

  private static final Logger log = LoggerFactory.getLogger(VoucherSubscription.class);

  @Autowired
  private final VoucherSubscriptionService voucherService;

  public VoucherSubscriptionController(VoucherSubscriptionService voucherService) {
    this.voucherService = voucherService;
  }

  @PostMapping
  public ResponseEntity<VoucherSubscription> subscribeToVoucher(@RequestBody VoucherSubscription request) {

    try {
      VoucherSubscription subscription = voucherService.subscribeToVoucher(request);
      return ResponseEntity.ok().body(subscription);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }
}
