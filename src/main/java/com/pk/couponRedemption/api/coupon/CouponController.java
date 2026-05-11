package com.pk.couponRedemption.api.coupon;

import com.pk.couponRedemption.api.coupon.dto.NewCouponRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public void createCoupon(@Valid @RequestBody NewCouponRequest request) {

    }
}
