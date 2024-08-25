package com.demokafka.service;

import com.demokafka.model.Voucher;
import com.demokafka.model.VoucherSubscription;
import com.demokafka.repository.VoucherSubscriptionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.shaded.com.google.protobuf.Any;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class VoucherSubscriptionService {

    private final VoucherSubscriptionRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public VoucherSubscriptionService(VoucherSubscriptionRepository repository, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.restTemplate = new RestTemplate();
    }

    public VoucherSubscription subscribeToVoucher(VoucherSubscription subscription) throws Exception {
        // Check if voucher exists via outbound call (e.g., REST API call)
        String voucherServiceUrl = "http://localhost:8003/client/voucher/" + subscription.getVoucherCode();
        log.info("Voucher service URL: {}", voucherServiceUrl);

        // http get request to voucher service
        Voucher voucherExists = restTemplate.getForEntity(voucherServiceUrl, Voucher.class).getBody();

        if (Objects.nonNull(voucherExists)) {
            // Save to database
            VoucherSubscription savedSubscription = repository.save(subscription);

            // Publish to Kafka if voucher exists
            String value = objectMapper.writeValueAsString(savedSubscription);
            String key = "voucher-subscription-request-" + savedSubscription.getId();
            kafkaTemplate.send("voucher-subscription-topic", key, value);

            // Delete voucher
            String voucherServiceUrlDelete = "http://localhost:8003/client/voucher/delete/" + subscription.getVoucherCode();
            restTemplate.delete(voucherServiceUrlDelete);
        } else {
            throw new RuntimeException("Voucher does not exist");
        }

        return subscription;
    }
}
