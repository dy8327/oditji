package com.project.oditji.order.vo;

import java.util.List;

import com.project.oditji.cart.vo.CartItemVO;

public class OrderVO {

    private Long orderId;
    private String createdAt;
    private String status;
    private long totalPrice;

    /*
     * 현재는 임시 화면 구성을 위해 CartItemVO를 사용한다.
     *
     * 이후 실제 주문 로직을 구현할 때는
     * OrderItemVO를 별도로 만들어 교체하는 것이 좋다.
     */
    private List<CartItemVO> items;

    public OrderVO() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(long totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<CartItemVO> getItems() {
        return items;
    }

    public void setItems(
            List<CartItemVO> items) {

        this.items = items;
    }
}