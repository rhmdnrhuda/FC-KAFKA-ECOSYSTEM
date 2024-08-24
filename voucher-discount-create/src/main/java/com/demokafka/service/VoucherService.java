package com.demokafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import com.demokafka.model.Voucher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.demokafka.repository.VoucherRepository;

@Service
@Slf4j
public class VoucherService {

  private final VoucherRepository voucherRepository;

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  public VoucherService(VoucherRepository voucherRepository) {
    this.voucherRepository = voucherRepository;
  }

  public Voucher createVoucher(Voucher voucher) throws JsonProcessingException {
    // Save voucher to database
    Voucher savedVoucher = voucherRepository.save(voucher);

    // Publish to Kafka
    String value = objectMapper.writeValueAsString(savedVoucher);
    String key = "voucher-issued-request-" + savedVoucher.getId();
    kafkaTemplate.send("voucher-topic", value);

    return savedVoucher;
  }
}
