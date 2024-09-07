package com.kafkademo.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkademo.domain.MusicEvent;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

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
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Configure retries and backoff
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 101);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 102);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 1000);

        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 10000); // 10 seconds
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        this.producer = producer;
    }

    private final KafkaProducer<String, String> producer;

    public CompletableFuture<SendResult<String, String>> sendMusicEvent(MusicEvent musicEvent) throws JsonProcessingException {

        for (int i = 1; i <= 2; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>("topic-test-ya", "key#" + i, "value#" + i);
            long startTime = System.currentTimeMillis();
            producer.send(record, (metadata, exception) -> {
                long endTime = System.currentTimeMillis();
                if (exception == null) {
                    System.out.println("Message sent successfully to topic " + metadata.topic() + " partition " + metadata.partition() + " offset " + metadata.offset() + " in " + (endTime - startTime) + " ms");
                } else {
                    System.err.println("Error sending message: " + exception.getMessage());
                }
            });
        }

        return null;
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
