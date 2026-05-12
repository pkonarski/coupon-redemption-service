package com.pk.couponRedemption.api.coupon.dto;

import jakarta.persistence.Column;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

public record CouponResponse(
        UUID id,
        String code,
        Instant createdAt,
        int maxUsages,
        int currentUsages,
        String countryCode
) {
}
