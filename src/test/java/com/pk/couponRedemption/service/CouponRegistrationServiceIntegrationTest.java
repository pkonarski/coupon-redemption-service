package com.pk.couponRedemption.service;

import com.pk.couponRedemption.api.coupon.dto.NewCouponRequest;
import com.pk.couponRedemption.exception.CouponAlreadyExistsException;
import com.pk.couponRedemption.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.pk.couponRedemption.TestcontainersConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class CouponRegistrationServiceIntegrationTest {

    @Autowired
    private CouponRegistrationService couponRegistrationService;

    @Autowired
    private CouponRepository couponRepository;

    @BeforeEach
    void setup() {
        couponRepository.deleteAll();
    }

    @Test
    void shouldAllowOnlySingleCouponCreationOnConcurrentWrite() {
        int concurrentWrites = 4;
        CyclicBarrier barrier = new CyclicBarrier(concurrentWrites + 1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Future<?>> futureResults = new ArrayList<>();

        var request = new NewCouponRequest("SAMPLE", 20, "PL");

        try(var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for(int i = 0; i<concurrentWrites; i++) {
                var result = executor.submit(() -> {
                    try {
                        barrier.await();
                        couponRegistrationService.register(request);
                        successCount.incrementAndGet();
                    } catch(CouponAlreadyExistsException e) {
                        failureCount.incrementAndGet();
                    } catch (InterruptedException | BrokenBarrierException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                futureResults.add(result);
            }

            barrier.await();

            for (Future<?> futureResult : futureResults) {
                futureResult.get();
            }
            assertThat(successCount.get()).isEqualTo(1);
            assertThat(failureCount.get()).isEqualTo(concurrentWrites - 1);

            assertThat(couponRepository.findAll().size()).isEqualTo(1);

        } catch (InterruptedException | BrokenBarrierException | ExecutionException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void shouldThrowErrorOnUsingCouponCodeCaseInsensitive() {
        var couponRequest = new NewCouponRequest("SUMMER", 1, "GB");
        var couponRequestWithLowerCase = new NewCouponRequest(couponRequest.code().toLowerCase(Locale.ROOT), 2, "GB");

        var savedCoupon = couponRegistrationService.register(couponRequest);
        assertThat(savedCoupon.id()).isNotNull();

        assertThatThrownBy(() -> couponRegistrationService.register(couponRequestWithLowerCase)).isInstanceOf(CouponAlreadyExistsException.class);
    }
}
