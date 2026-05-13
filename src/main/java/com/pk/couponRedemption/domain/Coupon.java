package com.pk.couponRedemption.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "coupons")
@Getter
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "max_usages", nullable = false)
    private int maxUsages;

    @Column(name = "current_usages", nullable = false)
    private int currentUsages = 0;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    public static Coupon create(String code, int maxUsages, String countryCode) {
        String baseOperationErrorMsg = "Coupon creation error";
        Objects.requireNonNull(code, String.format("%s. Coupon code required", baseOperationErrorMsg));
        Objects.requireNonNull(countryCode, String.format("%s. Country code required", baseOperationErrorMsg));
        if (maxUsages <= 0) throw new IllegalArgumentException(String.format("%s. Max usages must be greater than 0", baseOperationErrorMsg));

        Coupon coupon = new Coupon();

        coupon.code = code.toUpperCase(Locale.ROOT);
        coupon.maxUsages = maxUsages;
        coupon.countryCode = countryCode;

        return coupon;
    }

    public boolean isLimitReached() {
        return currentUsages >= maxUsages;
    }
}
