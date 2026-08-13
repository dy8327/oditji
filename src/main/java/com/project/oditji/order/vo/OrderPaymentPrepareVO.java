package com.project.oditji.order.vo;

import java.util.ArrayList;
import java.util.List;

import com.project.oditji.common.vo.OrderAddressBaseVO;

/**
 * 결제창 호출 전에 서버가 확정한 주문명·금액·상품 목록을 전달합니다.
 */
public class OrderPaymentPrepareVO extends OrderAddressBaseVO {

    private static final long serialVersionUID = 1L;

    private String paymentId;
    private String orderName;
    private Long totalAmount;
    private String storeId;
    private String channelKey;
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
