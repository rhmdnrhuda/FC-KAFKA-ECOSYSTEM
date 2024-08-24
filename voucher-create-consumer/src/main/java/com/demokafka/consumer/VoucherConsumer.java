package com.demokafka.consumer;

import com.demokafka.model.Voucher;
import com.demokafka.repository.VoucherRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VoucherConsumer {

  @Autowired
  ObjectMapper objectMapper;

  private final VoucherRepository voucherRepository;

  @Autowired
  public VoucherConsumer(VoucherRepository voucherRepository) {
    this.voucherRepository = voucherRepository;
  }

  @KafkaListener(topics = "voucher-topic", groupId = "voucher-consumer-mysql-group")
  public void consume(String string) throws JsonProcessingException {
    Voucher voucher = objectMapper.readValue(string, Voucher.class);

    log.info("Consuming voucher: {}", string);

    voucherRepository.save(voucher);
  }

}
