package com.project.oditji.order.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderVO {

    private Long orderNo;
    private Long memberNo;
    private Long totalAmount;
    private String orderStatus;
    private String receiverName;
    private String receiverPhone;
    private String address;
    private Date createdAt;

    /*
     * =========================================================
     * [결제수단별 부분 취소 제한 표시용 필드 추가]
     *
     * 사용자 주문내역에서 테스트 채널 간편결제 여부를 판단해
     * 상품 부분 취소 요청을 사전에 안내하기 위해 사용한다.
     * DB ORDERS 컬럼이 아니라 PAYMENT 조인 조회 결과이다.
     * =========================================================
     */
    private String payMethod;
    private String pgProvider;

    /*
     * =========================================================
     * [주문 전체 취소 상태 및 반려 사유 표시용 필드 추가]
     *
     * 가장 최근 FULL 취소 그룹의 처리 상태와 반려 사유를
     * 사용자 주문내역에 표시하기 위한 조회 전용 필드이다.
     * DB ORDERS 컬럼과 직접 매핑되지 않는다.
     * =========================================================
     */
    private String fullCancelStatus;
    private String fullCancelRejectReason;

    /*
     * 주문 목록/상세 화면 구성을 위해 서비스 계층에서 채워주는
     * 해당 주문에 속한 상품 목록 (ORDER_ITEM 조인 결과).
     * DB 컬럼과 직접 매핑되지 않는다.
     */
    private List<OrderItemVO> items = new ArrayList<OrderItemVO>();

    public OrderVO() {
    }

    public Long getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Long orderNo) {
        this.orderNo = orderNo;
    }

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getPgProvider() {
        return pgProvider;
    }

    public void setPgProvider(String pgProvider) {
        this.pgProvider = pgProvider;
    }

    public String getFullCancelStatus() {
        return fullCancelStatus;
    }

    public void setFullCancelStatus(String fullCancelStatus) {
        this.fullCancelStatus = fullCancelStatus;
    }

    public String getFullCancelRejectReason() {
        return fullCancelRejectReason;
    }

    public void setFullCancelRejectReason(String fullCancelRejectReason) {
        this.fullCancelRejectReason = fullCancelRejectReason;
    }

    public List<OrderItemVO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemVO> items) {
        this.items = items == null ? new ArrayList<OrderItemVO>() : items;
    }

    /*
     * =========================================================
     * [추가] 주문 카드 전체 취소/전체 환불 버튼 노출 판단
     * 모든 주문상품이 동일한 요청 가능 상태일 때만 전체 버튼을 표시한다.
     * =========================================================
     */
    public boolean isAllCancelEligible() {
        return !items.isEmpty() && items.stream().allMatch(OrderItemVO::isCancelEligible);
    }

    public boolean isAllRefundEligible() {
        return !items.isEmpty() && items.stream().allMatch(OrderItemVO::isRefundEligible);
    }
}