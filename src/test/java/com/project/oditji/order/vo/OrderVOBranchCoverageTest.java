package com.project.oditji.order.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/** 주문 VO의 전체·일부 취소 및 환불 가능 여부 분기를 검증합니다. */
class OrderVOBranchCoverageTest {

    @Test
    void emptyAndNullItemsShouldBeSafeAndIneligible() {
        OrderVO order = new OrderVO();
        assertFalse(order.isAllCancelEligible());
        assertFalse(order.isAllRefundEligible());
        assertFalse(order.isAnyCancelEligible());
        assertFalse(order.isAnyRefundEligible());

        order.setItems(null);
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void cancelAndRefundEligibilityShouldAggregateAcrossItems() {
        OrderItemVO cancel = item("PAID", "CONFIRMED");
        OrderItemVO refund = item("DELIVERED", "DELIVERED");
        OrderItemVO blocked = item("PAID", "SHIPPING");

        OrderVO allCancel = new OrderVO();
        allCancel.setItems(List.of(cancel, item("PAID", "CONFIRMED")));
        assertTrue(allCancel.isAllCancelEligible());
        assertTrue(allCancel.isAnyCancelEligible());
        assertFalse(allCancel.isAnyRefundEligible());

        OrderVO allRefund = new OrderVO();
        allRefund.setItems(List.of(refund, item("DELIVERED", "DELIVERED")));
        assertTrue(allRefund.isAllRefundEligible());
        assertTrue(allRefund.isAnyRefundEligible());
        assertFalse(allRefund.isAnyCancelEligible());

        OrderVO mixed = new OrderVO();
        mixed.setItems(List.of(cancel, refund, blocked));
        assertFalse(mixed.isAllCancelEligible());
        assertFalse(mixed.isAllRefundEligible());
        assertTrue(mixed.isAnyCancelEligible());
        assertTrue(mixed.isAnyRefundEligible());
    }

    @Test
    void orderAndItemAccessorsAndTotalPriceShouldHandleNulls() {
        OrderVO order = new OrderVO();
        order.setMemberNo(1L);
        order.setPayMethod("CARD");
        order.setPgProvider("TOSS");
        order.setFullCancelStatus("WAITING");
        order.setFullCancelRejectReason("사유");
        assertEquals(1L, order.getMemberNo());
        assertEquals("CARD", order.getPayMethod());
        assertEquals("TOSS", order.getPgProvider());
        assertEquals("WAITING", order.getFullCancelStatus());
        assertEquals("사유", order.getFullCancelRejectReason());

        OrderItemVO item = new OrderItemVO();
        assertEquals(0L, item.getItemTotalPrice());
        item.setProductPrice(1000);
        item.setQuantity(3);
        assertEquals(3000L, item.getItemTotalPrice());
    }

    private OrderItemVO item(String status, String deliveryStatus) {
        OrderItemVO item = new OrderItemVO();
        item.setStatus(status);
        item.setDeliveryStatus(deliveryStatus);
        return item;
    }
}
