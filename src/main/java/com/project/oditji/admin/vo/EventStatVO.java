package com.project.oditji.admin.vo;

/**
 * 이벤트 관리 화면 상단 통계 카드용 VO
 * (전체 이벤트 / 승인 대기 / 승인 완료 / 종료 이벤트 수)
 */
public class EventStatVO {

    private long totalCount;
    private long waitingCount;
    private long approvedCount;
    private long endCount;

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

    public long getEndCount() {
        return endCount;
    }

    public void setEndCount(long endCount) {
        this.endCount = endCount;
    }
}
