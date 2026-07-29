package com.project.oditji.admin.vo;

import java.util.Date;

/**
 * 사업자 관리 VO (테이블: BUSINESS + MEMBER 조인, GRADE_POLICY 참조)
 */
public class BusinessManageVO {

    private Long businessNo;
    private Long memberNo;
    private String memberId; // MEMBER.MEMBER_ID (조인)
    private String email; // MEMBER.EMAIL (조인)
    private String businessName;
    private String businessNumber;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private String gradeName; // BRONZE, SILVER, GOLD, PLATINUM, VIP (GRADE_POLICY 참조)
    private Double customRate;

    /*
     * [사업자 자동 등급 관리 추가]
     * 결제 완료 후 취소되지 않은 주문상품을 기준으로 계산한 사업자 누적 실매출이다.
     */
    private Long totalSales;
    private String status; // WAITING, APPROVED, REJECTED
    private Date createdAt;

    public Long getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(Long businessNo) {
        this.businessNo = businessNo;
    }

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessNumber() {
        return businessNumber;
    }

    public void setBusinessNumber(String businessNumber) {
        this.businessNumber = businessNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public Double getCustomRate() {
        return customRate;
    }

    public void setCustomRate(Double customRate) {
        this.customRate = customRate;
    }

    public Long getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(Long totalSales) {
        this.totalSales = totalSales;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}