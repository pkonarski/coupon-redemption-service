package com.pk.couponRedemption.exception;

public class CouponNotFoundException extends RuntimeException {
    public CouponNotFoundException(String code, String countryCode) {
        super(String.format("Coupon with code: %s for country with code: %s not found", code, countryCode));
    }
}
