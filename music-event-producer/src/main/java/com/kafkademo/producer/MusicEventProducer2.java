package com.kafkademo.producer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Properties;

public class MusicEventProducer2 {
    @Bean
    public NewTopic musicEvent(){
        return TopicBuilder
                .name("topic-test-ya")
                .partitions(3)
                .replicas(3)
                .build();
    }

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Configure retries and backoff
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 100);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 100);

        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 10000); // 10 seconds
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

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

        producer.close();
    }
}

