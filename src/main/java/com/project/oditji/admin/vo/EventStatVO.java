package com.project.oditji.admin.vo;

/**
 * 이벤트 관리 화면 상단 통계 카드용 VO
 * (전체 이벤트 / 승인 대기 / 승인 완료 / 반려 / 진행중 / 예정 / 종료 이벤트 수)
 *
 * [추가] 기존에는 승인상태(대기/승인/종료) 3가지만 세고 있었는데, 목록 화면에서
 * "승인상태"(대기/승인/반려)와 "현재상태"(진행중/예정/종료)를 별도 컬럼으로
 * 나눈 뒤에도 상단 카드는 그대로였다. 반려/진행중/예정 카드를 추가로 노출하기
 * 위해 rejectedCount/ongoingCount/upcomingCount를 추가한다.
 *
 * ongoingCount/upcomingCount는 EVENT.STATUS 원본값만으로는 구분할 수 없다
 * (EventStatusScheduler가 하루 한 번만 END로 갱신하므로, 승인된 이벤트는
 * 시작 전이든 진행중이든 END_DATE가 지났든 STATUS='APPROVED'일 수 있다).
 * 그래서 EventManageVO.getProgressStatus()와 동일하게 START_DATE/END_DATE를
 * SYSDATE와 직접 비교해서 계산한 값을 adminMapper.xml의 selectEventStats가
 * 내려준다.
 */
public class EventStatVO extends ApprovalStatVO {

    private long rejectedCount;
    private long ongoingCount;
    private long upcomingCount;
    private long endCount;

    public long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public long getOngoingCount() {
        return ongoingCount;
    }

    public void setOngoingCount(long ongoingCount) {
        this.ongoingCount = ongoingCount;
    }

    public long getUpcomingCount() {
        return upcomingCount;
    }

    public void setUpcomingCount(long upcomingCount) {
        this.upcomingCount = upcomingCount;
    }

    public long getEndCount() {
        return endCount;
    }

    public void setEndCount(long endCount) {
        this.endCount = endCount;
    }
}
