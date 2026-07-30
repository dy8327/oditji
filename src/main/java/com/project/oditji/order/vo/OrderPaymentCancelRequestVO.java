package com.project.oditji.order.vo;

import java.util.List;

/**
 * 사용자 주문 내역에서 결제 전액 취소를 요청할 때 사용하는 VO.
 *
 * 브라우저에서 paymentId를 직접 전달하지 않고 주문 번호만 전달한다.
 * 서버가 로그인 회원과 주문 소유권을 검증한 뒤
 * PAYMENT 테이블에서 실제 paymentId를 조회한다.
 */
public class OrderPaymentCancelRequestVO {

    private Long orderNo;

    /* [상품별 부분 취소 기능 추가] */
    private Long orderItemNo;

    /* [추가] 선택 상품 일괄 취소/환불 요청 번호 목록 */
    private List<Long> orderItemNos;

    private String reason;

    public OrderPaymentCancelRequestVO() {
    }

    public Long getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Long orderNo) {
        this.orderNo = orderNo;
    }

    public Long getOrderItemNo() {
        return orderItemNo;
    }

    public void setOrderItemNo(Long orderItemNo) {
        this.orderItemNo = orderItemNo;
    }

    public List<Long> getOrderItemNos() {
        return orderItemNos;
    }

    public void setOrderItemNos(List<Long> orderItemNos) {
        this.orderItemNos = orderItemNos;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}