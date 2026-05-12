package com.pk.couponRedemption.service.coupon;

import com.pk.couponRedemption.api.coupon.dto.CouponUsageResponse;
import com.pk.couponRedemption.exception.geolocation.UserCountryCodeParseException;
import com.pk.couponRedemption.mapper.CouponUsageMapper;
import com.pk.couponRedemption.service.geolocation.UserGeolocationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponRedemptionService {
    private final CouponUsageService couponUsageService;
    private final UserGeolocationStrategy userGeolocationStrategy;
    private final CouponUsageMapper couponUsageMapper;

    public CouponUsageResponse useCoupon(String ipAddress, String cuponCode, String userId) {
        try {
            String countryCode = userGeolocationStrategy.getUserCountryCode(ipAddress);
            var couponUsage = couponUsageService.useCoupon(cuponCode, countryCode, userId);
            return couponUsageMapper.map(couponUsage);
        } catch (UserGeolocationStrategy.GeolocationParseException e) {
            throw new UserCountryCodeParseException("Unable to detect user geolocation", e);
        }
    }
}
