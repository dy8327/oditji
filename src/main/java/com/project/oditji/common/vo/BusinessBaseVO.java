package com.project.oditji.common.vo;

import java.util.Date;

/**
 * 관리자 사업자 관리와 사업자 기능에서 공통으로 사용하는 계정·정산 정보입니다.
 */
public abstract class BusinessBaseVO {

    private Long businessNo;
    private Long memberNo;
    private String businessName;
    private String businessNumber;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private String gradeName;
    private Double customRate;
    private String status;
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
