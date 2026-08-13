package com.project.oditji.tmdb.vo;

import com.project.oditji.common.vo.PlatformBaseVO;

/**
 * 콘텐츠에 연결된 OTT 플랫폼 조회 정보입니다.
 */
public class OttPlatformVO extends PlatformBaseVO {

    private Integer platformNo;

    public Integer getPlatformNo() {
        return platformNo;
    }

    public void setPlatformNo(Integer platformNo) {
        this.platformNo = platformNo;
    }
}
