package com.pk.couponRedemption.exception;

public class CouponUsageException extends RuntimeException {
    private CouponUsageException(String message) { super(message); }
    private CouponUsageException(String message, Throwable e) { super(message, e); }

  public static CouponUsageException whenCouponUsedForUser(String code, String userId, Throwable rootCause) {
      return new CouponUsageException(String.format("Coupon with code: %s was used by user with id: %s", code, userId), rootCause);
  }

  public static CouponUsageException whenLimitReached(String code) {
      return new CouponUsageException(String.format("Limit has been reached for coupon with code: %s", code));
  }
}
