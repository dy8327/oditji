package com.project.oditji.cart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.cart.dao.CartDAO;
import com.project.oditji.cart.vo.CartItemVO;
import com.project.oditji.cart.vo.CartVO;

/**
 * 장바구니 등록, 수량 변경, 삭제와 입력값 검증을 DB 없이 확인합니다.
 */
@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartDAO cartDAO;

    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartServiceImpl(cartDAO);
    }

    @Test
    void getCartItemListShouldReturnEmptyListWhenDaoReturnsNull() {

        when(cartDAO.selectCartItemList(1L)).thenReturn(null);

        assertTrue(cartService.getCartItemList(1L).isEmpty());
    }

    @Test
    void addCartItemShouldInsertNewItemIntoExistingCart() {

        CartItemVO product = createApprovedProduct("ETC", 10);
        CartVO cart = new CartVO();
        cart.setCartNo(5L);

        when(cartDAO.selectProductForCart(10, null)).thenReturn(product);
        when(cartDAO.selectCartByMemberNo(1L)).thenReturn(cart);
        when(cartDAO.selectCartItemByProduct(5L, 10, null)).thenReturn(null);
        when(cartDAO.insertCartItem(any(CartItemVO.class))).thenReturn(1);
        when(cartDAO.countCartItems(1L)).thenReturn(2);

        int result = cartService.addCartItem(1L, 10, null, 2);

        assertEquals(2, result);

        ArgumentCaptor<CartItemVO> captor =
                ArgumentCaptor.forClass(CartItemVO.class);
        verify(cartDAO).insertCartItem(captor.capture());
        assertEquals(5L, captor.getValue().getCartNo());
        assertEquals(10, captor.getValue().getProductNo());
        assertEquals(2, captor.getValue().getQuantity());
    }

    @Test
    void addCartItemShouldCreateCartWhenMemberHasNoCart() {

        CartItemVO product = createApprovedProduct("ETC", 10);
        when(cartDAO.selectProductForCart(10, null)).thenReturn(product);
        when(cartDAO.selectCartByMemberNo(1L)).thenReturn(null);
        when(cartDAO.insertCart(any(CartVO.class))).thenAnswer(invocation -> {
            CartVO cart = invocation.getArgument(0);
            cart.setCartNo(8L);
            return 1;
        });
        when(cartDAO.selectCartItemByProduct(8L, 10, null)).thenReturn(null);
        when(cartDAO.insertCartItem(any(CartItemVO.class))).thenReturn(1);
        when(cartDAO.countCartItems(1L)).thenReturn(1);

        assertEquals(1, cartService.addCartItem(1L, 10, null, 1));
        verify(cartDAO).insertCart(any(CartVO.class));
    }

    @Test
    void addCartItemShouldIncreaseQuantityForExistingItem() {

        CartItemVO product = createApprovedProduct("ETC", 10);
        CartVO cart = new CartVO();
        cart.setCartNo(5L);
        CartItemVO existingItem = new CartItemVO();
        existingItem.setCartItemNo(20L);
        existingItem.setQuantity(3);

        when(cartDAO.selectProductForCart(10, null)).thenReturn(product);
        when(cartDAO.selectCartByMemberNo(1L)).thenReturn(cart);
        when(cartDAO.selectCartItemByProduct(5L, 10, null)).thenReturn(existingItem);
        when(cartDAO.updateCartItemQuantity(1L, 20L, 5)).thenReturn(1);
        when(cartDAO.countCartItems(1L)).thenReturn(1);

        cartService.addCartItem(1L, 10, null, 2);

        verify(cartDAO).updateCartItemQuantity(1L, 20L, 5);
        verify(cartDAO, never()).insertCartItem(any());
    }

    @Test
    void addCartItemShouldRejectClothesWithoutOption() {

        CartItemVO product = createApprovedProduct("CLOTHES", 10);
        product.setOptionNo(null);
        when(cartDAO.selectProductForCart(10, null)).thenReturn(product);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addCartItem(1L, 10, null, 1));

        assertEquals("색상과 사이즈 옵션을 선택해주세요.", exception.getMessage());
    }

    @Test
    void addCartItemShouldRejectQuantityGreaterThanStock() {

        CartItemVO product = createApprovedProduct("ETC", 2);
        when(cartDAO.selectProductForCart(10, null)).thenReturn(product);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addCartItem(1L, 10, null, 3));
    }

    @Test
    void updateCartItemQuantityShouldUpdateOwnedApprovedItem() {

        CartItemVO item = createApprovedProduct("ETC", 5);
        when(cartDAO.selectOwnedCartItem(1L, 20L)).thenReturn(item);
        when(cartDAO.updateCartItemQuantity(1L, 20L, 3)).thenReturn(1);

        cartService.updateCartItemQuantity(1L, 20L, 3);

        verify(cartDAO).updateCartItemQuantity(1L, 20L, 3);
    }

    @Test
    void updateCartItemQuantityShouldRejectMissingOwnedItem() {

        when(cartDAO.selectOwnedCartItem(1L, 20L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.updateCartItemQuantity(1L, 20L, 1));

        assertEquals("존재하지 않는 장바구니 상품입니다.", exception.getMessage());
    }

    @Test
    void deleteSelectedCartItemsShouldRemoveInvalidAndDuplicateNumbers() {

        when(cartDAO.deleteSelectedCartItems(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(2);

        cartService.deleteSelectedCartItems(
                1L,
                Arrays.asList(10L, null, -1L, 10L, 20L));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Long>> captor =
                ArgumentCaptor.forClass(List.class);
        verify(cartDAO).deleteSelectedCartItems(
                org.mockito.ArgumentMatchers.eq(1L),
                captor.capture());

        assertEquals(List.of(10L, 20L), captor.getValue());
    }

    @Test
    void countCartItemsShouldReturnZeroForInvalidMember() {

        assertEquals(0, cartService.countCartItems(null));
        verify(cartDAO, never()).countCartItems(
                org.mockito.ArgumentMatchers.anyLong());
    }

    private CartItemVO createApprovedProduct(String productType, int stock) {

        CartItemVO product = new CartItemVO();
        product.setStatus("APPROVED");
        product.setProductType(productType);
        product.setStock(stock);
        return product;
    }
}
