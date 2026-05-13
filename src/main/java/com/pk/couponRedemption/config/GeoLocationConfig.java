package com.pk.couponRedemption.config;

import com.pk.couponRedemption.service.geolocation.IPApiGeolocation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeoLocationConfig {

    @Bean
    public IPApiGeolocation ipApiGeolocation() {
        return new IPApiGeolocation();
    }
}
