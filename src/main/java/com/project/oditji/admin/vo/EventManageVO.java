package com.project.oditji.admin.vo;

import java.util.Date;

/**
 * 이벤트 관리 VO (테이블: EVENT + EVENT_PRODUCT + PRODUCT + BUSINESS)
 *
 * EVENT 테이블에는 사업자 연결(BUSINESS_NO), 요청 내용(DESCRIPTION),
 * 요청 유형(등록/수정/연장 구분) 컬럼이 여전히 없다.
 * 대신 EVENT -> EVENT_PRODUCT -> PRODUCT -> BUSINESS 경로로 조인하여
 * businessName / productName / price / discountRate를 실제 값으로 채운다.
 * (요청 유형 구분이 필요해지면 REQUEST_TYPE 컬럼 추가를 별도로 검토한다.)
 */
public class EventManageVO {

    private Long eventNo; // = EVENT.EVENT_NO (이벤트 번호)

    private String businessName; // = BUSINESS.BUSINESS_NAME (사업자명)

    private String title; // = EVENT.TITLE (이벤트 제목)

    private Date startDate; // = EVENT.START_DATE (이벤트 시작일)

    private Date endDate; // = EVENT.END_DATE (이벤트 종료일)

    private Date createdAt; // = EVENT.CREATED_AT (이벤트 등록일)

    private String status; // = EVENT.STATUS (WAITING/APPROVED/END/REJECTED/DELETED)

    private String description; // 이벤트 설명 (현재 EVENT 테이블 미지원)

    // ===================== 연결 상품 및 할인 정보 =====================

    private Long productNo; // = EVENT_PRODUCT.PRODUCT_NO (연결 상품 번호)

    private String productName; // = PRODUCT.PRODUCT_NAME (상품명)

    private Long price; // = PRODUCT.PRICE (상품 가격)

    private Integer eventDiscountRate; // = EVENT_PRODUCT.EVENT_DISCOUNT_RATE (이벤트 할인율 %)

    // ===================== Getter / Setter =====================

    public Long getEventNo() {
        return eventNo;
    }

    public void setEventNo(Long eventNo) {
        this.eventNo = eventNo;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    /**
     * 이벤트 할인 적용 후 판매 가격 계산
     *
     * 예:
     * price = 10000
     * eventDiscountRate = 20
     *
     * 결과:
     * 8000원
     */
    public Long getDiscountedPrice() {

        if (price == null || eventDiscountRate == null) {
            return null;
        }

        return Math.round(
                price * (100 - eventDiscountRate) / 100.0);
    }

}