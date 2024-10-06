package demokafka.model;

import java.io.Serializable;
import java.sql.Timestamp;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction implements Serializable {

  private String transactionId;
  private String productId;
  private String productName;
  private String productCategory;
  private double productPrice;
  private int productQuantity;
  private String productBrand;
  private double totalAmount;
  private String currency;
  private String customerId;
  private Timestamp transactionDate;
  private String paymentMethod;
}
