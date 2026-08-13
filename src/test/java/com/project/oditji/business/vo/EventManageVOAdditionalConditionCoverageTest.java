package com.project.oditji.business.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * 사업자 EventManageVO 할인금액 계산의 OR 단축평가 양쪽을 검증합니다.
 */
class EventManageVOAdditionalConditionCoverageTest {

    @Test
    void discountedPriceShouldReturnNullForMissingPriceOrDiscountRate() {
        EventManageVO event = new EventManageVO();

        event.setPrice(null);
        event.setEventDiscountRate(10);

        assertNull(event.getDiscountedPrice());

        event.setPrice(10000L);
        event.setEventDiscountRate(null);

        assertNull(event.getDiscountedPrice());

        event.setEventDiscountRate(25);

        assertEquals(
                7500L,
                event.getDiscountedPrice());
    }
}
