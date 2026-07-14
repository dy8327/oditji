package com.project.oditji.admin.vo;

import java.util.Date;

/**
 * 정산 관리 VO (테이블: SETTLEMENT + BUSINESS 조인)
 */
public class SettlementManageVO {

    private Long settlementNo;
    private Long businessNo;
    private String businessName;   // BUSINESS 조인
    private Long orderItemNo;
    private Long totalAmount;
    private String appliedGrade;
    private Double appliedRate;
    private Long feeAmount;
    private Long settledAmount;
    private String status;         // WAITING, DONE, REJECTED
    private String bankName;       // BUSINESS 조인
    private String accountNumber;  // BUSINESS 조인
    private String accountHolder;  // BUSINESS 조인
    private Date settledAt;
    private Date createdAt;

    public Long getSettlementNo() {
        return settlementNo;
    }

    public void setSettlementNo(Long settlementNo) {
        this.settlementNo = settlementNo;
    }

    public Long getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(Long businessNo) {
        this.businessNo = businessNo;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public Long getOrderItemNo() {
        return orderItemNo;
    }

    public void setOrderItemNo(Long orderItemNo) {
        this.orderItemNo = orderItemNo;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getAppliedGrade() {
        return appliedGrade;
    }

    public void setAppliedGrade(String appliedGrade) {
        this.appliedGrade = appliedGrade;
    }

    public Double getAppliedRate() {
        return appliedRate;
    }

    public void setAppliedRate(Double appliedRate) {
        this.appliedRate = appliedRate;
    }

    public Long getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(Long feeAmount) {
        this.feeAmount = feeAmount;
    }

    public Long getSettledAmount() {
        return settledAmount;
    }

    public void setSettledAmount(Long settledAmount) {
        this.settledAmount = settledAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Date getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(Date settledAt) {
        this.settledAt = settledAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
