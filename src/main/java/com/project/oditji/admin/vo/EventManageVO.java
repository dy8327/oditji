package com.project.oditji.admin.vo;

import java.util.Calendar;
import java.util.Date;

import com.project.oditji.common.vo.EventBaseVO;

/**
 * 이벤트 관리 VO (EVENT + EVENT_PRODUCT + PRODUCT + BUSINESS 조회 결과).
 */
public class EventManageVO extends EventBaseVO {

    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private String businessName;
    private Date startDate;
    private Date endDate;
    private Long productNo;
    private String productName;
    private Long price;
    private Integer eventDiscountRate;
    private String productDetail;

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
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

    public String getProductDetail() {
        return productDetail;
    }

    public void setProductDetail(String productDetail) {
        this.productDetail = productDetail;
    }

    public Long getDiscountedPrice() {
        if (price == null || eventDiscountRate == null) {
            return null;
        }

        return Math.round(price * (100 - eventDiscountRate) / 100.0);
    }

    public String getApprovalStatus() {
        String status = getStatus();

        if (STATUS_REJECTED.equals(status)) {
            return STATUS_REJECTED;
        }

        if (STATUS_APPROVED.equals(status) || "END".equals(status)) {
            return STATUS_APPROVED;
        }

        return "WAITING";
    }

    public String getApprovalStatusLabel() {
        return switch (getApprovalStatus()) {
            case STATUS_APPROVED -> "승인";
            case STATUS_REJECTED -> "반려";
            default -> "대기";
        };
    }

    public String getProgressStatus() {
        if (!STATUS_APPROVED.equals(getApprovalStatus()) || startDate == null || endDate == null) {
            return null;
        }

        Date today = truncateToDate(new Date());
        Date start = truncateToDate(startDate);
        Date end = truncateToDate(endDate);

        if (today.before(start)) {
            return "UPCOMING";
        }

        if (today.after(end)) {
            return "ENDED";
        }

        return "ONGOING";
    }

    public String getProgressStatusLabel() {
        String progress = getProgressStatus();

        if (progress == null) {
            return "-";
        }

        return switch (progress) {
            case "ONGOING" -> "진행중";
            case "UPCOMING" -> "예정";
            default -> "종료";
        };
    }

    private Date truncateToDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
}
