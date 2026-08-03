package com.project.oditji.business.vo;

import java.time.LocalDate;
import java.util.List;

import com.project.oditji.common.vo.EventBaseVO;

/**
 * 사업자 이벤트 등록·수정·조회에 사용하는 VO입니다.
 */
public class EventManageVO extends EventBaseVO {

    private LocalDate startDate;
    private LocalDate endDate;
    private Long eventProdNo;
    private List<Long> productNoList;
    private List<Integer> discountRateList;
    private String productName;
    private Long price;
    private Integer eventDiscountRate;
    private List<EventProductVO> connectedProducts;
    private Long businessNo;

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getEventProdNo() {
        return eventProdNo;
    }

    public void setEventProdNo(Long eventProdNo) {
        this.eventProdNo = eventProdNo;
    }

    public List<Long> getProductNoList() {
        return productNoList;
    }

    public void setProductNoList(List<Long> productNoList) {
        this.productNoList = productNoList;
    }

    public List<Integer> getDiscountRateList() {
        return discountRateList;
    }

    public void setDiscountRateList(List<Integer> discountRateList) {
        this.discountRateList = discountRateList;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Integer getEventDiscountRate() {
        return eventDiscountRate;
    }

    public void setEventDiscountRate(Integer eventDiscountRate) {
        this.eventDiscountRate = eventDiscountRate;
    }

    public List<EventProductVO> getConnectedProducts() {
        return connectedProducts;
    }

    public void setConnectedProducts(List<EventProductVO> connectedProducts) {
        this.connectedProducts = connectedProducts;
    }

    public Long getDiscountedPrice() {
        if (price == null || eventDiscountRate == null) {
            return null;
        }

        return price - (price * eventDiscountRate / 100);
    }

    public Long getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(Long businessNo) {
        this.businessNo = businessNo;
    }

    @Override
    public String toString() {
        return "EventManageVO{" +
                "eventNo=" + getEventNo() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", bannerImage='" + getBannerImage() + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status='" + getStatus() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", eventProdNo=" + eventProdNo +
                ", productNoList=" + productNoList +
                ", discountRateList=" + discountRateList +
                ", productName='" + productName + '\'' +
                ", price=" + price +
                ", eventDiscountRate=" + eventDiscountRate +
                ", connectedProducts=" + connectedProducts +
                ", businessNo=" + businessNo +
                '}';
    }
}
