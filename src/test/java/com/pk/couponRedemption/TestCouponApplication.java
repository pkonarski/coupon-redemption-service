package com.pk.couponRedemption;

import org.springframework.boot.SpringApplication;

public class TestCouponApplication {

    public static void main(String[] args) {
        SpringApplication.from(CouponRedemptionApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
