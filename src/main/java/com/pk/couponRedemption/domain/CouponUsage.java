package com.pk.couponRedemption.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "coupons_usage")
@Getter
public class CouponUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    @Column(name = "used_at", updatable = false, nullable = false)
    private Instant usedAt;

    @Column(name = "user_id", updatable = false, nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    public static CouponUsage create(Coupon coupon, String userId) {
        Objects.requireNonNull(coupon, "Coupon reference is required for coupon usage");
        Objects.requireNonNull(userId, "User id must be specified for coupon usage");

        CouponUsage couponUsage = new CouponUsage();
        couponUsage.coupon = coupon;
        couponUsage.userId = userId;

        return couponUsage;
    }
}
