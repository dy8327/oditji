package com.project.oditji.favorite.vo;

import java.time.LocalDateTime;

public class FavoriteVO {

    private Long favoriteNo;
    private Long memberNo;
    private Long contentNo;
    private Long tmdbId;
    private String contentType;
    private LocalDateTime createdAt;

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
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