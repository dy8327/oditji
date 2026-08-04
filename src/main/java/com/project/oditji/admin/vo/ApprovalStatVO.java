package com.project.oditji.admin.vo;

/**
 * 관리자 승인형 목록의 공통 통계 값입니다.
 *
 * 상품과 이벤트 관리 통계가 공통으로 사용하는 전체·대기·승인 건수를
 * 한 곳에서 관리하여 동일한 JavaBean 접근자를 반복하지 않습니다.
 */
public abstract class ApprovalStatVO {

    private long totalCount;
    private long waitingCount;
    private long approvedCount;

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
}
