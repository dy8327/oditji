package com.project.oditji.order.vo;

import java.util.List;
import com.project.oditji.cart.vo.CartVO;

public class OrderVO {

    private int orderId;
    private String createdAt;
    private String status;
    private int totalPrice;

    private List<CartVO> items;

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalPrice() { return totalPrice; }
    public void setTotalPrice(int totalPrice) { this.totalPrice = totalPrice; }

    public List<CartVO> getItems() { return items; }
    public void setItems(List<CartVO> items) { this.items = items; }
}