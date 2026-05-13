package com.pk.couponRedemption.exception.coupon;

public class CouponLimitReachedException extends RuntimeException {
    public CouponLimitReachedException(String code) {
        super(String.format("Limit was reached for coupon with code: %s", code));
    }
}
