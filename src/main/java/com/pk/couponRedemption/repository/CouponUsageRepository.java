package com.pk.couponRedemption.repository;

import com.pk.couponRedemption.domain.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, UUID> {
}
