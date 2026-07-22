package com.project.oditji.business.vo;

import java.util.List;

public class BusinessDashboardVO {

    private long todaySales;
    private int todayOrderCount;
    private int clickCount;
    private double purchaseRate;
    private long waitingSettlement;
    private int waitingProductCount;
    private List<GoodsManageVO> popularProducts;

    public long getTodaySales() {
        return todaySales;
    }

    public void setTodaySales(long todaySales) {
        this.todaySales = todaySales;
    }

    public int getTodayOrderCount() {
        return todayOrderCount;
    }

    public void setTodayOrderCount(int todayOrderCount) {
        this.todayOrderCount = todayOrderCount;
    }

    public int getClickCount() {
        return clickCount;
    }

    public void setClickCount(int clickCount) {
        this.clickCount = clickCount;
    }

    public double getPurchaseRate() {
        return purchaseRate;
    }

    public void setPurchaseRate(double purchaseRate) {
        this.purchaseRate = purchaseRate;
    }

    public long getWaitingSettlement() {
        return waitingSettlement;
    }

    public void setWaitingSettlement(long waitingSettlement) {
        this.waitingSettlement = waitingSettlement;
    }

    public int getWaitingProductCount() {
        return waitingProductCount;
    }

    public void setWaitingProductCount(int waitingProductCount) {
        this.waitingProductCount = waitingProductCount;
    }

    public List<GoodsManageVO> getPopularProducts() {
    return popularProducts;
    }

    public void setPopularProducts(List<GoodsManageVO> popularProducts) {
        this.popularProducts = popularProducts;
    }
}