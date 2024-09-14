package com.kafkademo.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.json.JsonConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ProduceSerializer {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.connect.json.JsonSerializer");

        KafkaProducer<String, Struct> producer = new KafkaProducer<>(props);

        Schema schema = SchemaBuilder.struct()
                .field("field1", Schema.STRING_SCHEMA)
                .field("field2", Schema.INT32_SCHEMA)
                .build();

        Struct value = new Struct(schema)
                .put("field1", "value1")
                .put("field2", 123);

        ProducerRecord<String, Struct> record = new ProducerRecord<>("test-topic-kafka-sink", value);

        producer.send(record);
        producer.close();
    }
}
