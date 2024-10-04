package com.kafkademo.producer;

import com.kafkademo.domain.User;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import org.json.JSONObject;

public class AvroKafkaProducer {

    private static final String SCHEMA_REGISTRY_URL = "http://localhost:8081";
    private static final String SUBJECT = "user-value";

    public static void main(String[] args) {
        String bootstrapServers = "localhost:9092";
        String topic = "user";

        // Fetch schema from Schema Registry
        Schema schema = fetchSchemaFromRegistry();

        // Producer properties
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        properties.put("schema.registry.url", SCHEMA_REGISTRY_URL);

        // Create Kafka Producer
        KafkaProducer<String, GenericRecord> producer = new KafkaProducer<>(properties);

        // Create a sample GenericRecord object (using Avro)
        GenericRecord userRecord = new org.apache.avro.generic.GenericData.Record(schema);
        userRecord.put("id", "5");
        userRecord.put("name", "Beckham");
        userRecord.put("email", "beck@example.com");

        // Create a ProducerRecord with the GenericRecord object
        ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(topic, "4", userRecord);

        // Send data to Kafka
        try {
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    System.out.println("Successfully sent record to Kafka:");
                    System.out.println("Topic: " + metadata.topic());
                    System.out.println("Partition: " + metadata.partition());
                    System.out.println("Offset: " + metadata.offset());
                } else {
                    exception.printStackTrace();
                }
            });
        } finally {
            // Close the producer
            producer.close();
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
