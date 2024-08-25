package com.demokafka.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@SuperBuilder
@Data
@NoArgsConstructor
@JsonIgnoreProperties
@Document(collection = "voucher_subscription")
public class VoucherSubscription {

  @Id
  private String id;
  private String userId;
  private String voucherCode;

}
