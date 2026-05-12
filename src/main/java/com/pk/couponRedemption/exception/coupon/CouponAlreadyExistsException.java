package com.pk.couponRedemption.exception;

public class CouponAlreadyExistsException extends RuntimeException {
    private CouponAlreadyExistsException(String message) {
        super(message);
    }
    private CouponAlreadyExistsException(String message, Throwable e) { super(message, e); }

    public static CouponAlreadyExistsException whenCouponAlreadyExistsOnPlannedCheckAgainstDB(String code) {
        return new CouponAlreadyExistsException(String.format("Coupon with code: %s already exists", code));
    }

    public static CouponAlreadyExistsException whenDatabaseWriteConflictedOnExistingCoupon(String code, Throwable rootCause) {
        return new CouponAlreadyExistsException(String.format("Coupon with code: %s creation failure. " +
                "Coupon was already existing in the DB during save", code), rootCause);
    }
}
