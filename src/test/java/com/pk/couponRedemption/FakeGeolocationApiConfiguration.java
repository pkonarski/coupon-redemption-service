package com.pk.couponRedemption;

import com.pk.couponRedemption.service.geolocation.UserGeolocationStrategy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class FakeGeolocationApiConfiguration {
    @Bean
    @Primary
    public UserGeolocationStrategy fakeUserGeolocationApi() {
        return new UserGeolocationStrategy() {
            @Override
            public String getUserCountryCode(String ipAddress) throws GeolocationParseException {
                return "PL";
            }
        };
    }
}
