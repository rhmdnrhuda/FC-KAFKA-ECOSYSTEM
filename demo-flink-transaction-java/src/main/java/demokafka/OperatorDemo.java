package demokafka;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class OperatorDemo {

  public static void main(String[] args) throws Exception {
    // Set up the execution environment
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    // Create a sample data stream (list of integers)
    DataStream<Integer> inputData = env.fromElements(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    DataStream<String> dash = env.fromElements("======");

    // Apply a map transformation to square each integer
    DataStream<Integer> squaredData = inputData.map(value -> value * value);

    // Apply a flatMap transformation to split each integer into two integers
    DataStream<Integer> splitData = inputData.flatMap((FlatMapFunction<Integer, Integer>) (integer, collector) -> {
      collector.collect(integer);
      collector.collect(integer * 2);
    }).returns(Integer.class);

    // Apply filter transformation to filter out even numbers
    DataStream<Integer> oddData = inputData.filter(value -> value % 2 != 0);

    // Example: KeyBy the integers (group by the value)
    // Example: Reduce the data by summing up the counts for each key
    DataStream<Tuple2<Integer, Integer>> keyedData = splitData
        .map(value ->
            new Tuple2<>(value, 1), TypeInformation.of(new TypeHint<Tuple2<Integer, Integer>>() {
        }))  // Create a tuple with (integer, 1)
        .keyBy(tuple -> tuple.f0) // Key by the integer value
        .reduce((tuple1, tuple2) -> new Tuple2<>(tuple1.f0, tuple1.f1 + tuple2.f1)); // Sum the counts

    // Print the final output
    dash.print();
    keyedData.print();

    // Execute the job
    env.execute("Simple Flink Operator Demo");
  }
}
