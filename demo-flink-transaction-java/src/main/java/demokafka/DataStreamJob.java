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

import demokafka.model.BiggestTransactionPerCategory;
import demokafka.model.Transaction;
import demokafka.model.UserTransactionAggregates;
import java.time.Duration;
import java.util.Properties;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.elasticsearch.sink.Elasticsearch7SinkBuilder;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentType;

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
public class DataStreamJob {

  private static ObjectMapper objectMapper = new ObjectMapper();

  public static void main(String[] args) throws Exception {
    // Sets up the execution environment, which is the main entry point
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    String topic = "dummy-transaction-topic";
    String alertTopic = "fraud-alert-transaction-topic";
    String biggestTransactionTopicPerCategory = "biggest-transaction-per-category-topic";
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
            .withIdleness(Duration.ofMinutes(1)));

    processBiggestTransaction(transactionDataStream, biggestTransactionTopicPerCategory);
    processInsertTransaction(transactionDataStream);
    processUsersTransaction(transactionDataStream);
    processElasticSearchSink(transactionDataStream);

    env.execute("Flink Transaction");
  }

  private static void processElasticSearchSink(DataStream<Transaction> transactionDataStream) {

    ObjectMapper objectMapper = new ObjectMapper();

    transactionDataStream.sinkTo(
        new Elasticsearch7SinkBuilder<Transaction>()
            .setHosts(new HttpHost("localhost", 9200, "http"))
            .setEmitter((transaction, runtimeContext, requestIndexer) -> {

              String json = null;
              try {
                json = objectMapper.writeValueAsString(transaction);
              } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
              }

              IndexRequest indexRequest = Requests.indexRequest()
                  .index("transactions")
                  .id(transaction.getTransactionId())
                  .source(json, XContentType.JSON);
              requestIndexer.add(indexRequest);
            })
            .build()
    ).name("Elasticsearch Sink");
  }

  private static void processInsertTransaction(DataStream<Transaction> transactionDataStream) {
    JdbcExecutionOptions jdbcExecutionOptions = JdbcExecutionOptions.builder()
        .withBatchSize(1000)
        .withBatchIntervalMs(200)
        .withMaxRetries(3)
        .build();

    JdbcConnectionOptions jdbcConnectionOptions = new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
        .withUrl("jdbc:mariadb://localhost:3306/fc-db?useUnicode=true&characterEncoding=UTF-8&useSSL=false")
        .withUsername("org.mariadb.jdbc.Driver")
        .withUsername("root")
        .withPassword("root")
        .build();

    transactionDataStream
        .addSink(JdbcSink.sink(
            "INSERT INTO transactions (transaction_id, product_id, product_name, product_category, product_price, " +
                "product_quantity, product_brand, total_amount, currency, customer_id, payment_method) "
                +
                "VALUES (?,? ,?, ?, ?, ?, ?, ?, ?, ?, ?)",
            (ps, value) -> {
              ps.setString(1, value.getTransactionId());
              ps.setString(2, value.getProductId());
              ps.setString(3, value.getProductName());
              ps.setString(4, value.getProductCategory());
              ps.setDouble(5, value.getProductPrice());
              ps.setInt(6, value.getProductQuantity());
              ps.setString(7, value.getProductBrand());
              ps.setDouble(8, value.getTotalAmount());
              ps.setString(9, value.getCurrency());
              ps.setString(10, value.getCustomerId());
              ps.setString(11, value.getPaymentMethod());
            },
            jdbcExecutionOptions,
            jdbcConnectionOptions
        )).name("Transaction MYSQL Sink");
  }

  private static void processUsersTransaction(DataStream<Transaction> transactionDataStream) {
    JdbcExecutionOptions jdbcExecutionOptions = JdbcExecutionOptions.builder()
        .withBatchSize(1000)
        .withBatchIntervalMs(200)
        .withMaxRetries(3)
        .build();

    JdbcConnectionOptions jdbcConnectionOptions = new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
        .withUrl("jdbc:mariadb://localhost:3306/fc-db?useUnicode=true&characterEncoding=UTF-8&useSSL=false")
        .withUsername("org.mariadb.jdbc.Driver")
        .withUsername("root")
        .withPassword("root")
        .build();

    DataStream<UserTransactionAggregates> userTransactionAggregatesDataStream = transactionDataStream
        .keyBy(Transaction::getCustomerId)
        .window(TumblingEventTimeWindows.of(Time.minutes(1)))
        .aggregate(new UserTransactionAggregatesAggregator())
        .name("User Transaction Aggregates");

    userTransactionAggregatesDataStream.print();

    userTransactionAggregatesDataStream
        .addSink(JdbcSink.sink(
            "INSERT INTO user_transaction_aggregates (customer_id, total_amount, currency) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "total_amount = VALUES(total_amount), " +
                "currency = VALUES(currency)",
            (ps, value) -> {
              ps.setString(1, value.getCustomerId());
              ps.setDouble(2, value.getTotalAmount());
              ps.setString(3, value.getCurrency());
            },
            jdbcExecutionOptions,
            jdbcConnectionOptions
        ))
        .name("User Transaction Aggregates MySQL Sink");
  }


  private static void processBiggestTransaction(DataStream<Transaction> transactionDataStream, String topic) {

    // Step 1: Find the biggest transaction
    DataStream<Transaction> biggestTransaction = transactionDataStream
        .filter(transaction -> transaction.getTotalAmount() >= 100) // Filter transactions with amount >= 100
        .keyBy(Transaction::getProductCategory) // Key by product category
        .window(TumblingEventTimeWindows.of(Time.minutes(1))) // Apply 1-minute tumbling window
        .reduce((t1, t2) -> t1.getTotalAmount() > t2.getTotalAmount() ? t1 : t2); // Reduce to keep highest totalAmount

    KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
        .setBootstrapServers("localhost:9092")
        .setRecordSerializer(KafkaRecordSerializationSchema.builder()
            .setTopic(topic)
            .setValueSerializationSchema(new SimpleStringSchema())
            .build())
        .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
        .setProperty("transaction.timeout.ms", "60000")
        .build();

    DataStream<BiggestTransactionPerCategory> biggestTransactionPerCategoryDataStream = biggestTransaction
        .map(transaction -> BiggestTransactionPerCategory.builder()
            .productCategory(transaction.getProductCategory())
            .totalAmount(transaction.getTotalAmount())
            .currency(transaction.getCurrency())
            .customerId(transaction.getCustomerId())
            .paymentMethod(transaction.getPaymentMethod())
            .timestamp(System.currentTimeMillis())
            .transactionDate(transaction.getTransactionDate())
            .build());

    biggestTransactionPerCategoryDataStream.print();

    ObjectMapper objectMapper = new ObjectMapper();

    biggestTransactionPerCategoryDataStream
        .map(objectMapper::writeValueAsString)
        .sinkTo(kafkaSink)
        .name("Biggest Transaction Per Category Per Windows Kafka Sink");
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

  public static class UserTransactionAggregatesAggregator
      implements AggregateFunction<Transaction, UserTransactionAggregates, UserTransactionAggregates> {

    @Override
    public UserTransactionAggregates createAccumulator() {
      return UserTransactionAggregates.builder()
          .customerId("")
          .totalAmount(0)
          .currency("")
          .build();
    }

    @Override
    public UserTransactionAggregates add(Transaction value, UserTransactionAggregates accumulator) {
      return UserTransactionAggregates.builder()
          .customerId(value.getCustomerId())
          .totalAmount(accumulator.getTotalAmount() + value.getTotalAmount())
          .currency(value.getCurrency())
          .build();
    }

    @Override
    public UserTransactionAggregates getResult(UserTransactionAggregates accumulator) {
      return accumulator;
    }

    @Override
    public UserTransactionAggregates merge(UserTransactionAggregates a, UserTransactionAggregates b) {
      return UserTransactionAggregates.builder()
          .customerId(a.getCustomerId())
          .totalAmount(a.getTotalAmount() + b.getTotalAmount())
          .currency(a.getCurrency())
          .build();
    }
  }

}
