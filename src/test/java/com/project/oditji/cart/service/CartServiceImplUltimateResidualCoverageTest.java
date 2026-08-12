package com.project.oditji.cart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.project.oditji.cart.dao.CartDAO;
import com.project.oditji.cart.vo.CartItemVO;
import com.project.oditji.cart.vo.CartVO;

/**
 * 옵션 상품 정상 처리와 null 재고 처리의 마지막 조건 분기를 검증합니다.
 */
class CartServiceImplUltimateResidualCoverageTest {

    private CartDAO cartDAO;
    private CartServiceImpl service;

    @BeforeEach
    void setUp() {
        cartDAO = mock(CartDAO.class);
        service = new CartServiceImpl(cartDAO);
    }

    @Test
    void shoesWithValidSelectedOptionShouldPassRequiredOptionValidation() {
        CartItemVO product = new CartItemVO();
        product.setStatus("APPROVED");
        product.setProductType("SHOES");
        product.setStock(5);
        product.setOptionNo(200L);

        CartVO cart = new CartVO();
        cart.setCartNo(30L);

        when(cartDAO.selectProductForCart(10, 200L)).thenReturn(product);
        when(cartDAO.selectCartByMemberNo(1L)).thenReturn(cart);
        when(cartDAO.selectCartItemByProduct(30L, 10, 200L)).thenReturn(null);
        when(cartDAO.insertCartItem(any(CartItemVO.class))).thenReturn(1);
        when(cartDAO.countCartItems(1L)).thenReturn(1);

        assertEquals(1, service.addCartItem(1L, 10, 200L, 1));

        verify(cartDAO).insertCartItem(any(CartItemVO.class));
    }

    @Test
    void updateQuantityShouldTreatNullStockAsSoldOut() {
        CartItemVO item = new CartItemVO();
        item.setStatus("APPROVED");
        item.setStock(null);

        when(cartDAO.selectOwnedCartItem(1L, 40L)).thenReturn(item);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateCartItemQuantity(1L, 40L, 1));

        assertEquals("품절된 상품입니다.", exception.getMessage());
    }
}
