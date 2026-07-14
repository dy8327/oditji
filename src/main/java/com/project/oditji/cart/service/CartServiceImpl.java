package com.project.oditji.cart.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.cart.dao.CartDAO;
import com.project.oditji.cart.vo.CartItemVO;
import com.project.oditji.cart.vo.CartVO;

@Service
public class CartServiceImpl implements CartService {

    private final CartDAO cartDAO;

    public CartServiceImpl(
            CartDAO cartDAO) {

        this.cartDAO = cartDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemVO> getCartItemList(
            Long memberNo) {

        validateMemberNo(memberNo);

        List<CartItemVO> cartItemList =
                cartDAO.selectCartItemList(memberNo);

        if (cartItemList == null) {
            return new ArrayList<CartItemVO>();
        }

        return cartItemList;
    }

    @Override
    @Transactional
    public int addCartItem(
            Long memberNo,
            Integer productNo,
            Integer quantity) {

        validateMemberNo(memberNo);
        validateProductNo(productNo);
        validateQuantity(quantity);

        /*
         * 실제 상품 조회
         */
        CartItemVO product =
                cartDAO.selectProductForCart(productNo);

        validateProductForCart(
                product,
                quantity
        );

        /*
         * 회원 장바구니 조회
         */
        CartVO cart =
                cartDAO.selectCartByMemberNo(memberNo);

        /*
         * 장바구니가 없다면 최초 생성
         */
        if (cart == null) {

            cart = new CartVO();
            cart.setMemberNo(memberNo);

            int insertResult =
                    cartDAO.insertCart(cart);

            if (insertResult <= 0
                    || cart.getCartNo() == null) {

                throw new IllegalStateException(
                        "장바구니 생성에 실패했습니다."
                );
            }
        }

        /*
         * 동일 상품이 이미 담겨 있는지 조회
         */
        CartItemVO existingItem =
                cartDAO.selectCartItemByProduct(
                        cart.getCartNo(),
                        productNo
                );

        if (existingItem == null) {

            CartItemVO newItem =
                    new CartItemVO();

            newItem.setCartNo(
                    cart.getCartNo()
            );

            newItem.setProductNo(
                    productNo
            );

            newItem.setQuantity(
                    quantity
            );

            int insertResult =
                    cartDAO.insertCartItem(newItem);

            if (insertResult <= 0) {
                throw new IllegalStateException(
                        "장바구니에 상품을 담지 못했습니다."
                );
            }

        } else {

            int existingQuantity =
                    existingItem.getQuantity() == null
                            ? 0
                            : existingItem.getQuantity();

            int changedQuantity =
                    existingQuantity + quantity;

            int stock =
                    product.getStock() == null
                            ? 0
                            : product.getStock();

            if (changedQuantity > stock) {

                throw new IllegalArgumentException(
                        "장바구니 수량이 재고를 초과합니다. "
                                + "현재 재고는 "
                                + stock
                                + "개입니다."
                );
            }

            int updateResult =
                    cartDAO.updateCartItemQuantity(
                            memberNo,
                            existingItem.getCartItemNo(),
                            changedQuantity
                    );

            if (updateResult <= 0) {
                throw new IllegalStateException(
                        "장바구니 수량을 변경하지 못했습니다."
                );
            }
        }

        return cartDAO.countCartItems(memberNo);
    }

    @Override
    @Transactional
    public void updateCartItemQuantity(
            Long memberNo,
            Long cartItemNo,
            Integer quantity) {

        validateMemberNo(memberNo);
        validateCartItemNo(cartItemNo);
        validateQuantity(quantity);

        /*
         * 반드시 로그인 회원 소유의 장바구니 상품만 조회
         */
        CartItemVO cartItem =
                cartDAO.selectOwnedCartItem(
                        memberNo,
                        cartItemNo
                );

        if (cartItem == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 장바구니 상품입니다."
            );
        }

        if (!"ON_SALE".equals(cartItem.getStatus())) {
            throw new IllegalArgumentException(
                    "현재 판매 중인 상품이 아닙니다."
            );
        }

        int stock =
                cartItem.getStock() == null
                        ? 0
                        : cartItem.getStock();

        if (stock <= 0) {
            throw new IllegalArgumentException(
                    "품절된 상품입니다."
            );
        }

        if (quantity > stock) {
            throw new IllegalArgumentException(
                    "현재 재고는 "
                            + stock
                            + "개입니다."
            );
        }

        int updateResult =
                cartDAO.updateCartItemQuantity(
                        memberNo,
                        cartItemNo,
                        quantity
                );

        if (updateResult <= 0) {
            throw new IllegalStateException(
                    "장바구니 수량을 변경하지 못했습니다."
            );
        }
    }

