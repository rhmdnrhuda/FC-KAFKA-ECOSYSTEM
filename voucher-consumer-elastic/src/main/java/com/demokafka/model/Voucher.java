package com.demokafka.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Voucher implements Serializable {

  private Long id;
  private String code;
  private double discount;

}
