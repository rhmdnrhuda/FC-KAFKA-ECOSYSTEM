package com.demokafka.repository;

import com.demokafka.model.ActivityTracker;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ActivityRepository extends ElasticsearchRepository<ActivityTracker, String> {

}
