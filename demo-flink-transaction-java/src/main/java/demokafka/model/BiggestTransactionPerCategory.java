package demokafka.model;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BiggestTransactionPerCategory implements Serializable {

  private String productCategory;
  private double totalAmount;
  private String currency;
  private String customerId;
  private String paymentMethod;
  private long timestamp;
  private Date transactionDate;

}
