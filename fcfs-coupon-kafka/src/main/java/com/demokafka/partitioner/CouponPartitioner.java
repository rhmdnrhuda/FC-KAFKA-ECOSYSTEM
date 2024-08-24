package com.demokafka.partitioner;

import java.util.Map;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CouponPartitioner implements Partitioner {

  private static final Logger logger = LoggerFactory.getLogger(CouponPartitioner.class);

  @Override
  public int partition(String topic, Object key, byte[] bytes, Object o1, byte[] bytes1, Cluster cluster) {
    // Implement the logic to partition the messages based on the coupon code
    // Sharding the messages based on the coupon code
    // Logic partition = couponCode.hashCode() % cluster.partitionCountForTopic(topic);

    String couponCodeKey = (String) key;
    int keyHash = couponCodeKey.hashCode();
    logger.info("Coupon Code : {} , Hash : {}", couponCodeKey, keyHash);

    int partition = keyHash % cluster.partitionCountForTopic(topic);
    logger.info("Coupon Code : {} , Partition : {}", couponCodeKey, partition);

    return Math.abs(partition);
  }

  @Override
  public void close() {

  }

  @Override
  public void configure(Map<String, ?> map) {

  }
}
