package com.pk.couponRedemption.mapper;

import com.pk.couponRedemption.api.coupon.dto.CouponResponse;
import com.pk.couponRedemption.domain.Coupon;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CouponMapper {
    CouponResponse mapToDto(Coupon coupon);
}
