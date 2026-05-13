package com.pk.couponRedemption.service.coupon;

import com.pk.couponRedemption.FakeGeolocationApiConfiguration;
import com.pk.couponRedemption.TestcontainersConfiguration;
import com.pk.couponRedemption.domain.Coupon;
import com.pk.couponRedemption.exception.coupon.CouponAlreadyUsedByUserException;
import com.pk.couponRedemption.exception.coupon.CouponLimitReachedException;
import com.pk.couponRedemption.repository.CouponRepository;
import com.pk.couponRedemption.repository.CouponUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import({FakeGeolocationApiConfiguration.class, TestcontainersConfiguration.class})
public class CouponRedemptionServiceIntegrationTest {
    @Autowired
    private CouponRedemptionService couponRedemptionService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUsageRepository couponUsageRepository;

    @BeforeEach
    void setup() {
        couponUsageRepository.deleteAll();
        couponRepository.deleteAll();
    }

    @Test
    void shouldNotExceedCouponUsagesCountForConcurrentRedemptions() {
        int concurrentUsagesCount = 5;
        int maxUsages = concurrentUsagesCount / 2;

        String sampleCouponCode = Coupon.normalizeCode("code");
        String couponCountryCode = "PL";

        Coupon coupon = Coupon.create(sampleCouponCode, maxUsages, couponCountryCode);
        couponRepository.saveAndFlush(coupon);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Future<?>> futureResults = new ArrayList<>();

        CyclicBarrier barrier = new CyclicBarrier(concurrentUsagesCount + 1);

        try(var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for(int i = 0; i < concurrentUsagesCount; i++) {
                String userId = "user-" + i;
                var result = executor.submit(() -> {
                    try {
                        barrier.await();
                        couponRedemptionService.useCoupon("0.0.0.0", sampleCouponCode, userId);
                        successCount.incrementAndGet();
                    } catch(CouponLimitReachedException e) {
                        failureCount.incrementAndGet();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    }
                });

                futureResults.add(result);
            }


            barrier.await();
            for (Future<?> futureResult : futureResults) {
                futureResult.get();
            }

            assertThat(successCount.get()).isEqualTo(maxUsages);
            assertThat(failureCount.get()).isEqualTo(concurrentUsagesCount - maxUsages);

            var registeredUsages = couponUsageRepository.findAll();
            assertThat(registeredUsages.size()).isEqualTo(maxUsages);

            var couponAfterUse = couponRepository.findByCode(Coupon.normalizeCode(sampleCouponCode));
            assert couponAfterUse.isPresent();
            assertThat(couponAfterUse.get().isLimitReached()).isEqualTo(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldNotAllowUsingCouponTwiceByUser() {
        String sampleCouponCode = "code";
        String couponCountryCode = "PL";
        String sampleUserId = "userId";

        Coupon coupon = Coupon.create(sampleCouponCode, 2, couponCountryCode);
        couponRepository.saveAndFlush(coupon);

        couponRedemptionService.useCoupon("0.0.0.0", sampleCouponCode, sampleUserId);

        var couponAfterFirstUse = couponRepository.findByCode(coupon.getCode());

        assert couponAfterFirstUse.isPresent();
        assertThat(couponAfterFirstUse.get().getCurrentUsages()).isEqualTo(1);

        assertThatThrownBy(() -> couponRedemptionService.useCoupon("0.0.0.0", sampleCouponCode, sampleUserId))
                .isInstanceOf(CouponAlreadyUsedByUserException.class);
    }
}
