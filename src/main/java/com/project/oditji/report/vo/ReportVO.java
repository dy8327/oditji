package com.project.oditji.report.vo;

import java.util.Date;

/**
 * REVIEW_REPORT 테이블 매핑 VO.
 * 콘텐츠 리뷰 / 상품 리뷰 신고 등록, 조회에 사용한다.
 * reviewType 이 "CONTENT" 이면 contentReviewNo,
 * "PRODUCT" 이면 productReviewNo 가 채워진다.
 */
public class ReportVO {

    private int reportNo;
    private Long memberNo;
    private String reviewType;
    private Integer contentReviewNo;
    private Integer productReviewNo;
    private String reason;
    private String detail;
    private String status;
    private Long adminNo;
    private String adminMemo;
    private Date createdAt;
    private Date processedAt;
    private Date updatedAt;

    public ReportVO() {
    }

    public int getReportNo() {
        return reportNo;
    }

    public void setReportNo(int reportNo) {
        this.reportNo = reportNo;
    }

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public String getReviewType() {
        return reviewType;
    }

    public void setReviewType(String reviewType) {
        this.reviewType = reviewType;
    }

    public Integer getContentReviewNo() {
        return contentReviewNo;
    }

    public void setContentReviewNo(Integer contentReviewNo) {
        this.contentReviewNo = contentReviewNo;
    }

    public Integer getProductReviewNo() {
        return productReviewNo;
    }

    public void setProductReviewNo(Integer productReviewNo) {
        this.productReviewNo = productReviewNo;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAdminNo() {
        return adminNo;
    }

    public void setAdminNo(Long adminNo) {
        this.adminNo = adminNo;
    }

    public String getAdminMemo() {
        return adminMemo;
    }

    public void setAdminMemo(String adminMemo) {
        this.adminMemo = adminMemo;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Date processedAt) {
        this.processedAt = processedAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
