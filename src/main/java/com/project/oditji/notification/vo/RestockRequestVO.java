package com.project.oditji.notification.vo;

import java.time.LocalDateTime;

/**
 * 품절 상품 재입고 알림 신청 정보를 담는 VO입니다.
 */
public class RestockRequestVO {

    private Long restockRequestNo;
    private Long memberNo;
    /*
     * [옵션별 재입고 알림 추가]
     * NULL이면 상품 전체 재입고 신청,
     * 값이 있으면 해당 PRODUCT_OPTION의 재입고 신청입니다.
     */
    private Long optionNo;
    private Long productNo;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime notifiedAt;

    public Long getRestockRequestNo() {
        return restockRequestNo;
    }

    public void setRestockRequestNo(Long restockRequestNo) {
        this.restockRequestNo = restockRequestNo;
    }

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public Long getOptionNo() {
        return optionNo;
    }

    public void setOptionNo(Long optionNo) {
        this.optionNo = optionNo;
    }

    public Long getProductNo() {
        return productNo;
    }

    public void setProductNo(Long productNo) {
        this.productNo = productNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(LocalDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }
}
