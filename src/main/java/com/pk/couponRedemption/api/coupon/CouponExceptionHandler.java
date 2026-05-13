package com.pk.couponRedemption.api.coupon;

import com.pk.couponRedemption.api.shared.dto.CustomErrorResponse;
import com.pk.couponRedemption.exception.coupon.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = CouponController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CouponExceptionHandler {

    @ExceptionHandler(CouponNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public CustomErrorResponse handleMissingCoupon(CouponNotFoundException e) {
        String message = "Requested coupon not found";
        log.warn(message, e);
        return new CustomErrorResponse(message);
    }

    @ExceptionHandler(CouponReservedForDifferentCountryException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public CustomErrorResponse handleCouponReservedForDifferentCountry(CouponReservedForDifferentCountryException e) {
        String message = "Coupon is dedicated for different country";
        log.warn(message, e);
        return new CustomErrorResponse(message);
    }

    @ExceptionHandler(CouponLimitReachedException.class)
    @ResponseStatus(HttpStatus.GONE)
    public CustomErrorResponse handleCouponLimitReached(CouponLimitReachedException e) {
        String message = "Coupon limit was reached";
        log.warn(message, e);
        return new CustomErrorResponse(message);
    }

    @ExceptionHandler(CouponAlreadyUsedByUserException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public CustomErrorResponse handleCouponAlreadyUsedByUser(CouponAlreadyUsedByUserException e) {
        String message = "Coupon already used";
        log.warn(message, e);
        return new CustomErrorResponse(message);
    }

    @ExceptionHandler(CouponAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public CustomErrorResponse handleAlreadyCreatedCoupon(CouponAlreadyExistsException e) {
        String message = "Coupon already exists";
        log.warn(message, e);
        return new CustomErrorResponse(message);
    }
}
