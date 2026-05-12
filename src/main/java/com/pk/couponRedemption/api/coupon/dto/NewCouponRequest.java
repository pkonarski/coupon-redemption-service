package com.pk.couponRedemption.api.coupon.dto;

import com.pk.couponRedemption.api.coupon.validation.CountryCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record NewCouponRequest(
        @NotBlank
        String code,

        @Min(1)
        int maxUsages,

        @NotNull
        @CountryCode
        String countryCode
) {

}
