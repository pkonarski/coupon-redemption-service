package com.pk.couponRedemption.api.shared;

import com.pk.couponRedemption.api.shared.dto.CustomErrorResponse;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;


@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final Tracer tracer;

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CustomErrorResponse globalExceptionHandler(Throwable throwable) {
        String message = "Unknown Error Occurred";
        log.error(message, throwable);

        return new CustomErrorResponse(message);
    }
}
