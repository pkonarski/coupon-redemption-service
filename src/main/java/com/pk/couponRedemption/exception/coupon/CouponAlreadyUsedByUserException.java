package com.pk.couponRedemption.exception.coupon;

public class CouponAlreadyUsedByUserException extends RuntimeException {
    public CouponAlreadyUsedByUserException(String code, String userId, Throwable rootCause) {
        super(String.format("Coupon with code: %s was used by user with id: %s", code, userId), rootCause);
    }
}
