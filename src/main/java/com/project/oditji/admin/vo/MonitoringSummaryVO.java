package com.project.oditji.admin.vo;

/**
 * 관리자 모니터링 화면 상단 요약 통계 VO
 *
 * - memberCount: 일반 사용자(USER) 회원 수
 * - recentVisitorCount: 최근 7일 접속한 일반 사용자 수 (ACCESS_LOG DISTINCT MEMBER_NO)
 * - deliveredOrderCount: 배송 완료된 주문 상세 건수
 * - deliveredSalesAmount: 배송 완료 주문의 실매출 합계 (승인 환불액 차감)
 */
public class MonitoringSummaryVO {

    private long memberCount;
    private long recentVisitorCount;
    private long deliveredOrderCount;
    private long deliveredSalesAmount;

    public long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(long memberCount) {
        this.memberCount = memberCount;
    }

    public long getRecentVisitorCount() {
        return recentVisitorCount;
    }

    public void setRecentVisitorCount(long recentVisitorCount) {
        this.recentVisitorCount = recentVisitorCount;
    }

    public long getDeliveredOrderCount() {
        return deliveredOrderCount;
    }

    public void setDeliveredOrderCount(long deliveredOrderCount) {
        this.deliveredOrderCount = deliveredOrderCount;
    }

    public long getDeliveredSalesAmount() {
        return deliveredSalesAmount;
    }

    public void setDeliveredSalesAmount(long deliveredSalesAmount) {
        this.deliveredSalesAmount = deliveredSalesAmount;
    }
}