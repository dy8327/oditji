package com.project.oditji.member.vo;

import java.time.LocalDateTime;

import com.project.oditji.common.vo.PlatformBaseVO;

/**
 * 회원의 OTT 선택 화면에서 사용하는 플랫폼 정보입니다.
 */
public class PlatformVO extends PlatformBaseVO {

    private Long platformNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getPlatformNo() {
        return platformNo;
    }

    public void setPlatformNo(Long platformNo) {
        this.platformNo = platformNo;
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
