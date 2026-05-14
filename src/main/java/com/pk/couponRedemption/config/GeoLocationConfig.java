package com.pk.couponRedemption.config;

import com.pk.couponRedemption.service.geolocation.IPApiGeolocationParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeoLocationConfig {

    @Bean
    public IPApiGeolocationParser ipApiGeolocation() {
        return new IPApiGeolocationParser();
    }
}
