package com.project.oditji.content.vo;

import java.util.Date;

/**
 * 회원의 콘텐츠 상세페이지 조회 이력을 담는 VO입니다.
 *
 * CONTENT_VIEW_HISTORY 테이블은
 * 회원 + 콘텐츠 + 조회 날짜별로 한 행을 유지합니다.
 *
 * 같은 회원이 같은 날 같은 콘텐츠를 다시 조회하면
 * 새로운 행을 추가하지 않고 VIEW_COUNT를 증가시킵니다.
 */
public class ContentViewHistoryVO {

    private Long viewHistoryNo;
    private Long memberNo;
    private int contentNo;
    private int viewCount;
    private Date viewDate;
    private Date firstViewedAt;
    private Date lastViewedAt;

    public Long getViewHistoryNo() {
        return viewHistoryNo;
    }

    public void setViewHistoryNo(
            Long viewHistoryNo) {

        this.viewHistoryNo = viewHistoryNo;
    }

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(
            Long memberNo) {

        this.memberNo = memberNo;
    }

    public int getContentNo() {
        return contentNo;
    }

    public void setContentNo(
            int contentNo) {

        this.contentNo = contentNo;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(
            int viewCount) {

        this.viewCount = viewCount;
    }

    public Date getViewDate() {
        return viewDate;
    }

    public void setViewDate(
            Date viewDate) {

        this.viewDate = viewDate;
    }

    public Date getFirstViewedAt() {
        return firstViewedAt;
    }

    public void setFirstViewedAt(
            Date firstViewedAt) {

        this.firstViewedAt = firstViewedAt;
    }

    public Date getLastViewedAt() {
        return lastViewedAt;
    }

    public void setLastViewedAt(
            Date lastViewedAt) {

        this.lastViewedAt = lastViewedAt;
    }
}