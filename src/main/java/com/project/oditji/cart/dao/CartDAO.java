package com.project.oditji.cart.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.project.oditji.cart.vo.CartItemVO;
import com.project.oditji.cart.vo.CartVO;

public interface CartDAO {

        CartVO selectCartByMemberNo(
                        @Param("memberNo") Long memberNo);

        int insertCart(
                        CartVO cartVO);

        CartItemVO selectProductForCart(
                        @Param("productNo") Integer productNo,
                        @Param("optionNo") Long optionNo);

        CartItemVO selectCartItemByProduct(
                        @Param("cartNo") Long cartNo,
                        @Param("productNo") Integer productNo,
                        @Param("optionNo") Long optionNo);

        CartItemVO selectOwnedCartItem(
                        @Param("memberNo") Long memberNo,
                        @Param("cartItemNo") Long cartItemNo);

        int insertCartItem(
                        CartItemVO cartItemVO);

        int updateCartItemQuantity(
                        @Param("memberNo") Long memberNo,
                        @Param("cartItemNo") Long cartItemNo,
                        @Param("quantity") Integer quantity);

        int deleteCartItem(
                        @Param("memberNo") Long memberNo,
                        @Param("cartItemNo") Long cartItemNo);

        int deleteSelectedCartItems(
                        @Param("memberNo") Long memberNo,
                        @Param("cartItemNos") List<Long> cartItemNos);

        List<CartItemVO> selectCartItemList(
                        @Param("memberNo") Long memberNo);

        int countCartItems(
                        @Param("memberNo") Long memberNo);
}