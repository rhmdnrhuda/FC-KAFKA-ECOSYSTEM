package com.kafkademo.transforms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.transforms.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class StringToJsonTransform<R extends ConnectRecord<R>> implements Transformation<R> {

    private static final Logger log = LoggerFactory.getLogger(StringToJsonTransform.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public R apply(R record) {
        if (record.value() == null) {
            return record;
        }

        try {
            // Parse the string value as JSON
            String value = (String) record.value();
            JsonNode jsonNode = objectMapper.readTree(value);

            // Define the schema for the JSON object
            Schema schema = SchemaBuilder.struct()
                    .field("userId", Schema.STRING_SCHEMA)
                    .field("action", Schema.STRING_SCHEMA)
                    .field("page", Schema.STRING_SCHEMA)
                    .field("timestamp", Schema.STRING_SCHEMA)
                    .build();

            // Create a struct based on the JSON content
            Struct struct = new Struct(schema)
                    .put("userId", jsonNode.get("userId").asText())
                    .put("action", jsonNode.get("action").asText())
                    .put("page", jsonNode.get("page").asText())
                    .put("timestamp", jsonNode.get("timestamp").asText());

            // Return the new record with the JSON object as value
            return record.newRecord(record.topic(), record.kafkaPartition(), record.keySchema(), record.key(), schema, struct, record.timestamp());
        } catch (IOException e) {
            log.error("Failed to convert string to JSON", e);
            throw new DataException("Failed to convert string to JSON", e);
        }
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef();
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
    }
}
