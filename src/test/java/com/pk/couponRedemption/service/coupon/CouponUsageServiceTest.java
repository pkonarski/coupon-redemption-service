package com.pk.couponRedemption.service.coupon;

import com.pk.couponRedemption.domain.Coupon;
import com.pk.couponRedemption.domain.CouponUsage;
import com.pk.couponRedemption.exception.coupon.CouponAlreadyUsedByUserException;
import com.pk.couponRedemption.exception.coupon.CouponLimitReachedException;
import com.pk.couponRedemption.exception.coupon.CouponNotFoundException;
import com.pk.couponRedemption.exception.coupon.CouponReservedForDifferentCountryException;
import com.pk.couponRedemption.mapper.CouponMapper;
import com.pk.couponRedemption.repository.CouponRepository;
import com.pk.couponRedemption.repository.CouponUsageRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CouponUsageServiceTest {
    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @InjectMocks
    private CouponUsageService couponUsageService;

    @Test
    void shouldUseCouponWhenAvailable() {
        String sampleUserId = "sampleUserId";
        String sampleCountryCode = "sampleCountryCode";
        String sampleCouponCode = "sampleCode";
        Coupon sampleCoupon = Coupon.create(sampleCouponCode, 3, sampleCountryCode);

        when(couponRepository.findByCode(anyString())).thenReturn(Optional.of(sampleCoupon));
        when(couponRepository.incrementCodeUsage(anyString())).thenReturn(1);
        when(couponUsageRepository.saveAndFlush(any())).thenReturn(CouponUsage.create(sampleCoupon, sampleUserId));

        var couponUsage = couponUsageService.useCoupon(sampleCouponCode, sampleCountryCode, sampleUserId);

        assertThat(couponUsage.getUserId()).isEqualTo(sampleUserId);
        assertThat(couponUsage.getCoupon().getCode()).isEqualTo(sampleCouponCode.toUpperCase(Locale.ROOT));
    }

    @Test
    void shouldThrowExceptionWhenCouponNotExist() {
        when(couponRepository.findByCode(anyString())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> couponUsageService.useCoupon("sampleCode", "sampleCountryCode", "sampleUserId"))
                .isInstanceOf(CouponNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenCouponDedicatedForDifferentCountryCode() {
        Coupon sampleCoupon = Coupon.create("sampleCode", 10, "PL");

        when(couponRepository.findByCode(anyString())).thenReturn(Optional.of(sampleCoupon));
        assertThatThrownBy(() -> couponUsageService.useCoupon(sampleCoupon.getCode(), "NL", "sampleUserId"))
                .isInstanceOf(CouponReservedForDifferentCountryException.class);
    }

    @Test
    void shouldThrowExceptionWhenCouponAlreadyUsedByUser() {
        Coupon sampleCoupon = Coupon.create("sampleCode", 10, "PL");

        when(couponUsageRepository.saveAndFlush(any())).thenThrow(DataIntegrityViolationException.class);
        when(couponRepository.findByCode(anyString())).thenReturn(Optional.of(sampleCoupon));

        assertThatThrownBy(() -> couponUsageService.useCoupon(sampleCoupon.getCode(), sampleCoupon.getCountryCode(), "sampleUserId"))
                .isInstanceOf(CouponAlreadyUsedByUserException.class);
    }

    @Test
    void shouldThrowExceptionWhenCouponLimitReached() {
        Coupon sampleCoupon = Coupon.create("sampleCode", 10, "PL");
        Coupon existingCouponMock = Mockito.mock(Coupon.class);

        when(couponRepository.findByCode(anyString()))
                .thenReturn(Optional.of(sampleCoupon))
                .thenReturn(Optional.ofNullable(existingCouponMock));
        when(couponRepository.incrementCodeUsage(anyString())).thenReturn(0);
        assert existingCouponMock != null;
        when(existingCouponMock.isLimitReached()).thenReturn(true);

        assertThatThrownBy(() -> couponUsageService.useCoupon(sampleCoupon.getCode(), sampleCoupon.getCountryCode(), "sampleUserId"))
                .isInstanceOf(CouponLimitReachedException.class);
    }
}
