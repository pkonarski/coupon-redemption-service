package com.pk.couponRedemption.service;

import com.pk.couponRedemption.api.coupon.dto.NewCouponRequest;
import com.pk.couponRedemption.domain.Coupon;
import com.pk.couponRedemption.exception.coupon.CouponAlreadyExistsException;
import com.pk.couponRedemption.mapper.CouponMapper;
import com.pk.couponRedemption.repository.CouponRepository;
import com.pk.couponRedemption.service.coupon.CouponRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CouponRegistrationServiceTest {
    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponMapper couponMapper;

    private CouponRegistrationService couponRegistrationService;

    @BeforeEach
    void setup() {
        couponRegistrationService = new CouponRegistrationService(couponRepository, couponMapper);
    }

    @Test
    void shouldCreateNewCouponWhenNotExist() {
        var request = new NewCouponRequest("SAMPLECODE", 20, "PL");
        Mockito.when(couponRepository.existsByCode(Mockito.anyString())).thenReturn(false);
        couponRegistrationService.register(request);

        ArgumentCaptor<Coupon> couponArgumentCaptor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository).saveAndFlush(couponArgumentCaptor.capture());
        assertThat(couponArgumentCaptor.getValue())
                .extracting(Coupon::getCode, Coupon::getMaxUsages, Coupon::getCountryCode)
                .containsExactly(request.code(), request.maxUsages(), request.countryCode());
    }

    @Test
    void shouldCreateNewCouponWithNormalizedCode() {
        var request = new NewCouponRequest("notnormalized", 1, "PL");
        Mockito.when(couponRepository.existsByCode(Mockito.anyString())).thenReturn(false);
        couponRegistrationService.register(request);

        ArgumentCaptor<Coupon> couponArgumentCaptor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository).saveAndFlush(couponArgumentCaptor.capture());
        assertThat(couponArgumentCaptor.getValue().getCode()).isEqualTo(request.code().toUpperCase(Locale.ROOT));
    }

    @Test
    void shouldThrowErrorOnCouponCreateWithExactName() {
        var request = new NewCouponRequest("EXISTING", 1, "PL");
        Mockito.when(couponRepository.existsByCode(Mockito.anyString())).thenReturn(true);
        assertThatThrownBy(() -> couponRegistrationService.register(request)).isInstanceOf(CouponAlreadyExistsException.class);
        verify(couponRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldWrapDatabaseCodeConstraintViolationWithBusinessException() {
        var request = new NewCouponRequest("EXISTING", 1, "PL");
        Mockito.when(couponRepository.existsByCode(Mockito.anyString())).thenReturn(false);
        when(couponRepository.saveAndFlush(any())).thenThrow(CouponAlreadyExistsException.class);
        assertThatThrownBy(() -> couponRegistrationService.register(request)).isInstanceOf(CouponAlreadyExistsException.class);
    }
}
