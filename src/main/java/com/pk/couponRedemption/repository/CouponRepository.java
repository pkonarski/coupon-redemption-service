package com.pk.couponRedemption.repository;

import com.pk.couponRedemption.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    boolean existsByCode(String code);

    Optional<Coupon> findByCode(String code);

    @Modifying
    @Query("""
            UPDATE Coupon c
            SET c.currentUsages = c.currentUsages + 1
            WHERE c.code = :code AND c.currentUsages < c.maxUsages
    """)
    int incrementCodeUsage(@Param("code") String code);
}
