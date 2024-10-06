package demokafka;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.functions.ScalarFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ETLJob {

  private static final Logger logger = LoggerFactory.getLogger(ETLJob.class);

  public static void main(String[] args) throws Exception {
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    EnvironmentSettings settings = EnvironmentSettings.newInstance()
        .inStreamingMode()
        .build();

    final TableEnvironment tableEnv = TableEnvironment.create(settings);

    env.setParallelism(1); // Adjust based on your requirements

    // Enable checkpointing
    env.enableCheckpointing(10000); // Checkpoint every 10 seconds

    registerCatalog(tableEnv);

    // Register the UpperCase function
    tableEnv.createTemporarySystemFunction("upperCase", UpperCase.class);

    // Step 3: Register Source Table (transactions)
    try {
      tableEnv.executeSql(
          "CREATE TABLE transactions (" +
              "transaction_id STRING, " +
              "product_id STRING, " +
              "product_name STRING, " +
              "product_category STRING, " +
              "product_price DECIMAL(10, 2), " +
              "product_quantity INT, " +
              "product_brand STRING, " +
              "total_amount DECIMAL(18, 2), " +
              "currency STRING, " +
              "customer_id STRING, " +
              "transaction_date TIMESTAMP(3), " +
              "payment_method STRING" +
              ") WITH (" +
              "'connector' = 'jdbc'," +
              "'url' = 'jdbc:mysql://localhost:3306/fc-db'," +
              "'table-name' = 'transactions'," +
              "'driver' = 'com.mysql.cj.jdbc.Driver'," +
              "'username' = 'root'," +
              "'password' = 'root'" +
              ")");
      logger.info("Created transactions table successfully.");
    } catch (Exception e) {
      logger.error("Error creating transactions table", e);
      return;
    }

    // Step 4: Create the Target Table (transactions_back)
    try {
      tableEnv.executeSql(
          "CREATE TABLE transactions_back (" +
              "transaction_id STRING PRIMARY KEY NOT ENFORCED, " +
              "product_id STRING, " +
              "product_name STRING, " +
              "product_category STRING, " +
              "product_price DECIMAL(10, 2), " +
              "product_quantity INT, " +
              "product_brand STRING, " +
              "total_amount DECIMAL(18, 2), " +
              "currency STRING, " +
              "customer_id STRING, " +
              "transaction_date TIMESTAMP(3), " +
              "payment_method STRING" +
              ") WITH (" +
              "'connector' = 'jdbc'," +
              "'url' = 'jdbc:mysql://localhost:3306/fc-db'," +
              "'table-name' = 'transactions_back'," +
              "'driver' = 'com.mysql.cj.jdbc.Driver'," +
              "'username' = 'root'," +
              "'password' = 'root'" +
              ")");
      logger.info("Created transactions_back table successfully.");
    } catch (Exception e) {
      logger.error("Error creating transactions_back table", e);
      return;
    }

    // Step 5: Write the Transformation Query

    try {
      tableEnv.executeSql(
          "CREATE VIEW new_transactions AS " +
              "SELECT * FROM transactions");
      logger.info("Created new_transactions view successfully.");
    } catch (Exception e) {
      logger.error("Error creating new_transactions view", e);
      return;
    }

    // Insert new transactions that are not already in the back table
    try {
      tableEnv.executeSql(
          "INSERT INTO transactions_back " +
              "SELECT transaction_id, product_id, upperCase(product_name), upperCase(product_category), " +
              "product_price, product_quantity, product_brand, " +
              "total_amount, " +
              "currency, customer_id, transaction_date, payment_method " +
              "FROM new_transactions AS nt " +
              "WHERE NOT EXISTS (SELECT 1 FROM transactions_back tb WHERE tb.transaction_id = nt.transaction_id)");
      logger.info("Inserted new transactions successfully.");
    } catch (Exception e) {
      logger.error("Error inserting new transactions", e);
      return;
    }

    // Update existing records
    try {
      tableEnv.executeSql(
          "UPDATE transactions_back " +
              "SET product_id = nt.product_id, " +
              "upperCase(product_name) = nt.product_name, " +
              "upperCase(product_category) = nt.product_category, " +
              "product_price = nt.product_price, " +
              "product_quantity = nt.product_quantity, " +
              "product_brand = nt.product_brand, " +
              "total_amount = nt.total_amount, " +
              "currency = nt.currency, " +
              "customer_id = nt.customer_id, " +
              "transaction_date = nt.transaction_date, " +
              "payment_method = nt.payment_method " +
              "FROM new_transactions nt " +
              "WHERE transactions_back.transaction_id = nt.transaction_id");
      logger.info("Updated existing transactions successfully.");
    } catch (Exception e) {
      logger.error("Error updating existing transactions", e);
      return;
    }

    // Execute the job
    env.execute("ETL Job from transactions to transactions_back");
  }

  private static void registerCatalog(TableEnvironment tableEnv) {
    // Register the catalog; here we're assuming you want to create a MySQL catalog
    String catalogSql = "CREATE CATALOG my_catalog WITH (" +
        "'type' = 'jdbc', " +
        "'default-database' = 'fc-db', " +
        "'username' = 'root', " +
        "'password' = 'root', " +
        "'driver' = 'com.mysql.cj.jdbc.Driver', " +
        "'url' = 'jdbc:mysql://localhost:3306/fc-db')";

    try {
      tableEnv.executeSql(catalogSql);
      tableEnv.useCatalog("my_catalog"); // Use the catalog after creation
      logger.info("Catalog my_catalog registered successfully.");
    } catch (Exception e) {
      logger.error("Error registering catalog my_catalog", e);
    }
  }

  public static class UpperCase extends ScalarFunction {

    public String eval(String s) {
      return s.toUpperCase();
    }
  }

}



