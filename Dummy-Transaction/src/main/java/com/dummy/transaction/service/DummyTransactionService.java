package com.dummy.transaction.service;


import com.dummy.transaction.model.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Random;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DummyTransactionService {

  private final Faker faker = new Faker();

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  KafkaTemplate<String, String> kafkaTemplate;

  @Value("${topic.dummy-transaction-topic}")
  private String dummyTransactionTopic;

  public Transaction createDummyTransaction() {
    String[] productIds = {"product1", "product2", "product3", "product4", "product5", "product6"};
    String[] productNames = {"laptop", "smartphone", "tablet", "smartwatch", "headphone", "earphone"};
    String[] productCategories = {"electronics", "gadget", "accessories", "wearable", "audio"};
    String[] paymentMethods = {"credit card", "debit card", "e-wallet", "bank transfer"};
    String[] customerIds = {"customer1", "customer2", "customer3", "customer4", "customer5", "customer6", "customer7", "customer8", "customer9", "customer10"};

    // Random instance for choosing random products
    Random random = new Random();

    // Select random product details
    String randomProductId = productIds[random.nextInt(productIds.length)];
    String randomProductName = productNames[random.nextInt(productNames.length)];
    String randomProductCategory = productCategories[random.nextInt(productCategories.length)];
    String randomCustomerId = customerIds[random.nextInt(customerIds.length)];

    BigDecimal productPrice = new BigDecimal(faker.commerce().price()).multiply(new BigDecimal(1000000))
        .setScale(0, BigDecimal.ROUND_HALF_UP);
    BigInteger productQuantity = BigInteger.valueOf(faker.number().numberBetween(1, 10));
    BigDecimal totalAmount = productPrice.multiply(new BigDecimal(productQuantity));

    // generate random uuid
    String randomUUIDString = UUID.randomUUID().toString();

    return Transaction.builder()
        .transactionId(randomUUIDString)
        .productId(randomProductId) // Using random product ID
        .productName(randomProductName) // Using random product name
        .productCategory(randomProductCategory) // Using random product category
        .productPrice(productPrice.doubleValue())
        .productQuantity(productQuantity.intValue())
        .productBrand(faker.company().name())
        .totalAmount(totalAmount.doubleValue())
        .currency("IDR")
        .customerId(randomCustomerId)
        .transactionDate(new Timestamp(System.currentTimeMillis()))
        .paymentMethod(paymentMethods[random.nextInt(paymentMethods.length)]) // Using random payment method
        .build();
  }

  public void publishTransaction(Transaction transaction) throws JsonProcessingException {
    log.info("Publishing transaction: {}", transaction);

    String key = "transaction-" + transaction.getTransactionId();
    String value = objectMapper.writeValueAsString(transaction);

    kafkaTemplate.send(dummyTransactionTopic, key, value);
  }

  public void generateAndPublishDummyTransaction() throws JsonProcessingException {
    /*
       Create a dummy transaction every 5 seconds
        and publish it to the Kafka topic
     */

    while (true) {
      Transaction transaction = createDummyTransaction();
      publishTransaction(transaction);
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        log.error("Error occurred while sleeping the thread: {}", e.getMessage());
      }
    }
  }

}
