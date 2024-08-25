package com.demokafka.repository;

import com.demokafka.model.VoucherSubscription;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VoucherSubscriptionRepository extends MongoRepository<VoucherSubscription, String> {

}
