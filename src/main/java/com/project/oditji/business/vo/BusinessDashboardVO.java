package com.project.oditji.business.vo;

import java.util.List;
import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.review.vo.ProductReviewVO;

public class BusinessDashboardVO {

    private long todaySales;
    private int todayOrderCount;

    /*
     * =========================================================
     * [오늘 구매 고객 수 추가]
     * 오늘 정상 판매 상태의 상품을 구매한 중복 회원 수.
     * =========================================================
     */
    private int todayCustomerCount;

    private int clickCount;
    private double purchaseRate;
    private long waitingSettlement;
    private int waitingProductCount;
    private List<GoodsManageVO> popularProducts;

    /*
     * =========================================================
     * [사업자 대시보드 최근 현황 추가]
     *
     * 최근 주문 / 최근 상품 리뷰 / 전체 상품 평균 리뷰 점수를
     * 사업자 메인 화면에서 출력하기 위한 필드입니다.
     * =========================================================
     */
    private List<OrderItemVO> recentOrders;
    private List<ProductReviewVO> recentReviews;
    private double averageRating;

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

    /*
     * =========================================================
     * [오늘 구매 고객 수 getter/setter 추가]
     * =========================================================
     */
    public int getTodayCustomerCount() {
        return todayCustomerCount;
    }

    public void setTodayCustomerCount(int todayCustomerCount) {
        this.todayCustomerCount = todayCustomerCount;
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

    /*
     * =========================================================
     * [사업자 대시보드 최근 주문]
     * =========================================================
     */
    public List<OrderItemVO> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<OrderItemVO> recentOrders) {
        this.recentOrders = recentOrders;
    }

    /*
     * =========================================================
     * [사업자 대시보드 최근 리뷰]
     * =========================================================
     */
    public List<ProductReviewVO> getRecentReviews() {
        return recentReviews;
    }

    public void setRecentReviews(List<ProductReviewVO> recentReviews) {
        this.recentReviews = recentReviews;
    }

    /*
     * =========================================================
     * [사업자 대시보드 평균 상품 리뷰 점수]
     * =========================================================
     */
    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
}