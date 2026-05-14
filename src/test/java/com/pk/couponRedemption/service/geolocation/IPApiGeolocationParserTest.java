package com.pk.couponRedemption.service.geolocation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringJUnitConfig
public class IPApiGeolocationParserTest {
    private final static String SAMPLE_IP_ADDRESS = "1.1.1.1";

    private IPApiGeolocationParser ipApiGeolocationParser;

    private MockRestServiceServer server;

    @BeforeEach
    void setup() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ip-api.com");
        server = MockRestServiceServer.bindTo(builder).build();

        ipApiGeolocationParser = new IPApiGeolocationParser(builder);
    }

    @Test
    void shouldReturnCountryCodeOnSuccessRequest() throws UserGeolocationStrategy.GeolocationParseException {
        server.expect(requestTo(containsString("/json/" + SAMPLE_IP_ADDRESS)))
                .andRespond(withSuccess("""
            {"status":"success","countryCode":"PL"}
            """, MediaType.APPLICATION_JSON));

        var countryCode = ipApiGeolocationParser.getUserCountryCode(SAMPLE_IP_ADDRESS);
        assertEquals("PL", countryCode);
    }

    @Test
    void shouldWrapHttpClientException() throws UserGeolocationStrategy.GeolocationParseException {
        server.expect(requestTo(containsString("/json/" + SAMPLE_IP_ADDRESS)))
                .andRespond(withServerError());
        assertThatThrownBy(() -> ipApiGeolocationParser.getUserCountryCode(SAMPLE_IP_ADDRESS))
                .isInstanceOf(UserGeolocationStrategy.GeolocationParseException.class)
                .hasMessage("HTTP error during geolocation. IP API call status code: 500 INTERNAL_SERVER_ERROR");
    }

    @Test
    void shouldWrapUnknownException() {
        String exceptionMessage = "Sample failure";
        server.expect(requestTo(containsString("/json/" + SAMPLE_IP_ADDRESS)))
                .andRespond(withException(new IOException(exceptionMessage)));
        assertThatThrownBy(() -> ipApiGeolocationParser.getUserCountryCode(SAMPLE_IP_ADDRESS))
                .isInstanceOf(UserGeolocationStrategy.GeolocationParseException.class)
                .hasMessage("Unexpected error: I/O error on GET request for \"http://ip-api.com/json/1.1.1.1\": " + exceptionMessage);
    }

    @Test
    void shouldThrowExceptionWhenIpApiResponseBodyIsEmpty() {
        server.expect(requestTo(containsString("/json/" + SAMPLE_IP_ADDRESS)))
                .andRespond(withSuccess());

        assertThatThrownBy(() -> ipApiGeolocationParser.getUserCountryCode(SAMPLE_IP_ADDRESS))
                .isInstanceOf(UserGeolocationStrategy.GeolocationParseException.class)
                .hasMessage("Unexpected error: Failed to fetch ip address details for address 1.1.1.1. Root cause: Empty response body");
    }

    @Test
    void shouldThrowExceptionWhenIpApiRespondWithFailWithMessage() {
        server.expect(requestTo(containsString("/json/" + SAMPLE_IP_ADDRESS)))
                .andRespond(withSuccess("""
                    {"status":"fail","countryCode":"", "message": "IP Address not exist"}
                """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> ipApiGeolocationParser.getUserCountryCode(SAMPLE_IP_ADDRESS))
                .isInstanceOf(UserGeolocationStrategy.GeolocationParseException.class)
                .hasMessage("Unexpected error: Failed to fetch ip address details for address 1.1.1.1. Root cause: IP Address not exist");
    }

    @Test
    void shouldBlockApiRequestOnKnownLimitResetTimestamp() throws UserGeolocationStrategy.GeolocationParseException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Rl", "0");
        headers.set("X-Ttl", "60");
        server.expect(ExpectedCount.once(), requestTo(containsString("/json/" + SAMPLE_IP_ADDRESS)))
                .andRespond(withTooManyRequests().headers(headers));

        assertThatThrownBy(() -> ipApiGeolocationParser.getUserCountryCode(SAMPLE_IP_ADDRESS))
                .isInstanceOf(UserGeolocationStrategy.GeolocationParseException.class)
                .hasMessage("External API rate limit exceeded.");
        assertThatThrownBy(() -> ipApiGeolocationParser.getUserCountryCode(SAMPLE_IP_ADDRESS))
                .isInstanceOf(UserGeolocationStrategy.GeolocationParseException.class)
                .hasMessage("Local rate-limit active. Try again later.");

        server.verify();
    }

    @Test
    void shouldSucceedOnSecondCallAfterInMemoryLockFree() throws InterruptedException, UserGeolocationStrategy.GeolocationParseException {
        int secondsToWait = 1;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Rl", "0");
        headers.set("X-Ttl", String.valueOf(secondsToWait));

        server.expect(ExpectedCount.once(), requestTo(containsString("/json/" + SAMPLE_IP_ADDRESS)))
                .andRespond(withTooManyRequests().headers(headers));
        server.expect(ExpectedCount.once(), requestTo(containsString("/json/" + SAMPLE_IP_ADDRESS)))
                .andRespond(withSuccess("""
            {"status":"success","countryCode":"PL"}
            """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> ipApiGeolocationParser.getUserCountryCode(SAMPLE_IP_ADDRESS))
                .isInstanceOf(UserGeolocationStrategy.GeolocationParseException.class)
                .hasMessage("External API rate limit exceeded.");

        TimeUnit.SECONDS.sleep(secondsToWait);

        var fetchedCountryCode = ipApiGeolocationParser.getUserCountryCode(SAMPLE_IP_ADDRESS);

        server.verify();
        assertEquals("PL", fetchedCountryCode);
    }
}
