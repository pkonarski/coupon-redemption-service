package com.pk.couponRedemption.api.coupon;

import com.pk.couponRedemption.api.shared.dto.CustomErrorResponse;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice(assignableTypes = {CouponController.class})
public class CouponExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CustomErrorResponse handleMethodArgumentValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Field not valid"));

        return new CustomErrorResponse("Bad Request", errors);
    }
}
