/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demokafka;

import demokafka.model.Transaction;
import java.time.Duration;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

/**
 * Skeleton for a Flink DataStream Job.
 *
 * <p>For a tutorial how to write a Flink application, check the
 * tutorials and examples on the <a href="https://flink.apache.org">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class DataStreamJobBack {

  public static void main(String[] args) throws Exception {
    // Sets up the execution environment, which is the main entry point
    // to building Flink applications.
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    String topic = "dummy-transaction-topic";
    String topic2 = "dummy-window-transaction-topic";
    String groupId = "flink-group";

    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, "1000");
    properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    properties.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");

    KafkaSource<String> source = KafkaSource.<String>builder()
        .setBootstrapServers("localhost:9092")
        .setTopics(topic)
        .setGroupId(groupId)
        .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST))
        .setValueOnlyDeserializer(new SimpleStringSchema())
        .setProperties(properties)
        .build();

    DataStream<String> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

    ObjectMapper objectMapper = new ObjectMapper();

    DataStream<Transaction> transactionDataStream = stream
        .map(value -> objectMapper.readValue(value, Transaction.class))
        .assignTimestampsAndWatermarks(WatermarkStrategy.<Transaction>forBoundedOutOfOrderness(Duration.ofSeconds(5))
            .withTimestampAssigner((event, timestamp) -> event.getTransactionDate().getTime())
            .withIdleness(Duration.ofMinutes(1)))
        .process(new DataStreamJob.WatermarkLogger());

//    DataStream<Transaction> transactionDataStream = stream
//        .map(value -> objectMapper.readValue(value, Transaction.class))
//        .assignTimestampsAndWatermarks(WatermarkStrategy.forGenerator(context -> new CustomWatermarkGenerator())
//            .withTimestampAssigner((event, timestamp) -> event.getTransactionDate().getTime())
//            .withIdleness(Duration.ofMinutes(1)))
//        .process(new WatermarkLogger());

    transactionDataStream.print();

    // Execute program, beginning computation.
    env.execute("Flink Java API Skeleton");
  }


  public static class WatermarkLogger extends ProcessFunction<Transaction, Transaction> {

    @Override
    public void processElement(Transaction value, ProcessFunction<Transaction, Transaction>.Context ctx,
        Collector<Transaction> collector) throws Exception {
      // log watermark
      System.out.println("Processed Transaction: " + value.getTransactionId() +
          " | Event Time: " + value.getTransactionDate() +
          " | Current Watermark: " + ctx.timerService().currentWatermark());
    }
  }
}
