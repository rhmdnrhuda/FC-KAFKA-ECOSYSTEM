package com.demokafka.controller;

import com.demokafka.model.Voucher;
import com.demokafka.repository.VoucherRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client/voucher")
public class VoucherController {

  private final RedisTemplate<String, Object> redisTemplate;
  private final VoucherRepository voucherRepository;

  public VoucherController(RedisTemplate<String, Object> redisTemplate, VoucherRepository voucherRepository) {
    this.redisTemplate = redisTemplate;
    this.voucherRepository = voucherRepository;
  }

  @Autowired
  ObjectMapper objectMapper;

  @RequestMapping("/{voucherCode}")
  public ResponseEntity<Voucher> getVoucher(@PathVariable String voucherCode) {


    // Cache Aside Pattern
    // Cache Aside Pattern is a pattern that is used to keep the data in sync between the cache and the database.
    // When the data is requested, the application first checks the cache. If the data is found in the cache, it is returned to the user.
    // If the data is not found in the cache, the application fetches the data from the database, updates the cache, and then returns the data to the user.
    Voucher voucher = objectMapper.convertValue(redisTemplate.opsForValue().get(voucherCode), Voucher.class);
    if (Objects.isNull(voucher)) {
      voucher = voucherRepository.findByCode(voucherCode);
      if (Objects.nonNull(voucher)) {
        redisTemplate.opsForValue().set(voucherCode, voucher);
      }
    }

    return ResponseEntity.ok().body(voucher);
  }

  @RequestMapping("/delete/{voucherCode}")
  public ResponseEntity<String> deleteVoucher(@PathVariable String voucherCode) {
    Voucher voucher = voucherRepository.findByCode(voucherCode);
    if (Objects.nonNull(voucher)) {
      voucherRepository.delete(voucher);
      redisTemplate.delete(voucherCode);
    }

    return ResponseEntity.ok().body("Voucher deleted");
  }

  @RequestMapping()
  public ResponseEntity<Iterable<Voucher>> getAllVouchers() {
    return ResponseEntity.ok().body(voucherRepository.findAll());
  }

}
