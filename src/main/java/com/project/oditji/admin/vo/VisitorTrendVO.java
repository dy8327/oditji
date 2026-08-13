package com.project.oditji.admin.vo;

/**
 * 모니터링 화면 - 최근 7일 방문자 추이 차트용 VO
 * 테이블: ACCESS_LOG (일자별 DISTINCT MEMBER_NO 카운트)
 */
public class VisitorTrendVO {

    private String accessDate;   // YYYY-MM-DD
    private Long visitorCount;   // 해당 일자 방문 회원 수 (distinct)

    public String getAccessDate() {
        return accessDate;
    }

    public void setAccessDate(String accessDate) {
        this.accessDate = accessDate;
    }

    public Long getVisitorCount() {
        return visitorCount;
    }

    public void setVisitorCount(Long visitorCount) {
        this.visitorCount = visitorCount;
    }
}
