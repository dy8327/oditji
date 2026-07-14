package com.project.oditji.admin.vo;

/**
 * 관리자 메인 대시보드 통계 VO
 */
public class AdminVO {

    private long memberCount;            // 전체 회원 수 (MEMBER)
    private long contentCount;            // 전체 콘텐츠 수 (CONTENT)
    private long reviewCount;             // 전체 리뷰 수 (REVIEW + PRODUCT_REVIEW)
    private long reportCount;             // 처리 대기 신고 수 (REVIEW_REPORT, STATUS='WAITING')
    private long businessRequestCount;    // 사업자 입점 승인 대기 수 (BUSINESS, STATUS='WAITING')
    private long productRequestCount;     // 상품 등록 요청 대기 수 (PRODUCT, STATUS='WAITING')
    private long eventRequestCount;       // 이벤트 요청 대기 수 (EVENT, STATUS='WAITING')
    private long settlementWaitingCount;  // 입금 확인 대기 수 (SETTLEMENT, STATUS='WAITING')
    private long visitorCount;            // 방문자 수 (ACCESS_LOG 기준 금일 접속 회원 수)

    public long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(long memberCount) {
        this.memberCount = memberCount;
    }

    public long getContentCount() {
        return contentCount;
    }

    public void setContentCount(long contentCount) {
        this.contentCount = contentCount;
    }

    public long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(long reviewCount) {
        this.reviewCount = reviewCount;
    }

    public long getReportCount() {
        return reportCount;
    }

    public void setReportCount(long reportCount) {
        this.reportCount = reportCount;
    }

    public long getBusinessRequestCount() {
        return businessRequestCount;
    }

    public void setBusinessRequestCount(long businessRequestCount) {
        this.businessRequestCount = businessRequestCount;
    }

    public long getProductRequestCount() {
        return productRequestCount;
    }

    public void setProductRequestCount(long productRequestCount) {
        this.productRequestCount = productRequestCount;
    }

    public long getEventRequestCount() {
        return eventRequestCount;
    }

    public void setEventRequestCount(long eventRequestCount) {
        this.eventRequestCount = eventRequestCount;
    }

    public long getSettlementWaitingCount() {
        return settlementWaitingCount;
    }

    public void setSettlementWaitingCount(long settlementWaitingCount) {
        this.settlementWaitingCount = settlementWaitingCount;
    }

    public long getVisitorCount() {
        return visitorCount;
    }

    public void setVisitorCount(long visitorCount) {
        this.visitorCount = visitorCount;
    }

    @Override
    public String toString() {
        return "AdminVO{" +
                "memberCount=" + memberCount +
                ", contentCount=" + contentCount +
                ", reviewCount=" + reviewCount +
                ", reportCount=" + reportCount +
                ", businessRequestCount=" + businessRequestCount +
                ", productRequestCount=" + productRequestCount +
                ", eventRequestCount=" + eventRequestCount +
                ", settlementWaitingCount=" + settlementWaitingCount +
                ", visitorCount=" + visitorCount +
                '}';
    }
}
