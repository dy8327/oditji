package com.project.oditji.common.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** 상품 판매 공통 VO의 계산 및 판매 가능 조건을 경계값별로 검증합니다. */
class ProductSaleInfoVOResidualCoverageTest {

    @Test
    void discountPriceShouldClampNullNegativeAndOverHundredRates() {
        TestSaleInfo info = new TestSaleInfo();
        assertEquals(0, info.getDiscountPrice());

        info.setPrice(10000);
        info.setDiscountRate(null);
        assertEquals(10000, info.getDiscountPrice());

        info.setDiscountRate(-10);
        assertEquals(10000, info.getDiscountPrice());

        info.setDiscountRate(150);
        assertEquals(0, info.getDiscountPrice());

        info.setDiscountRate(25);
        assertEquals(7500, info.getDiscountPrice());
    }

    @Test
    void totalPriceShouldCoverNullAndPositiveQuantity() {
        TestSaleInfo info = approved(10000, 10, 5, null);
        assertEquals(0L, info.getItemTotalPrice());

        info.setQuantity(2);
        assertEquals(18000L, info.getItemTotalPrice());
    }

    @Test
    void availabilityShouldCoverEveryShortCircuitAndValidBoundary() {
        TestSaleInfo info = approved(1000, 0, 5, 1);
        info.setStatus("WAITING");
        assertFalse(info.isAvailable());

        info.setStatus("APPROVED");
        info.setStock(null);
        assertFalse(info.isAvailable());

        info.setStock(0);
        assertFalse(info.isAvailable());

        info.setStock(5);
        info.setQuantity(null);
        assertFalse(info.isAvailable());

        info.setQuantity(0);
        assertFalse(info.isAvailable());

        info.setQuantity(6);
        assertFalse(info.isAvailable());

        info.setQuantity(5);
        assertTrue(info.isAvailable());
    }

    @Test
    void soldOutShouldCoverNullZeroNegativeAndPositiveStock() {
        TestSaleInfo info = new TestSaleInfo();
        assertTrue(info.isSoldOut());

        info.setStock(0);
        assertTrue(info.isSoldOut());

        info.setStock(-1);
        assertTrue(info.isSoldOut());

        info.setStock(1);
        assertFalse(info.isSoldOut());
    }

    private TestSaleInfo approved(Integer price, Integer rate, Integer stock, Integer quantity) {
        TestSaleInfo info = new TestSaleInfo();
        info.setPrice(price);
        info.setDiscountRate(rate);
        info.setStock(stock);
        info.setQuantity(quantity);
        info.setStatus("APPROVED");
        return info;
    }

    private static final class TestSaleInfo extends ProductSaleInfoVO {
        private static final long serialVersionUID = 1L;
    }
}
