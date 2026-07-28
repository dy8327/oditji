package com.project.oditji.admin.vo;

/**
 * 사업자 관리 화면 상단 통계 카드용 VO
 * (입점 완료 사업자 / 승인 대기 사업자 수)
 */
public class BusinessStatVO {

    private long approvedCount;
    private long waitingCount;

    public long getApprovedCount() {
        return approvedCount;
    }

    public void setApprovedCount(long approvedCount) {
        this.approvedCount = approvedCount;
    }

    public long getWaitingCount() {
        return waitingCount;
    }

    public void setWaitingCount(long waitingCount) {
        this.waitingCount = waitingCount;
    }
}
