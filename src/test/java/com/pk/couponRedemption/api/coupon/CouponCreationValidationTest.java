package com.pk.couponRedemption.api.coupon;

import com.pk.couponRedemption.api.shared.dto.CustomErrorResponse;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;


import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponController.class)
public class CouponCreationValidationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @MethodSource("provideInvalidRequests")
    void shouldReturn400WithErrorDetailsWhenInvalidCouponCreateRequest(String request, Map<String, String> errorDetails) throws Exception {
        var result = mockMvc.perform(
                        post("/api/coupons/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        String resultBody = result.getResponse().getContentAsString();
        CustomErrorResponse errorResponse = objectMapper.readValue(resultBody, CustomErrorResponse.class);

        assertEquals("Validation failed", errorResponse.message());
        assertThat(errorResponse.details())
                .asInstanceOf(InstanceOfAssertFactories.map(String.class, String.class))
                .isEqualTo(errorDetails);
    }

    private static Stream<Arguments> provideInvalidRequests() {
        return Stream.of(
                Arguments.of(
                        """
                          {
                              "code": "",
                              "countryCode": "PL",
                              "maxUsages": 20
                          }
                          """,
                        Map.of("code", "must not be blank")
                ),
                Arguments.of(
                        """
                          {
                              "code": "SAMPLECODE",
                              "countryCode": "XX",
                              "maxUsages": 20
                          }
                          """,
                        Map.of("countryCode", "Invalid ISO country code")
                ),
                Arguments.of(
                        """
                          {
                              "code": "SAMPLECODE",
                              "countryCode": "PL",
                              "maxUsages": -2
                          }
                          """,
                        Map.of("maxUsages", "must be greater than 0")
                ),
                Arguments.of(
                        """
                          {
                              "code": "",
                              "countryCode": "XX",
                              "maxUsages": -2
                          }
                          """,
                        Map.of("code", "must not be blank", "countryCode", "Invalid ISO country code", "maxUsages", "must be greater than 0")
                )
        );
    }

    @Test
    void shouldReturn201OnSuccess() throws Exception {
        String request = """
                          {
                              "code": "SAMPLECODE",
                              "countryCode": "PL",
                              "maxUsages": 200
                          }
                          """;

        mockMvc.perform(
                        post("/api/coupons/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated());
    }
}
