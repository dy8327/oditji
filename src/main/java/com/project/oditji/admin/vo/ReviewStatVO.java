package com.project.oditji.admin.vo;

/**
 * 리뷰 관리 화면 상단 통계 카드용 VO
 * (전체 리뷰 수 / 신고 접수(WAITING) 건수)
 *
 * reviewManage.jsp(콘텐츠 리뷰)와 productReviewManage.jsp(상품 리뷰)에서
 * 각각 별도로 조회하여 공용으로 사용한다.
 * (eventManage.jsp의 EventStatVO, productManage.jsp의 ProductStatVO와 동일한 구조)
 */
public class ReviewStatVO {

    private long totalCount;
    private long reportCount;

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getReportCount() {
        return reportCount;
    }

    public void setReportCount(long reportCount) {
        this.reportCount = reportCount;
    }
}
