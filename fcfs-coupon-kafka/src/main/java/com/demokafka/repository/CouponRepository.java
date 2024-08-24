package com.demokafka.repository;


import com.demokafka.model.Coupon;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.text.html.Option;
import org.springframework.stereotype.Repository;

@Repository
public class CouponRepository {

  private ConcurrentHashMap<String, Coupon> couponStore = new ConcurrentHashMap<>();

  public void save(Coupon coupon) {
    couponStore.put(coupon.getCouponCode(), coupon);
  }

  public Optional<Coupon> findByCouponCode(String couponCode) {
    return Optional.ofNullable(couponStore.get(couponCode));
  }

}
