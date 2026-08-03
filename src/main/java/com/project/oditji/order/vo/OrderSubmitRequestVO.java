package com.project.oditji.order.vo;

/**
 * 주문서 화면에서 결제 준비 요청과 함께 전달되는 배송지 정보입니다.
 */
public record OrderSubmitRequestVO(
        String receiverName,
        String receiverPhone,
        String address) {
}
