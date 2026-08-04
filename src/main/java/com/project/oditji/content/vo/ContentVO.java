package com.project.oditji.content.vo;

import java.time.LocalDate;
import java.util.Date;

import com.project.oditji.common.vo.ContentMetadataVO;

/**
 * CONTENT 테이블의 콘텐츠 정보입니다.
 */
public class ContentVO extends ContentMetadataVO {

    private int contentNo;
    private LocalDate releaseDate;
    private int viewCount;
    private Date createdAt;
    private Date updatedAt;

    public int getContentNo() {
        return contentNo;
    }

    public void setContentNo(int contentNo) {
        this.contentNo = contentNo;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
