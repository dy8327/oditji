package com.project.oditji.admin.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.project.oditji.common.vo.ContentMetadataVO;

/**
 * 콘텐츠 관리 VO (테이블: CONTENT, 제공 OTT는 CONTENT_PLATFORM 조인 집계)
 */
public class ContentManageVO extends ContentMetadataVO {

    private Long contentNo;
    private LocalDate releaseDate;
    private Long viewCount;
    private String platformNames;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getContentNo() {
        return contentNo;
    }

    public void setContentNo(Long contentNo) {
        this.contentNo = contentNo;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    /**
     * CONTENT_PLATFORM과 OTT_PLATFORM을 LISTAGG로 조회한 플랫폼 이름입니다.
     */
    public String getPlatformNames() {
        return platformNames;
    }

    public void setPlatformNames(String platformNames) {
        this.platformNames = platformNames;
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
