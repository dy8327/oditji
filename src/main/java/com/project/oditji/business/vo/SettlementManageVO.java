package com.project.oditji.business.vo;

import java.time.LocalDate;

public class SettlementManageVO {

    /*
     * =========================================================
     * 사업자 판매 현황 및 수수료 관리 공용 VO
     *
     * 기존 판매 현황 필드는 그대로 유지하고,
     * 이번 달 수수료/납부 내역/계좌 정보 화면에 필요한 필드를 추가한다.
     * =========================================================
     */
    private long dailySales;
    private String productName;
    private int orderCount;
    private LocalDate saleDate;
    private int quantity;
    private long amount;

    /* [수정] 이번 달 수수료 요약 조회용 필드 */
    private long monthSales;
    private long feeAmount;
    private double feeRate;
    private String expectedDate;
    private String status;
    private int monthOrderCount;

    /* [수정] 월별 납부 내역 조회용 필드 */
    private String settlementMonth;
    private LocalDate requestedAt;
    private LocalDate settledAt;

    /* [수정] 사업자 정산 계좌 정보 조회/수정용 필드 */
    private long businessNo;
    private String businessName;
    private String bankName;
    private String accountNumber;
    private String accountHolder;

    public long getDailySales() {
        return dailySales;
    }

    public void setDailySales(long dailySales) {
        this.dailySales = dailySales;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getMonthSales() {
        return monthSales;
    }

    public void setMonthSales(long monthSales) {
        this.monthSales = monthSales;
    }

    public long getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(long feeAmount) {
        this.feeAmount = feeAmount;
    }

    public double getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(double feeRate) {
        this.feeRate = feeRate;
    }

    public String getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(String expectedDate) {
        this.expectedDate = expectedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getMonthOrderCount() {
        return monthOrderCount;
    }

    public void setMonthOrderCount(int monthOrderCount) {
        this.monthOrderCount = monthOrderCount;
    }

    public String getSettlementMonth() {
        return settlementMonth;
    }

    public void setSettlementMonth(String settlementMonth) {
        this.settlementMonth = settlementMonth;
    }

    public LocalDate getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDate requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDate getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(LocalDate settledAt) {
        this.settledAt = settledAt;
    }

    public long getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(long businessNo) {
        this.businessNo = businessNo;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
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
}