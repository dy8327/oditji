package com.project.oditji.common.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.project.oditji.order.vo.DeliveryVO;

/**
 * DeliveryBaseVO#getItemTotalPrice()의 두 null 삼항조건을 각각 검증합니다.
 */
class DeliveryBaseVOConditionCoverageTest {

    @Test
    void itemTotalPriceShouldTreatNullPriceAsZero() {
        DeliveryVO delivery = new DeliveryVO();
        delivery.setProductPrice(null);
        delivery.setQuantity(3);

        assertEquals(0L, delivery.getItemTotalPrice());
    }

    @Test
    void itemTotalPriceShouldTreatNullQuantityAsZero() {
        DeliveryVO delivery = new DeliveryVO();
        delivery.setProductPrice(1500);
        delivery.setQuantity(null);

        assertEquals(0L, delivery.getItemTotalPrice());
    }

    @Test
    void itemTotalPriceShouldMultiplyBothValuesWhenPresent() {
        DeliveryVO delivery = new DeliveryVO();
        delivery.setProductPrice(1500);
        delivery.setQuantity(3);

        assertEquals(4500L, delivery.getItemTotalPrice());
    }
}
