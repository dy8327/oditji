package com.project.oditji.admin.vo;

/**
 * 상품 관리 화면 상단 통계 카드용 VO
 * (전체 상품 요청 / 승인 대기 / 승인 완료 / 삭제 요청 수)
 *
 * eventManage.jsp의 EventStatVO와 동일한 구조로,
 * PRODUCT.STATUS 값을 기준으로 집계한다.
 */
public class ProductStatVO {

    private long totalCount;
    private long waitingCount;
    private long approvedCount;
    private long deleteRequestedCount;

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getWaitingCount() {
        return waitingCount;
    }

    public void setWaitingCount(long waitingCount) {
        this.waitingCount = waitingCount;
    }

    public long getApprovedCount() {
        return approvedCount;
    }

    public void setApprovedCount(long approvedCount) {
        this.approvedCount = approvedCount;
    }

    public long getDeleteRequestedCount() {
        return deleteRequestedCount;
    }

    public void setDeleteRequestedCount(long deleteRequestedCount) {
        this.deleteRequestedCount = deleteRequestedCount;
    }
}
