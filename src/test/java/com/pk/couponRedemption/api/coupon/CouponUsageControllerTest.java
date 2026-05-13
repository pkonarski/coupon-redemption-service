package com.pk.couponRedemption.api.coupon;

import com.pk.couponRedemption.api.coupon.dto.CouponUsageResponse;
import com.pk.couponRedemption.exception.coupon.*;
import com.pk.couponRedemption.exception.geolocation.UserCountryCodeParseException;
import com.pk.couponRedemption.service.coupon.CouponRedemptionService;
import com.pk.couponRedemption.service.coupon.CouponRegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponController.class)
public class CouponUsageControllerTest {
    @MockitoBean
    private CouponRegistrationService couponRegistrationService;

    @MockitoBean
    private CouponRedemptionService couponRedemptionService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void handleSuccessfulCouponRedemption() throws Exception {
        UUID sampleUsageId = UUID.randomUUID();
        Instant usedAt = Instant.now();

        when(couponRedemptionService.useCoupon(anyString(), anyString(), anyString())).thenReturn(new CouponUsageResponse(sampleUsageId, usedAt));

        mockMvc.perform(
                        post("/api/coupons/SAMPLECODE/redemption")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                            {
                                                "userId": "sampleUsageId"
                                            }
                                        """
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usageId").value(sampleUsageId.toString()))
                .andExpect(jsonPath("$.usedAt").value(usedAt.toString()));
    }

    @ParameterizedTest
    @MethodSource("provideCouponExceptions")
    void handleCouponUsageException(RuntimeException exception, ResultMatcher matcher) throws Exception {
        when(couponRedemptionService.useCoupon(anyString(), anyString(), anyString())).thenThrow(exception);
        mockMvc.perform(
                        post("/api/coupons/SAMPLECODE/redemption")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                            {
                                                "userId": "sampleUserId"
                                            }
                                        """
                                )
                )
                .andExpect(matcher);
    }

    private static Stream<Arguments> provideCouponExceptions() {
        return Stream.of(
                Arguments.of(new CouponAlreadyUsedByUserException("sampleCode", "sampleUserId", new RuntimeException()), status().isConflict()),
                Arguments.of(new CouponLimitReachedException("sampleCode"), status().isGone()),
                Arguments.of(new CouponNotFoundException("sampleCode", "sampleCountryCode"), status().isNotFound()),
                Arguments.of(new CouponReservedForDifferentCountryException("sampleCode", "sampleUsageCountryCode"), status().isForbidden()),
                Arguments.of(new UserCountryCodeParseException("errorMessage", new RuntimeException()), status().isServiceUnavailable())
        );
    }
}
