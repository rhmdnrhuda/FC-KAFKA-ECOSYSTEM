package com.demokafka.repository;

import com.demokafka.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

  // find by code
  Voucher findByCode(String code);

}
