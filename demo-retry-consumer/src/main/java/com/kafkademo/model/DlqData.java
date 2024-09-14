package com.kafkademo.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@SuperBuilder
@NoArgsConstructor
@Data
@Document(collection = "dlq_data")
public class DlqData {

  @Id
  private String id;
  private String topic;
  private String key;
  private String value;
  private String originalTopic;

}
