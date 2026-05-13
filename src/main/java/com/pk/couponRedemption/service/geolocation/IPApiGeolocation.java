package com.pk.couponRedemption.service.geolocation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class IPApiGeolocation implements UserGeolocationStrategy {
    private final static String IP_API_BASE_URL = "http://ip-api.com";
    private final RestClient restClient;
    private final AtomicLong rateLimitingResetTimestamp = new AtomicLong(0);

    public IPApiGeolocation(RestClient.Builder builder) {
        this.restClient = builder.baseUrl(IP_API_BASE_URL).build();
    }

    @Override
    public String getUserCountryCode(String ipAddress) throws GeolocationParseException {
        long now = System.currentTimeMillis();
        if (now < rateLimitingResetTimestamp.get()) {
            throw new GeolocationParseException("Local rate-limit active. Try again later.");
        }

        try {
            ResponseEntity<IpCountryResponse> entity = restClient.get()
                    .uri("/json/{ip}?fields=countryCode,status", ipAddress)
                    .retrieve()
                    .toEntity(IpCountryResponse.class);

            updateRateLimit(entity.getHeaders());

            IpCountryResponse body = entity.getBody();
            if (body != null && "success".equals(body.status())) {
                return body.countryCode();
            } else {
                String reason = (body != null) ? body.message() : "Empty response body";
                throw new GeolocationParseException(String.format("Failed to fetch ip address details for address %s. Root cause: %s", ipAddress, reason));
            }

        } catch (HttpClientErrorException.TooManyRequests e) {
            HttpHeaders headers = e.getResponseHeaders();
            if (headers != null) {
                updateRateLimit(e.getResponseHeaders());
            } else {
                log.debug("IP API call for ip address {}. Unable to parse X-RL and X-Ttl as headers are not passed", ipAddress);
            }
            throw new GeolocationParseException("External API rate limit exceeded.");

        } catch (RestClientResponseException e) {
            throw new GeolocationParseException("HTTP error during geolocation. IP API call status code: " + e.getStatusCode());

        } catch (Exception e) {
            throw new GeolocationParseException("Unexpected error: " + e.getMessage());
        }
    }

    private void updateRateLimit(HttpHeaders headers) {
        String rlHeader = headers.getFirst("X-Rl");
        String ttlHeader = headers.getFirst("X-Ttl");

        if (rlHeader != null && ttlHeader != null) {
            int remaining = Integer.parseInt(rlHeader);
            int ttlSeconds = Integer.parseInt(ttlHeader);

            if (remaining <= 0) {
                rateLimitingResetTimestamp.set(System.currentTimeMillis() + (ttlSeconds * 1000L));
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record IpCountryResponse(
            String countryCode,
            String status,
            String message
    ) {}
}