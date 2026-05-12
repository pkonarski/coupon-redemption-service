package com.pk.couponRedemption.api.coupon.dto;

import java.time.Instant;
import java.util.UUID;

public record CouponUsageResponse(
        UUID usageId,
        Instant usedAt
) {
}
