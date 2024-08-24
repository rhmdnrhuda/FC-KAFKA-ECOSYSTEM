package com.demokafka.controller;

import com.demokafka.model.Voucher;
import com.demokafka.service.VoucherService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/voucher")
public class VoucherController {

  @Autowired
  private final VoucherService voucherService;

  public VoucherController(VoucherService voucherService) {
    this.voucherService = voucherService;
  }

  @PostMapping()
  public ResponseEntity<Voucher> createVoucher(@RequestBody Voucher request) throws JsonProcessingException {

    Voucher voucher = voucherService.createVoucher(request);
    return ResponseEntity.ok().body(voucher);

  }
}
