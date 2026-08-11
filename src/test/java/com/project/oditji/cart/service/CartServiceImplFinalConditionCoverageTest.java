package com.project.oditji.cart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.cart.dao.CartDAO;
import com.project.oditji.cart.vo.CartItemVO;
import com.project.oditji.cart.vo.CartVO;

/**
 * 장바구니 서비스의 남은 null/단락 조건과 생성 실패 경계를 검증합니다.
 */
class CartServiceImplFinalConditionCoverageTest {

    private CartDAO cartDAO;
    private CartServiceImpl service;

    @BeforeEach
    void setUp() {
        cartDAO = mock(CartDAO.class);
        service = new CartServiceImpl(cartDAO);
    }

    @Test
    void nullIdentifiersShouldReachFirstOperandBranches() {
        assertEquals(0, service.countCartItems(null));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.addCartItem(1L, null, null, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteCartItem(1L, null));

        verifyNoInteractions(cartDAO);
    }

    @Test
    void selectedDeletionShouldSkipNullAndNonPositiveValuesBeforeUsingValidIds() {
        List<Long> rawSelection = Arrays.asList(null, 0L, -1L, 10L, 10L);
        when(cartDAO.deleteSelectedCartItems(1L, List.of(10L))).thenReturn(1);

        service.deleteSelectedCartItems(1L, rawSelection);

        verify(cartDAO).deleteSelectedCartItems(1L, List.of(10L));
    }

    @Test
    void createdCartWithoutGeneratedNumberShouldFailAfterSuccessfulInsert() {
        CartItemVO product = approvedProduct(5);
        when(cartDAO.selectProductForCart(20, null)).thenReturn(product);
        when(cartDAO.selectCartByMemberNo(1L)).thenReturn(null);
        when(cartDAO.insertCart(any(CartVO.class))).thenReturn(1);

        assertThrows(
                IllegalStateException.class,
                () -> service.addCartItem(1L, 20, null, 1));
    }

    @Test
    void existingItemHelperShouldTreatNullStockAsZero() {
        CartItemVO existingItem = new CartItemVO();
        existingItem.setCartItemNo(30L);
        existingItem.setQuantity(1);

        CartItemVO product = approvedProduct(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "updateExistingCartItem",
                        1L,
                        existingItem,
                        product,
                        1));
    }

    private CartItemVO approvedProduct(Integer stock) {
        CartItemVO product = new CartItemVO();
        product.setStatus("APPROVED");
        product.setProductType("ETC");
        product.setStock(stock);
        return product;
    }
}
