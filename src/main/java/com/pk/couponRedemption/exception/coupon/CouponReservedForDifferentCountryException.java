package com.pk.couponRedemption.exception.coupon;

public class CouponReservedForDifferentCountryException extends RuntimeException {
    public CouponReservedForDifferentCountryException(String code, String usageCountryCode) {
        super(String.format("Coupon with code %s cannot be used for country %s.", code, usageCountryCode));
    }
}
