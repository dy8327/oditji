package com.project.oditji.business.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * EventManageVO의 나머지 필드와 toString 실행 라인을 보완합니다.
 */
class EventManageVOFinalCoverageTest {

    @Test
    void remainingPropertiesAndToStringShouldRoundTrip() {
        EventManageVO event = new EventManageVO();

        EventProductVO connected =
                new EventProductVO();

        event.setEventProdNo(10L);
        event.setProductNoList(List.of(1L, 2L));
        event.setDiscountRateList(List.of(10, 20));
        event.setProductName("상품");
        event.setBusinessNo(30L);
        event.setConnectedProducts(List.of(connected));

        assertEquals(10L, event.getEventProdNo());
        assertEquals(List.of(1L, 2L), event.getProductNoList());
        assertEquals(List.of(10, 20), event.getDiscountRateList());
        assertEquals("상품", event.getProductName());
        assertEquals(30L, event.getBusinessNo());
        assertEquals(List.of(connected), event.getConnectedProducts());

        String value = event.toString();

        assertTrue(value.contains("EventManageVO"));
        assertTrue(value.contains("businessNo=30"));
    }

    @Test
    void zeroPercentDiscountShouldReturnOriginalPrice() {
        EventManageVO event = new EventManageVO();
        event.setPrice(10000L);
        event.setEventDiscountRate(0);

        assertEquals(
                10000L,
                event.getDiscountedPrice());
    }
}
