package com.pk.couponRedemption.api.coupon;

import com.pk.couponRedemption.api.coupon.dto.CouponResponse;
import com.pk.couponRedemption.api.coupon.dto.CouponUsageResponse;
import com.pk.couponRedemption.api.coupon.dto.CouponUseRequest;
import com.pk.couponRedemption.api.coupon.dto.NewCouponRequest;
import com.pk.couponRedemption.service.coupon.CouponRedemptionService;
import com.pk.couponRedemption.service.coupon.CouponRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponRegistrationService couponRegistrationService;
    private final CouponRedemptionService couponRedemptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse createCoupon(@Valid @RequestBody NewCouponRequest request) {
        return couponRegistrationService.register(request);
    }

    @PostMapping("/{code}/redemption")
    @ResponseStatus(HttpStatus.OK)
    public CouponUsageResponse useCoupon(@PathVariable String code, @Valid @RequestBody CouponUseRequest request, HttpServletRequest httpServletRequest) {
        return couponRedemptionService.useCoupon(httpServletRequest.getRemoteAddr(), code, request.userId());
    }
}
