package com.project.oditji.order.vo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** DB에서 계산된 환불 가능 여부가 존재하는 명시적 분기를 보완합니다. */
class OrderItemVOExplicitRefundCoverageTest {

    @Test
    void refundEligibilityShouldPreferExplicitDatabaseValue() {
        OrderItemVO item = new OrderItemVO();
        item.setStatus("ORDERED");
        item.setDeliveryStatus("PREPARING");

        item.setRefundEligible(Boolean.TRUE);
        assertTrue(item.isRefundEligible());

        item.setStatus("DELIVERED");
        item.setDeliveryStatus("DELIVERED");
        item.setRefundEligible(Boolean.FALSE);
        assertFalse(item.isRefundEligible());
    }
}
