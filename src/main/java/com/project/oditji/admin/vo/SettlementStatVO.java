package com.project.oditji.admin.vo;

/**
 * 정산 관리 화면 상단 통계 카드용 VO
 * (전체 정산 건 / 입금 대기 / 입금 완료 / 반려 건수)
 *
 * selectSettlementList와 동일하게 사업자/정산월 단위로 묶은 건 수를 기준으로 한다.
 */
public class SettlementStatVO {

    private long totalCount;
    private long requestedCount;
    private long doneCount;
    private long rejectedCount;

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getRequestedCount() {
        return requestedCount;
    }

    public void setRequestedCount(long requestedCount) {
        this.requestedCount = requestedCount;
    }

    public long getDoneCount() {
        return doneCount;
    }

    public void setDoneCount(long doneCount) {
        this.doneCount = doneCount;
    }

    public long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }
}
