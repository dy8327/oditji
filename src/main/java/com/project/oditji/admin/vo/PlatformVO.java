package com.project.oditji.admin.vo;

import com.project.oditji.common.vo.PlatformBaseVO;

/**
 * OTT 플랫폼 VO (테이블: OTT_PLATFORM)
 * contentManage.jsp의 플랫폼 등록·관리 기능에서 사용합니다.
 */
public class PlatformVO extends PlatformBaseVO {

    private Long platformNo;

    public Long getPlatformNo() {
        return platformNo;
    }

    public void setPlatformNo(Long platformNo) {
        this.platformNo = platformNo;
    }
}
