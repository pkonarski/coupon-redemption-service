package com.pk.couponRedemption.service.geolocation;


public interface UserGeolocationStrategy {
    String getUserCountryCode(String ipAddress) throws GeolocationParseException;

    class GeolocationParseException extends Exception {
        public GeolocationParseException(String message) {
            super(message);
        }
    }
}
