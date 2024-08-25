package com.demokafka.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@SuperBuilder
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "activity-tracker")
public class ActivityTracker implements Serializable {

  @Id
  private String id;
  private String userId;
  private String voucherCode;
  private String action;
  private String timestamp;

}
