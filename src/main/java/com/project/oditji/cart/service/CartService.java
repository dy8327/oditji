package com.project.oditji.cart.service;

import java.util.List;

import com.project.oditji.cart.vo.CartItemVO;

public interface CartService {

        List<CartItemVO> getCartItemList(
                        Long memberNo);

        int addCartItem(
                        Long memberNo,
                        Integer productNo,
                        Long optionNo,
                        Integer quantity);

        void updateCartItemQuantity(
                        Long memberNo,
                        Long cartItemNo,
                        Integer quantity);

        void deleteCartItem(
                        Long memberNo,
                        Long cartItemNo);

        void deleteSelectedCartItems(
                        Long memberNo,
                        List<Long> cartItemNos);

        int countCartItems(
                        Long memberNo);
}