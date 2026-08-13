package com.project.oditji.cart.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 장바구니 상품의 할인 가격, 합계, 판매 가능 상태 계산을 검증합니다.
 */
class CartItemVOTest {

    @Test
    void getDiscountPriceShouldApplyNormalDiscountRate() {

        CartItemVO item = new CartItemVO();
        item.setPrice(20_000);
        item.setDiscountRate(15);

        assertEquals(17_000, item.getDiscountPrice());
    }

    @Test
    void getDiscountPriceShouldNormalizeOutOfRangeRate() {

        CartItemVO negativeRateItem = new CartItemVO();
        negativeRateItem.setPrice(20_000);
        negativeRateItem.setDiscountRate(-10);

        CartItemVO excessiveRateItem = new CartItemVO();
        excessiveRateItem.setPrice(20_000);
        excessiveRateItem.setDiscountRate(150);

        assertEquals(20_000, negativeRateItem.getDiscountPrice());
        assertEquals(0, excessiveRateItem.getDiscountPrice());
    }

    @Test
    void getItemTotalPriceShouldMultiplyDiscountPriceAndQuantity() {

        CartItemVO item = new CartItemVO();
        item.setPrice(10_000);
        item.setDiscountRate(20);
        item.setQuantity(3);

        assertEquals(24_000L, item.getItemTotalPrice());
    }

    @Test
    void isAvailableShouldReturnTrueOnlyForApprovedProductWithinStock() {

        CartItemVO availableItem = createItem("APPROVED", 5, 2);
        CartItemVO overStockItem = createItem("APPROVED", 1, 2);
        CartItemVO waitingItem = createItem("WAITING", 5, 2);

        assertTrue(availableItem.isAvailable());
        assertFalse(overStockItem.isAvailable());
        assertFalse(waitingItem.isAvailable());
    }

    @Test
    void isSoldOutShouldTreatNullOrZeroStockAsSoldOut() {

        CartItemVO nullStockItem = new CartItemVO();
        CartItemVO zeroStockItem = new CartItemVO();
        zeroStockItem.setStock(0);
        CartItemVO stockedItem = new CartItemVO();
        stockedItem.setStock(1);

        assertTrue(nullStockItem.isSoldOut());
        assertTrue(zeroStockItem.isSoldOut());
        assertFalse(stockedItem.isSoldOut());
    }

    private CartItemVO createItem(String status, int stock, int quantity) {

        CartItemVO item = new CartItemVO();
        item.setStatus(status);
        item.setStock(stock);
        item.setQuantity(quantity);
        return item;
    }
}
