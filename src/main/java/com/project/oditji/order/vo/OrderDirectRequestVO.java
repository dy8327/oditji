package com.project.oditji.order.vo;

import com.project.oditji.common.vo.ProductOptionSelectionVO;

/**
 * 상품 상세 화면(goodsDetail.jsp)의 "바로 구매" 버튼으로
 * 장바구니를 거치지 않고 바로 주문서를 작성할 때 사용하는 요청 VO.
 * POST /order/direct 요청 본문에 대응한다.
 *
 * 상품 번호, 옵션 번호, 옵션명, 수량은 공통 부모 VO에서 상속받습니다.
 */
public class OrderDirectRequestVO extends ProductOptionSelectionVO {

    private static final long serialVersionUID = 1L;
}
