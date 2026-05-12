package com.pk.couponRedemption.api.coupon;

import com.pk.couponRedemption.api.coupon.dto.CouponResponse;
import com.pk.couponRedemption.api.coupon.dto.NewCouponRequest;
import com.pk.couponRedemption.service.CouponRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponRegistrationService couponRegistrationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse createCoupon(@Valid @RequestBody NewCouponRequest request) {
        return couponRegistrationService.register(request);
    }
}
