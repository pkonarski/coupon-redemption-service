package com.pk.couponRedemption.service.coupon;

import com.pk.couponRedemption.domain.Coupon;
import com.pk.couponRedemption.domain.CouponUsage;
import com.pk.couponRedemption.exception.coupon.CouponNotFoundException;
import com.pk.couponRedemption.exception.coupon.CouponUsageException;
import com.pk.couponRedemption.repository.CouponRepository;
import com.pk.couponRedemption.repository.CouponUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponUsageService {
    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    @Transactional
    public CouponUsage useCoupon(String code, String countryCode, String userId) {
        Optional<Coupon> fetchedCoupon = couponRepository.findByCodeAndCountryCode(code, countryCode);

        if(fetchedCoupon.isEmpty()) throw new CouponNotFoundException(code, countryCode);
        CouponUsage couponUsage = createCouponUsage(fetchedCoupon.get(), userId);

        int incremented = couponRepository.incrementCodeUsage(code);

        if(incremented == 0) {
            Coupon couponAfterIncrement = couponRepository.findByCodeAndCountryCode(code, countryCode).orElseThrow();
            throw couponAfterIncrement.isLimitReached() ? CouponUsageException.whenLimitReached(couponAfterIncrement.getCode()) :
                    new RuntimeException("Coupon usage was not increased. Unknown error occurred. Coupon data: " + couponAfterIncrement);
        }

        return couponUsage;
    }

    private CouponUsage createCouponUsage(Coupon coupon, String userId) {
        try {
            CouponUsage couponUsage = CouponUsage.create(coupon, userId);
            return couponUsageRepository.saveAndFlush(couponUsage);
        } catch (DataIntegrityViolationException e) {
            throw CouponUsageException.whenCouponUsedForUser(coupon.getCode(), userId, e);
        }
    }
}
