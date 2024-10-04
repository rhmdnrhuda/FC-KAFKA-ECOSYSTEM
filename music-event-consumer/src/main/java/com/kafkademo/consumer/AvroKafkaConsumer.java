package com.kafkademo.consumer;

import com.kafkademo.domain.User;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.Schema;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class AvroKafkaConsumer {
    private static final String SCHEMA_REGISTRY_URL = "http://localhost:8081";
    private static final String SUBJECT = "user-value";

    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";
        String schemaRegistryUrl = "http://localhost:8081";
        String groupId = "avro-consumer-group";
        String topic = "user";

        // Consumer properties
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        properties.put("schema.registry.url", schemaRegistryUrl);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put("specific.avro.reader", "true");  // Required for deserializing specific Avro records

        // Create the consumer
        KafkaConsumer<String, User> consumer = new KafkaConsumer<>(properties);

        // Subscribe to the topic
        consumer.subscribe(Collections.singletonList(topic));

        // Poll for new data
        try {
            while (true) {
                ConsumerRecords<String, User> records = consumer.poll(Duration.ofMillis(1000));

                records.forEach(record -> {
                    System.out.println("Key: " + record.key() + ", Value: " + record.value());
                    System.out.println("Partition: " + record.partition() + ", Offset: " + record.offset());
                });
            }
        } finally {
            consumer.close();
        }
    }

    private static Schema fetchSchemaFromRegistry() {
        try {
            URL url = new URL(SCHEMA_REGISTRY_URL + "/subjects/" + SUBJECT + "/versions/latest");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            // Log the response for debugging
            String schemaJson = response.toString();
            System.out.println("Schema Registry response: " + schemaJson);

            // Extract schema from the response using JSONObject
            JSONObject jsonObject = new JSONObject(schemaJson);
            String schemaString = jsonObject.getString("schema");

            return new Schema.Parser().parse(schemaString);

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch schema from registry", e);
        }
    }
}
