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

    public List<OrderItemVO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemVO> items) {
        this.items = items == null ? new ArrayList<OrderItemVO>() : items;
    }
}
