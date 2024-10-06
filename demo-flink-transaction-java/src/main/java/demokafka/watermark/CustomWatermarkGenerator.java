package demokafka.watermark;

import demokafka.model.Transaction;
import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkOutput;

public class CustomWatermarkGenerator implements WatermarkGenerator<Transaction> {

  private final long maxOutOfOrderness = 3500; // 3.5 seconds

  private long currentMaxTimestamp;

  @Override
  public void onEvent(Transaction transaction, long eventTimestamp, WatermarkOutput watermarkOutput) {
    currentMaxTimestamp = Math.max(currentMaxTimestamp, eventTimestamp);
  }

  @Override
  public void onPeriodicEmit(WatermarkOutput watermarkOutput) {
    watermarkOutput.emitWatermark(new Watermark(currentMaxTimestamp - maxOutOfOrderness - 1));
  }
}
