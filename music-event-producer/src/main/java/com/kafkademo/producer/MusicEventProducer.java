package com.kafkademo.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkademo.domain.MusicEvent;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class MusicEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topic}")
    private String topic;

    public MusicEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public CompletableFuture<SendResult<String, String>> sendMusicEvent(MusicEvent musicEvent) throws JsonProcessingException {
        String value = objectMapper.writeValueAsString(musicEvent);
        String key = "music-event-" + musicEvent.musicEventId().toString();

        return kafkaTemplate.executeInTransaction(operations -> {
            CompletableFuture<SendResult<String, String>> future = operations.send(buildProducerRecord(key, value, topic));
            future.whenComplete((sendResult, throwable) -> {
                if (throwable == null) {
                    handleSuccess(key, value, sendResult);
                } else {
                    handleFailure(key, value, throwable);
                    throw new RuntimeException("Failed to send message", throwable); // Propagate exception for rollback
                }
            });
            return future;
        });
    }

    private ProducerRecord<String, String> buildProducerRecord(String key, String value, String topic) {
        List<Header> headers = List.of(
                new RecordHeader("event-source", "API".getBytes()),
                new RecordHeader("user", "user".getBytes()),
                new RecordHeader("music-label", "music-label".getBytes()),
                new RecordHeader("timestamp", String.valueOf(System.currentTimeMillis()).getBytes())
        );
        return new ProducerRecord<>(topic, null, key, value, headers);
    }

    private void handleFailure(String key, String value, Throwable throwable) {
        log.error("Error sending message: {}", throwable.getMessage());
    }

    private void handleSuccess(String key, String value, SendResult<String, String> sendResult) {
        log.info("Message sent successfully: key={}, value={}, partition={}, offset={}",
                key, value, sendResult.getRecordMetadata().partition(), sendResult.getRecordMetadata().offset());
    }
}
