package com.kafkademo.producer;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;
import java.util.concurrent.TimeoutException;

/*
* Create 3 brokers
* create with 2 min in sync replice
* Create a topic with replication factor 3:  docker exec -it a1fb68ebca0d85399da2a5e14fdc14d0fb7cda509383bb3a1a1bcdd4f1be999f kafka-topics --create --topic test-topic --bootstrap-server kafka1:9092 --replication-factor 3 --partitions 1
 *
* */

public class KafkaProducerWithRetry {

    public static void main(String[] args) {
        // Kafka producer configuration settings
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092"); // Kafka broker
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        // ACKS configuration for durability and data consistency
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Ensure all in-sync replicas must acknowledge
        props.put(ProducerConfig.RETRIES_CONFIG, 5); // Number of retries before giving up
        props.put("retry.backoff.ms", 1000); // Wait time between retries
        props.put("max.block.ms", 3000); // Max time to block before throwing an exception

        // Create Kafka producer instance
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        // Define the topic and the message to send
        String topic = "financial-transactions";
        String key = "transaction-key";
        String value = "transaction-data";

        // Create producer record
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);

        try {
            // Send the message asynchronously with callback
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception == null) {
                        // Successfully sent the message
                        System.out.println("Message sent successfully to partition " + metadata.partition() +
                                " with offset " + metadata.offset());
                    } else {
                        // Handle failure
                        if (exception instanceof TimeoutException) {
                            System.err.println("Message failed due to TimeoutException: " + exception.getMessage());
                        } else {
                            System.err.println("Message failed: " + exception.getMessage());
                        }
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Failed to send message: " + e.getMessage());
        } finally {
            // Close the producer to release resources
            producer.close();
        }
    }
}
