package com.kafkademo.repository;

import com.kafkademo.model.DlqData;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DlqRepository extends MongoRepository<DlqData, String> {

  List<DlqData> findTop10ByOrderByIdAsc();

}
