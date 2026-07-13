package com.project.oditji.favorite.vo;

import java.util.Date;

public class FavoriteVO {

    private Long favoriteNo;
    private Long memberNo;
    private Long contentNo;
    private Date createdAt;

    public FavoriteVO() {
    }

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
                + ", createdAt=" + createdAt + "]";
    }
}