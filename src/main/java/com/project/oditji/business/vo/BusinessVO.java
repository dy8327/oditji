package com.project.oditji.business.vo;

import java.util.Date;

public class BusinessVO {

    private long businessNo;
    private long memberNo;

    // 사업자 기본 정보
    private String businessName;
    private String businessNumber;
    private String representativeName;   // 대표자명
    private String openDate;             // 개업일 YYYYMMDD

    // 사업자등록증
    private String licenseFilePath;      // 사업자등록증 파일 경로

    // 국세청 사업자 확인 정보
    private String ntsBusinessStatus;    // 계속사업자 / 휴업자 / 폐업자 등
    private Date ntsCheckedAt;           // 국세청 확인 일시

    // 정산 정보
    private String bankName;
    private String accountNumber;
    private String accountHolder;

    // 사업자 등급 / 수수료
    private String gradeName;
    private Double customRate;

    // 관리자 승인 상태
    private String status;               // WAITING / APPROVED / REJECTED
    private String rejectReason;         // 승인 거절 사유

    private Date createdAt;


    public long getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(long businessNo) {
        this.businessNo = businessNo;
    }

    public long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(long memberNo) {
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

    public String getRepresentativeName() {
        return representativeName;
    }

    public void setRepresentativeName(String representativeName) {
        this.representativeName = representativeName;
    }

    public String getOpenDate() {
        return openDate;
    }

    public void setOpenDate(String openDate) {
        this.openDate = openDate;
    }

    public String getLicenseFilePath() {
        return licenseFilePath;
    }

    public void setLicenseFilePath(String licenseFilePath) {
        this.licenseFilePath = licenseFilePath;
    }

    public String getNtsBusinessStatus() {
        return ntsBusinessStatus;
    }

    public void setNtsBusinessStatus(String ntsBusinessStatus) {
        this.ntsBusinessStatus = ntsBusinessStatus;
    }

    public Date getNtsCheckedAt() {
        return ntsCheckedAt;
    }

    public void setNtsCheckedAt(Date ntsCheckedAt) {
        this.ntsCheckedAt = ntsCheckedAt;
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

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}