package com.project.oditji.event.vo;

public class EventProductVO {

    private Long productNo;

    private String productName;

    private Long price;

    private Integer eventDiscountRate;

    private String imagePath;

    public Long getProductNo() {
        return productNo;
    }

    public void setProductNo(Long productNo) {
        this.productNo = productNo;
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

    public Long getDiscountPrice() {

        if (price == null || eventDiscountRate == null) {

            return null;

        }

        return Math.round(
                price * (100 - eventDiscountRate) / 100.0);

    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

}