package com.pk.couponRedemption.api.coupon.dto;

import jakarta.validation.constraints.NotBlank;

public record CouponUseRequest(
        @NotBlank String userId
) {

}
