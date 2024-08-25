package com.demokafka.consumer;

import com.demokafka.model.Voucher;
import com.demokafka.repository.VoucherRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VoucherConsumer {

  @Autowired
  ObjectMapper objectMapper;

  private final VoucherRepository voucherRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  @Autowired
  public VoucherConsumer(VoucherRepository voucherRepository, RedisTemplate<String, Object> redisTemplate) {
    this.voucherRepository = voucherRepository;
    this.redisTemplate = redisTemplate;
  }

  @KafkaListener(topics = "voucher-topic", groupId = "voucher-consumer-mysql-group")
  public void consume(String string) throws JsonProcessingException {
    Voucher voucher = objectMapper.readValue(string, Voucher.class);

    log.info("Consuming voucher: {}", string);

    voucherRepository.save(voucher);

    String value = objectMapper.writeValueAsString(voucher);

    redisTemplate.opsForValue().set(voucher.getCode(), value, Duration.ofMinutes(10));
  }

}
