package com.project.oditji.favorite.vo;

import java.util.Date;

public class FavoriteVO {

    private Long favoriteNo;
    private Long memberNo;
    private Long contentNo;
    private Long tmdbId;
    private String contentType;
    private Date createdAt;

    public Long getFavoriteNo() {
        return favoriteNo;
    }

    public void setFavoriteNo(Long favoriteNo) {
        this.favoriteNo = favoriteNo;
    }

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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "FavoriteVO [favoriteNo=" + favoriteNo
                + ", memberNo=" + memberNo
                + ", contentNo=" + contentNo
                + ", tmdbId=" + tmdbId
                + ", contentType=" + contentType
                + ", createdAt=" + createdAt + "]";
    }
}