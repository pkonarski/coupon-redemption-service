package com.pk.couponRedemption.service;

import com.pk.couponRedemption.api.coupon.dto.CouponResponse;
import com.pk.couponRedemption.api.coupon.dto.NewCouponRequest;
import com.pk.couponRedemption.domain.Coupon;
import com.pk.couponRedemption.exception.CouponAlreadyExistsException;
import com.pk.couponRedemption.mapper.CouponMapper;
import com.pk.couponRedemption.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponRegistrationService {
    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    @Transactional
    public CouponResponse register(NewCouponRequest couponRequest) {
        Coupon newCoupon = Coupon.create(couponRequest.code(), couponRequest.maxUsages(), couponRequest.countryCode());
        String couponCode = newCoupon.getCode();
        log.info("Creating new coupon for code {}", couponCode);

        if(couponRepository.existsByCode(couponCode)) {
            throw CouponAlreadyExistsException.whenCouponAlreadyExistsOnPlannedCheckAgainstDB(couponCode);
        }

        try {
            Coupon savedCoupon = couponRepository.saveAndFlush(newCoupon);
            var couponCreationResponse = couponMapper.mapToDto(savedCoupon);
            log.info("Coupon for code: {} created", couponCode);
            return couponCreationResponse;
        } catch (DataIntegrityViolationException e) {
            throw CouponAlreadyExistsException.whenDatabaseWriteConflictedOnExistingCoupon(couponCode);
        }
    }
}
