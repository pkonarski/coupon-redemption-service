package com.pk.couponRedemption.api.shared.dto;

import org.slf4j.MDC;

import java.util.Map;

public record CustomErrorResponse(
        String message,
        String traceId,
        Object details
) {

    public CustomErrorResponse(String message) {
        this(message, getTraceId(), Map.of());
    }

    public CustomErrorResponse(String message, Object details) {
        this(message, getTraceId(), details);
    }

    public static String getTraceId() {
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : "trace-id-unavailable";
    }
}
