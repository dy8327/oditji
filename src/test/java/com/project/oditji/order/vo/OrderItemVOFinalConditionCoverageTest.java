package com.project.oditji.order.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** 주문상품 VO의 남은 short-circuit와 null 가격/수량 분기를 보완합니다. */
class OrderItemVOFinalConditionCoverageTest {

    @Test
    void eligibilityShouldCoverBothSidesOfEachAndCondition() {
        OrderItemVO item = new OrderItemVO();

        item.setStatus("ORDERED");
        item.setDeliveryStatus("CONFIRMED");
        assertFalse(item.isCancelEligible());

        item.setStatus("PAID");
        item.setDeliveryStatus("PREPARING");
        assertFalse(item.isCancelEligible());

        item.setDeliveryStatus("CONFIRMED");
        assertTrue(item.isCancelEligible());

        item.setStatus("PAID");
        item.setDeliveryStatus("DELIVERED");
        assertFalse(item.isRefundEligible());

        item.setStatus("DELIVERED");
        item.setDeliveryStatus("SHIPPING");
        assertFalse(item.isRefundEligible());

        item.setDeliveryStatus("DELIVERED");
        assertTrue(item.isRefundEligible());
    }

    @Test
    void totalPriceShouldCoverNullProductPriceAndNullQuantity() {
        OrderItemVO item = new OrderItemVO();

        item.setProductPrice(null);
        item.setQuantity(3);
        assertEquals(0L, item.getItemTotalPrice());

        item.setProductPrice(1500);
        item.setQuantity(null);
        assertEquals(0L, item.getItemTotalPrice());

        item.setQuantity(2);
        assertEquals(3000L, item.getItemTotalPrice());
    }
}
