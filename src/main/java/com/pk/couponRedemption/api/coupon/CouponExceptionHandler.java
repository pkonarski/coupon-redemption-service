package com.pk.couponRedemption.api.coupon;

import com.pk.couponRedemption.api.shared.dto.CustomErrorResponse;
import com.pk.couponRedemption.exception.CouponAlreadyExistsException;
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

    @ExceptionHandler(CouponAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public CustomErrorResponse handleAlreadyCreatedCoupon(CouponAlreadyExistsException e) {
        String message = "Coupon already exists";
        log.warn(message, e);
        return new CustomErrorResponse(message);
    }
}
