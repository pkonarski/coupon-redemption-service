package com.pk.couponRedemption.exception.geolocation;

public class UserCountryCodeParseException extends RuntimeException {
    public UserCountryCodeParseException(String message, Throwable rootCause) {
        super(message, rootCause);
    }
}
