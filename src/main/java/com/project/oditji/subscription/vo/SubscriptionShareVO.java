package com.project.oditji.subscription.vo;

import java.util.Date;

/**
 * SUBSCRIPTION_RESULT 테이블 매핑 VO입니다.
 *
 * OTT 구독 조합 계산기의 계산 결과를 저장하고, 공유 링크로 복원할 때 사용합니다.
 * selectedServicesJson에는 SubscriptionCalculationResultVO의 selectedPlatformList,
 * unresolvedItemList 제목, allPlatformMonthlyPrice를 JSON 문자열로 담습니다.
 */
public class SubscriptionShareVO {

    private String resultId;

    private Long memberNo;

    private int totalPrice;

    private int discountPrice;

    private int finalPrice;

    private String selectedServicesJson;

    /*
     * [비회원 공유 링크 임시 보관 추가]
     * 비회원(MEMBER_NO NULL)이 저장한 결과에만 만료 시각을 채운다.
     * 회원 결과는 계속 NULL로 두어 영구 보관한다.
     */
    private Date expiresAt;

    private Date createdAt;

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(int discountPrice) {
        this.discountPrice = discountPrice;
    }

    public int getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(int finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getSelectedServicesJson() {
        return selectedServicesJson;
    }

    public void setSelectedServicesJson(String selectedServicesJson) {
        this.selectedServicesJson = selectedServicesJson;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
