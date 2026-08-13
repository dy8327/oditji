package com.project.oditji.admin.vo;

/**
 * 주문 관리 화면 상단 통계 카드용 VO
 * (전체 주문 / 전체 환불 요청 / 환불 대기 / 환불 완료 건수)
 */
public class OrderStatVO {

    private long totalOrderCount;
    private long refundTotalCount;
    private long refundWaitingCount;
    private long refundApprovedCount;

    public long getTotalOrderCount() {
        return totalOrderCount;
    }

    public void setTotalOrderCount(long totalOrderCount) {
        this.totalOrderCount = totalOrderCount;
    }

    public long getRefundTotalCount() {
        return refundTotalCount;
    }

    public void setRefundTotalCount(long refundTotalCount) {
        this.refundTotalCount = refundTotalCount;
    }

    public long getRefundWaitingCount() {
        return refundWaitingCount;
    }

    public void setRefundWaitingCount(long refundWaitingCount) {
        this.refundWaitingCount = refundWaitingCount;
    }

    public long getRefundApprovedCount() {
        return refundApprovedCount;
    }

    public void setRefundApprovedCount(long refundApprovedCount) {
        this.refundApprovedCount = refundApprovedCount;
    }
}
