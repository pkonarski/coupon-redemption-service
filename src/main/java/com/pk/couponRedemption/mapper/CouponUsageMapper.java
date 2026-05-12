package com.pk.couponRedemption.mapper;

import com.pk.couponRedemption.api.coupon.dto.CouponUsageResponse;
import com.pk.couponRedemption.domain.CouponUsage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CouponUsageMapper {
    @Mapping(target = "usageId", source = "couponUsage.id")
    CouponUsageResponse map(CouponUsage couponUsage);
}
