package com.project.oditji.content.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.project.oditji.common.vo.ContentMetadataVO;

/**
 * CONTENT 테이블의 콘텐츠 정보입니다.
 */
public class ContentVO extends ContentMetadataVO {

    private int contentNo;
    private LocalDate releaseDate;
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
