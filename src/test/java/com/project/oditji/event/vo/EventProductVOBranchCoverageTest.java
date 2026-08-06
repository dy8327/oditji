package com.project.oditji.event.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** 이벤트 상품의 할인가 계산과 null 분기를 검증합니다. */
class EventProductVOBranchCoverageTest {

    @Test
    void discountPriceShouldHandleMissingValuesAndRounding() {
        EventProductVO product = new EventProductVO();

        assertNull(product.getDiscountPrice());

        product.setPrice(10001L);
        assertNull(product.getDiscountPrice());

        product.setEventDiscountRate(25);
        assertEquals(7501L, product.getDiscountPrice());

        product.setEventDiscountRate(0);
        assertEquals(10001L, product.getDiscountPrice());

        product.setEventDiscountRate(100);
        assertEquals(0L, product.getDiscountPrice());
    }

    @Test
    void imageAndDiscountPropertiesShouldRoundTrip() {
        EventProductVO product = new EventProductVO();
        product.setEventDiscountRate(30);
        product.setImagePath("/images/event-product.png");

        assertEquals(30, product.getEventDiscountRate());
        assertEquals("/images/event-product.png", product.getImagePath());
    }
}
