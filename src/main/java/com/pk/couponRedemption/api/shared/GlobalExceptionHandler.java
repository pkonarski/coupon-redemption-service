package com.pk.couponRedemption.api.shared;

import com.pk.couponRedemption.api.shared.dto.CustomErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            @NonNull Exception ex, Object body, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status, @NonNull WebRequest request) {

        log.warn("Exception occurred during request processing", ex);

        if(body instanceof CustomErrorResponse) {
            return ResponseEntity.status(status).headers(headers).body(body);
        }

        String message = switch (status.value()) {
            case 400 -> "Malformed or invalid request";
            case 405 -> "Method not allowed";
            case 415 -> "Unsupported media type";
            default  -> "Request could not be processed";
        };

        var custom = new CustomErrorResponse(message);
        return ResponseEntity.status(status).headers(headers).body(custom);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        Map<String, String> errors = ex.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Field not valid"));

        return handleExceptionInternal(ex, new CustomErrorResponse("Validation failed", errors), headers, status, request);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CustomErrorResponse globalExceptionHandler(Throwable throwable) {
        String message = "Unknown Error Occurred";
        log.error(message, throwable);

        return new CustomErrorResponse(message);
    }
}
