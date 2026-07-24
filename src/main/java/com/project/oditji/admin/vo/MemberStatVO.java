package com.project.oditji.admin.vo;

/**
 * 회원 관리 화면 상단 통계 카드용 VO
 * (총 회원 / 정상 회원 / 정지 회원 / 탈퇴 회원 수)
 */
public class MemberStatVO {

    private long totalCount;
    private long activeCount;
    private long blockedCount;
    private long withdrawnCount;

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(long activeCount) {
        this.activeCount = activeCount;
    }

    public long getBlockedCount() {
        return blockedCount;
    }

    public void setBlockedCount(long blockedCount) {
        this.blockedCount = blockedCount;
    }

    public long getWithdrawnCount() {
        return withdrawnCount;
    }

    public void setWithdrawnCount(long withdrawnCount) {
        this.withdrawnCount = withdrawnCount;
    }
}
