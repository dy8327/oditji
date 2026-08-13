package com.project.oditji.cart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.cart.dao.CartDAO;
import com.project.oditji.cart.vo.CartItemVO;
import com.project.oditji.cart.vo.CartVO;

/** 장바구니 서비스의 입력 검증과 DAO 실패 분기를 추가로 검증합니다. */
@ExtendWith(MockitoExtension.class)
class CartServiceImplBranchCoverageTest {

    @Mock
    private CartDAO cartDAO;

    private CartServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CartServiceImpl(cartDAO);
    }

    @Test
    void listAndCountShouldCoverValidAndInvalidMemberBranches() {
        List<CartItemVO> items = List.of(new CartItemVO());
        when(cartDAO.selectCartItemList(1L)).thenReturn(items);
        when(cartDAO.countCartItems(1L)).thenReturn(4);

        assertSame(items, service.getCartItemList(1L));
        assertEquals(4, service.countCartItems(1L));
        assertEquals(0, service.countCartItems(0L));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getCartItemList(null));

        verify(cartDAO, never()).countCartItems(0L);
    }

    @Test
    void addShouldRejectInvalidIdentifiersQuantityAndUnavailableProducts() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.addCartItem(null, 1, null, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.addCartItem(1L, 0, null, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.addCartItem(1L, 1, null, 0));

        when(cartDAO.selectProductForCart(1, null)).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.addCartItem(1L, 1, null, 1));

        CartItemVO waiting = product("WAITING", "ETC", 3, null);
        when(cartDAO.selectProductForCart(2, null)).thenReturn(waiting);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.addCartItem(1L, 2, null, 1));

        CartItemVO soldOut = product("APPROVED", "ETC", null, null);
        when(cartDAO.selectProductForCart(3, null)).thenReturn(soldOut);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.addCartItem(1L, 3, null, 1));
    }

    @Test
    void addShouldRequireMatchingOptionForClothesAndShoes() {
        CartItemVO clothes = product("APPROVED", "CLOTHES", 5, null);
        when(cartDAO.selectProductForCart(10, 100L)).thenReturn(clothes);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addCartItem(1L, 10, 100L, 1));

        CartItemVO shoes = product("APPROVED", "SHOES", 5, 200L);
        when(cartDAO.selectProductForCart(11, null)).thenReturn(shoes);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addCartItem(1L, 11, null, 1));
    }

    @Test
    void addShouldFailWhenCartOrNewItemCannotBeCreated() {
        CartItemVO product = product("APPROVED", "ETC", 5, null);
        when(cartDAO.selectProductForCart(20, null)).thenReturn(product);
        when(cartDAO.selectCartByMemberNo(1L)).thenReturn(null);
        when(cartDAO.insertCart(any(CartVO.class))).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.addCartItem(1L, 20, null, 1));

        CartVO existingCart = new CartVO();
        existingCart.setCartNo(30L);
        when(cartDAO.selectProductForCart(21, null)).thenReturn(product);
        when(cartDAO.selectCartByMemberNo(2L)).thenReturn(existingCart);
        when(cartDAO.selectCartItemByProduct(30L, 21, null)).thenReturn(null);
        when(cartDAO.insertCartItem(any(CartItemVO.class))).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.addCartItem(2L, 21, null, 1));
    }

    @Test
    void addExistingItemShouldCoverNullQuantityStockLimitAndDaoFailure() {
        CartVO cart = new CartVO();
        cart.setCartNo(40L);
        CartItemVO product = product("APPROVED", "ETC", 3, null);
        CartItemVO existing = new CartItemVO();
        existing.setCartItemNo(41L);
        existing.setQuantity(null);

        when(cartDAO.selectProductForCart(30, null)).thenReturn(product);
        when(cartDAO.selectCartByMemberNo(1L)).thenReturn(cart);
        when(cartDAO.selectCartItemByProduct(40L, 30, null)).thenReturn(existing);
        when(cartDAO.updateCartItemQuantity(1L, 41L, 2)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.addCartItem(1L, 30, null, 2));

        existing.setQuantity(3);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.addCartItem(1L, 30, null, 1));
    }

    @Test
    void updateQuantityShouldCoverValidationStockStateAndDaoFailure() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateCartItemQuantity(0L, 1L, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateCartItemQuantity(1L, 0L, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateCartItemQuantity(1L, 1L, null));

        CartItemVO waiting = product("WAITING", "ETC", 5, null);
        when(cartDAO.selectOwnedCartItem(1L, 10L)).thenReturn(waiting);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateCartItemQuantity(1L, 10L, 1));

        CartItemVO soldOut = product("APPROVED", "ETC", 0, null);
        when(cartDAO.selectOwnedCartItem(1L, 11L)).thenReturn(soldOut);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateCartItemQuantity(1L, 11L, 1));

        CartItemVO limited = product("APPROVED", "ETC", 2, null);
        when(cartDAO.selectOwnedCartItem(1L, 12L)).thenReturn(limited);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateCartItemQuantity(1L, 12L, 3));

        when(cartDAO.selectOwnedCartItem(1L, 13L)).thenReturn(limited);
        when(cartDAO.updateCartItemQuantity(1L, 13L, 2)).thenReturn(0);
        assertThrows(
                IllegalStateException.class,
                () -> service.updateCartItemQuantity(1L, 13L, 2));
    }

    @Test
    void deleteSingleShouldCoverSuccessInputValidationAndMissingTarget() {
        when(cartDAO.deleteCartItem(1L, 10L)).thenReturn(1);
        service.deleteCartItem(1L, 10L);
        verify(cartDAO).deleteCartItem(1L, 10L);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteCartItem(null, 10L));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteCartItem(1L, -1L));

        when(cartDAO.deleteCartItem(1L, 11L)).thenReturn(0);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteCartItem(1L, 11L));
    }

    @Test
    void deleteSelectedShouldCoverEmptyInvalidSuccessAndDaoFailureBranches() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteSelectedCartItems(1L, null));
        List<Long> emptySelection = List.of();
        assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteSelectedCartItems(1L, emptySelection));
        List<Long> invalidSelection = List.of(0L, -1L);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteSelectedCartItems(1L, invalidSelection));

        when(cartDAO.deleteSelectedCartItems(eq(1L), anyList())).thenReturn(2);
        service.deleteSelectedCartItems(1L, List.of(10L, 10L, 20L));
        verify(cartDAO).deleteSelectedCartItems(1L, List.of(10L, 20L));

        List<Long> missingSelection = List.of(30L);
        when(cartDAO.deleteSelectedCartItems(1L, missingSelection)).thenReturn(0);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteSelectedCartItems(1L, missingSelection));
    }

    private CartItemVO product(
            String status,
            String productType,
            Integer stock,
            Long optionNo) {

        CartItemVO item = new CartItemVO();
        item.setStatus(status);
        item.setProductType(productType);
        item.setStock(stock);
        item.setOptionNo(optionNo);
        return item;
    }
}
