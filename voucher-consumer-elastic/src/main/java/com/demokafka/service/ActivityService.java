package com.demokafka.service;

import com.demokafka.model.ActivityTracker;
import com.demokafka.model.Voucher;
import com.demokafka.model.VoucherSubscription;
import com.demokafka.repository.ActivityRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ActivityService {

  private final ActivityRepository activityRepository;

  public ActivityService(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  public void trackActivityVoucherCreated(Voucher voucher) {
    log.info("Tracking activity for voucher created: {}", voucher);

    ActivityTracker activityTracker = ActivityTracker.builder()
        .id(UUID.randomUUID().toString())
        .userId("admin")
        .voucherCode(voucher.getCode())
        .action("CREATE")
        .timestamp(Instant.now().toString())
        .build();

    // save to elastic
    activityRepository.save(activityTracker);
  }

  public void trackActivityVoucherSubscription(VoucherSubscription voucherSubscription) {
    log.info("Tracking activity for voucher subscription: {}", voucherSubscription);

    ActivityTracker activityTracker = ActivityTracker.builder()
        .id(UUID.randomUUID().toString())
        .userId(voucherSubscription.getUserId())
        .voucherCode(voucherSubscription.getVoucherCode())
        .action("SUBSCRIBE")
        .timestamp(Instant.now().toString())
        .build();

    // save to elastic
    activityRepository.save(activityTracker);
  }

}
