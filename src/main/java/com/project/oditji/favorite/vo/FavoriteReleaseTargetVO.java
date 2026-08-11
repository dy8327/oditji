package com.project.oditji.favorite.vo;

/**
 * 찜한 콘텐츠의 공개일이 다가오는 회원에게 알림을 보내기 위한 조회 결과입니다.
 *
 * FAVORITE와 CONTENT를 조인해, 특정 날짜(예: 내일)에
 * 개봉·공개하는 찜한 콘텐츠와 그 콘텐츠를 찜한 회원 정보를 담습니다.
 */
public class FavoriteReleaseTargetVO {

    private Long memberNo;
    private Long contentNo;
    private Long tmdbId;
    private String contentType;
    private String title;

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
}
