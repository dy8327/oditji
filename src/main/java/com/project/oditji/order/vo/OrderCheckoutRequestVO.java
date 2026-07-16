package com.project.oditji.order.vo;

import java.util.List;

/**
 * 장바구니 화면(cart.jsp)에서 선택한 상품으로 주문서를 작성할 때 사용하는 요청 VO.
 * POST /order/checkout 요청 본문에 대응한다.
 */
public class OrderCheckoutRequestVO {

    private List<Long> cartItemNos;

    public OrderCheckoutRequestVO() {
    }

    public List<Long> getCartItemNos() {
        return cartItemNos;
    }

    public void setCartItemNos(List<Long> cartItemNos) {
        this.cartItemNos = cartItemNos;
    }
}
