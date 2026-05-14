package com.pk.couponRedemption.service.coupon;

import com.pk.couponRedemption.domain.Coupon;
import com.pk.couponRedemption.domain.CouponUsage;
import com.pk.couponRedemption.exception.coupon.*;
import com.pk.couponRedemption.repository.CouponRepository;
import com.pk.couponRedemption.repository.CouponUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponUsageService {
    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    @Transactional
    public CouponUsage useCoupon(String code, String countryCode, String userId) {
        String normalizedCode = Coupon.normalizeCode(code);

        log.info("User with id: {} is trying to use coupon with code {}.", userId, code);
        Optional<Coupon> fetchedCoupon = couponRepository.findByCode(normalizedCode);

        if(fetchedCoupon.isEmpty()) throw new CouponNotFoundException(normalizedCode, countryCode);
        if(!fetchedCoupon.get().getCountryCode().equals(countryCode)) throw new CouponReservedForDifferentCountryException(normalizedCode, countryCode);

        CouponUsage couponUsage = createCouponUsage(fetchedCoupon.get(), userId);

        int incremented = couponRepository.incrementCodeUsage(normalizedCode);

        if(incremented == 0) {
            Coupon couponAfterIncrement = couponRepository.findByCode(normalizedCode).orElseThrow();
            throw couponAfterIncrement.isLimitReached() ? new CouponLimitReachedException(normalizedCode) :
                    new IllegalStateException("Coupon usage was not increased. Unknown error occurred. Coupon data: " + couponAfterIncrement);
        }

        return couponUsage;
    }

    private CouponUsage createCouponUsage(Coupon coupon, String userId) {
        try {
            CouponUsage couponUsage = CouponUsage.create(coupon, userId);
            return couponUsageRepository.saveAndFlush(couponUsage);
        } catch (DataIntegrityViolationException e) {
            throw new CouponAlreadyUsedByUserException(coupon.getCode(), userId, e);
        }
    }
}
