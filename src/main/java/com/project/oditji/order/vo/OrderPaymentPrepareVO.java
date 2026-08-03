package com.project.oditji.order.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrderPaymentPrepareVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String paymentId;
    private String orderName;
    private Long totalAmount;

    private String receiverName;
    private String receiverPhone;
    private String address;

    private String storeId;
    private String channelKey;

    /*
     * 결제 준비 시점에 서버가 DB에서 다시 조회한 상품 정보.
     * 브라우저가 보내는 가격이 아니라 이 목록의 가격을 사용한다.
     */
    private List<OrderSheetItemVO> items = new ArrayList<OrderSheetItemVO>();

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
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

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getChannelKey() {
        return channelKey;
    }

    public void setChannelKey(String channelKey) {
        this.channelKey = channelKey;
    }

    public List<OrderSheetItemVO> getItems() {
        return items;
    }

    public void setItems(List<OrderSheetItemVO> items) {

        this.items = items == null
                ? new ArrayList<OrderSheetItemVO>()
                : items;
    }
}