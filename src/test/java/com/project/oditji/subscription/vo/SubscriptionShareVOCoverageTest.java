package com.project.oditji.subscription.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.Month;

import org.junit.jupiter.api.Test;

/** 구독 계산 결과 공유 VO의 모든 접근자를 검증합니다. */
class SubscriptionShareVOCoverageTest {

    @Test
    void settersAndGettersShouldRoundTripEveryField() {
        SubscriptionShareVO share = new SubscriptionShareVO();
        LocalDateTime expiresAt = LocalDateTime.of(2026, Month.SEPTEMBER, 12, 10, 30);
        LocalDateTime createdAt = LocalDateTime.of(2026, Month.AUGUST, 13, 9, 30);

        share.setResultId("SUBS_TEST");
        share.setMemberNo(7L);
        share.setTotalPrice(30000);
        share.setDiscountPrice(5000);
        share.setFinalPrice(25000);
        share.setSelectedServicesJson("{\"selectedPlatformList\":[]}");
        share.setExpiresAt(expiresAt);
        share.setCreatedAt(createdAt);

        assertEquals("SUBS_TEST", share.getResultId());
        assertEquals(Long.valueOf(7L), share.getMemberNo());
        assertEquals(30000, share.getTotalPrice());
        assertEquals(5000, share.getDiscountPrice());
        assertEquals(25000, share.getFinalPrice());
        assertEquals("{\"selectedPlatformList\":[]}", share.getSelectedServicesJson());
        assertEquals(expiresAt, share.getExpiresAt());
        assertEquals(createdAt, share.getCreatedAt());
    }
}
