package com.demokafka.service;

import com.demokafka.model.Voucher;
import com.demokafka.repository.VoucherRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VoucherService {

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  private final VoucherRepository voucherRepository;

  @Autowired
  public VoucherService(VoucherRepository voucherRepository) {
    this.voucherRepository = voucherRepository;
  }

  public Voucher createVoucher(Voucher voucher) throws JsonProcessingException {

    // save voucher to database
    Voucher savedVoucher = voucherRepository.save(voucher);

    // publish to kafka
    String value = objectMapper.writeValueAsString(savedVoucher);
    String key = "voucher-created-request-" + savedVoucher.getId();
    kafkaTemplate.send("voucher-topic", key, value);

    return savedVoucher;
  }
}
