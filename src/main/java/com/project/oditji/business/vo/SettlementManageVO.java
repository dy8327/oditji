package com.project.oditji.business.vo;

import java.time.LocalDate;

public class SettlementManageVO {

    /*
     * =========================================================
     * 사업자 판매 현황 조회용 VO
     *
     * 같은 VO를 조회 기간 요약(salesStatus)과
     * 날짜별 판매 내역(salesHistory)에 함께 사용한다.
     * =========================================================
     */
    private long dailySales;
    private String productName;
    private int orderCount;

    private LocalDate saleDate;
    private int quantity;
    private long amount;

    public long getDailySales() {
        return dailySales;
    }

    public void setDailySales(long dailySales) {
        this.dailySales = dailySales;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
