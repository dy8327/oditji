package com.project.oditji.admin.vo;

import java.util.Date;

import com.project.oditji.common.vo.ContentMetadataVO;

/**
 * 콘텐츠 관리 VO (테이블: CONTENT, 제공 OTT는 CONTENT_PLATFORM 조인 집계)
 */
public class ContentManageVO extends ContentMetadataVO {

    private Long contentNo;
    private Date releaseDate;
    private Long viewCount;
    private String platformNames;
    private Date createdAt;
    private Date updatedAt;

    public Long getContentNo() {
        return contentNo;
    }

    public void setContentNo(Long contentNo) {
        this.contentNo = contentNo;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
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