    @Override
    @Transactional
    public void deleteCartItem(
            Long memberNo,
            Long cartItemNo) {

        validateMemberNo(memberNo);
        validateCartItemNo(cartItemNo);

        int deleteResult =
                cartDAO.deleteCartItem(
                        memberNo,
                        cartItemNo
                );

        if (deleteResult <= 0) {
            throw new IllegalArgumentException(
                    "삭제할 장바구니 상품을 찾을 수 없습니다."
            );
        }
    }

    @Override
    @Transactional
    public void deleteSelectedCartItems(
            Long memberNo,
            List<Long> cartItemNos) {

        validateMemberNo(memberNo);

        if (cartItemNos == null
                || cartItemNos.isEmpty()) {

            throw new IllegalArgumentException(
                    "삭제할 상품을 선택해주세요."
            );
        }

        List<Long> normalizedCartItemNos =
                new ArrayList<Long>();

        for (Long cartItemNo : cartItemNos) {

            if (cartItemNo == null
                    || cartItemNo <= 0) {
                continue;
            }

            if (!normalizedCartItemNos.contains(cartItemNo)) {
                normalizedCartItemNos.add(cartItemNo);
            }
        }

        if (normalizedCartItemNos.isEmpty()) {
            throw new IllegalArgumentException(
                    "삭제할 상품을 선택해주세요."
            );
        }

        int deleteResult =
                cartDAO.deleteSelectedCartItems(
                        memberNo,
                        normalizedCartItemNos
                );

        if (deleteResult <= 0) {
            throw new IllegalArgumentException(
                    "삭제할 장바구니 상품을 찾을 수 없습니다."
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int countCartItems(
            Long memberNo) {

        if (memberNo == null
                || memberNo <= 0) {

            return 0;
        }

        return cartDAO.countCartItems(memberNo);
    }

    private void validateProductForCart(
            CartItemVO product,
            Integer quantity) {

        if (product == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 상품입니다."
            );
        }

        if (!"ON_SALE".equals(product.getStatus())) {
            throw new IllegalArgumentException(
                    "현재 판매 중인 상품이 아닙니다."
            );
        }

        int stock =
                product.getStock() == null
                        ? 0
                        : product.getStock();

        if (stock <= 0) {
            throw new IllegalArgumentException(
                    "품절된 상품입니다."
            );
        }

        if (quantity > stock) {
            throw new IllegalArgumentException(
                    "현재 재고는 "
                            + stock
                            + "개입니다."
            );
        }
    }

    private void validateMemberNo(
            Long memberNo) {

        if (memberNo == null
                || memberNo <= 0) {

            throw new IllegalArgumentException(
                    "로그인 회원 정보가 올바르지 않습니다."
            );
        }
    }

    private void validateProductNo(
            Integer productNo) {

        if (productNo == null
                || productNo <= 0) {

            throw new IllegalArgumentException(
                    "상품 번호가 올바르지 않습니다."
            );
        }
    }

    private void validateCartItemNo(
            Long cartItemNo) {

        if (cartItemNo == null
                || cartItemNo <= 0) {

            throw new IllegalArgumentException(
                    "장바구니 상품 번호가 올바르지 않습니다."
            );
        }
    }

    private void validateQuantity(
            Integer quantity) {

        if (quantity == null
                || quantity <= 0) {

            throw new IllegalArgumentException(
                    "상품 수량은 1개 이상이어야 합니다."
            );
        }
    }
}