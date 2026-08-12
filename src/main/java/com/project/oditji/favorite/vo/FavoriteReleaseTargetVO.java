package com.project.oditji.favorite.vo;

/**
 * 찜한 콘텐츠의 공개일이 다가오는 회원에게 알림을 보내기 위한 조회 결과입니다.
 *
 * FAVORITE와 CONTENT를 조인해, 특정 날짜(오늘 또는 내일)에
 * 개봉·공개하는 찜한 콘텐츠와 그 콘텐츠를 찜한 회원 정보를 담습니다.
 */
public class FavoriteReleaseTargetVO {

    private Long memberNo;
    private Long contentNo;
    private Long tmdbId;
    private String contentType;
    private String title;

    /**
     * 공개일까지 남은 일수입니다. 0이면 오늘 공개, 1이면 내일(하루 전) 공개를
     * 의미합니다. 조회 시점 기준으로 계산되어 내려오며, 알림 문구 분기에 사용합니다.
     */
    private Integer daysUntilRelease;

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public Long getContentNo() {
        return contentNo;
    }

    public void setContentNo(Long contentNo) {
        this.contentNo = contentNo;
    }

    public Long getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(Long tmdbId) {
        this.tmdbId = tmdbId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getDaysUntilRelease() {
        return daysUntilRelease;
    }

    public void setDaysUntilRelease(Integer daysUntilRelease) {
        this.daysUntilRelease = daysUntilRelease;
    }
}
