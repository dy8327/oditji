package com.project.oditji.cart.vo;

import java.util.List;

import com.project.oditji.common.vo.ProductSelectionVO;

/**
 * 장바구니 추가, 수량 변경, 선택 삭제 요청에 사용하는 VO입니다.
 *
 * 상품 번호, 옵션 번호, 수량은 공통 부모 VO에서 상속받습니다.
 */
public class CartRequestVO extends ProductSelectionVO {

    private static final long serialVersionUID = 1L;

    private Long cartItemNo;
    private List<Long> cartItemNos;

    public Long getCartItemNo() {
        return cartItemNo;
    }

    public void setCartItemNo(Long cartItemNo) {
        this.cartItemNo = cartItemNo;
    }

    public List<Long> getCartItemNos() {
        return cartItemNos;
    }

    public void setCartItemNos(List<Long> cartItemNos) {
        this.cartItemNos = cartItemNos;
    }
}
