package com.pk.couponRedemption.service.coupon;

import com.pk.couponRedemption.domain.CouponUsage;
import com.pk.couponRedemption.exception.geolocation.UserCountryCodeParseException;
import com.pk.couponRedemption.mapper.CouponUsageMapper;
import com.pk.couponRedemption.service.geolocation.UserGeolocationStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CouponRedemptionServiceTest {
    @Mock
    private CouponUsageService couponUsageService;

    @Mock
    private UserGeolocationStrategy userGeolocationStrategy;

    @Spy
    private CouponUsageMapper mapper = Mappers.getMapper(CouponUsageMapper.class);

    @InjectMocks
    private CouponRedemptionService couponRedemptionService;

    @Test
    void shouldReturnCouponUsageDtoOnSuccessfulUse() throws UserGeolocationStrategy.GeolocationParseException {
        var sampleCouponUsageMock = Mockito.mock(CouponUsage.class);
        var sampleUuid = UUID.randomUUID();
        var sampleTimestamp = Instant.now();

        when(sampleCouponUsageMock.getId()).thenReturn(sampleUuid);
        when(sampleCouponUsageMock.getUsedAt()).thenReturn(sampleTimestamp);
        when(userGeolocationStrategy.getUserCountryCode(anyString())).thenReturn("PL");

        when(couponUsageService.useCoupon(anyString(), anyString(), anyString())).thenReturn(sampleCouponUsageMock);
        var result = couponRedemptionService.useCoupon("sampleIp", "sampleCouponCode", "sampleUserId");

        assertThat(result.usageId()).isEqualTo(sampleUuid);
        assertThat(result.usedAt()).isEqualTo(sampleTimestamp);
    }

    @Test
    void shouldWrapGeolocationTechnicalExceptionOnCouponUse() throws UserGeolocationStrategy.GeolocationParseException {
        when(userGeolocationStrategy.getUserCountryCode(anyString())).thenThrow(UserGeolocationStrategy.GeolocationParseException.class);
        assertThatThrownBy(() -> couponRedemptionService.useCoupon("sampleIp", "sampleCouponCode", "sampleUserId"))
                .isInstanceOf(UserCountryCodeParseException.class);
    }
}
