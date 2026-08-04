package com.project.oditji.member.vo;

import java.util.Date;

import com.project.oditji.common.vo.PlatformBaseVO;

/**
 * 회원의 OTT 선택 화면에서 사용하는 플랫폼 정보입니다.
 */
public class PlatformVO extends PlatformBaseVO {

    private Long platformNo;
    private Date createdAt;
    private Date updatedAt;

    public Long getPlatformNo() {
        return platformNo;
    }

    public void setPlatformNo(Long platformNo) {
        this.platformNo = platformNo;
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
