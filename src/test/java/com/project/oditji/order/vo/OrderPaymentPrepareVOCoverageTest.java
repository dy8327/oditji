package com.project.oditji.order.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * 결제 준비 VO의 기본값, null 목록 방어와 배송지 상속 필드를 검증합니다.
 */
class OrderPaymentPrepareVOCoverageTest {

    @Test
    void fieldsShouldRoundTripAndItemsShouldNeverRemainNull() {
        OrderPaymentPrepareVO prepare =
                new OrderPaymentPrepareVO();

        assertTrue(
                prepare.getItems().isEmpty());

        prepare.setPaymentId("payment-id");
        prepare.setOrderName("주문명");
        prepare.setTotalAmount(5000L);
        prepare.setStoreId("store");
        prepare.setChannelKey("channel");
        prepare.setReceiverName("홍길동");
        prepare.setReceiverPhone("010-1234-5678");
        prepare.setAddress("서울");

        prepare.setItems(null);
        assertTrue(
                prepare.getItems().isEmpty());

        OrderSheetItemVO item =
                new OrderSheetItemVO();
        List<OrderSheetItemVO> items =
                List.of(item);

        prepare.setItems(items);

        assertEquals(
                "payment-id",
                prepare.getPaymentId());
        assertEquals(
                "주문명",
                prepare.getOrderName());
        assertEquals(
                5000L,
                prepare.getTotalAmount());
        assertEquals(
                "store",
                prepare.getStoreId());
        assertEquals(
                "channel",
                prepare.getChannelKey());
        assertEquals(
                "홍길동",
                prepare.getReceiverName());
        assertEquals(
                "010-1234-5678",
                prepare.getReceiverPhone());
        assertEquals(
                "서울",
                prepare.getAddress());
        assertSame(
                items,
                prepare.getItems());
    }
}
