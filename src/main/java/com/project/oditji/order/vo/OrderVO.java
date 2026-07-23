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

    public List<OrderItemVO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemVO> items) {
        this.items = items == null ? new ArrayList<OrderItemVO>() : items;
    }
}
