package com.project.oditji.business.vo;

import java.util.Date;

import com.project.oditji.common.vo.BusinessBaseVO;

/**
 * 사업자 기본 정보와 등록증·국세청 확인 결과를 보관합니다.
 */
public class BusinessVO extends BusinessBaseVO {

    private String representativeName;
    private String openDate;
    private String licenseFilePath;
    private String ntsBusinessStatus;
    private Date ntsCheckedAt;
    private String rejectReason;

    public String getRepresentativeName() {
        return representativeName;
    }

    public void setRepresentativeName(String representativeName) {
        this.representativeName = representativeName;
    }

    public String getOpenDate() {
        return openDate;
    }

    public void setOpenDate(String openDate) {
        this.openDate = openDate;
    }

    public String getLicenseFilePath() {
        return licenseFilePath;
    }

    public void setLicenseFilePath(String licenseFilePath) {
        this.licenseFilePath = licenseFilePath;
    }

    public String getNtsBusinessStatus() {
        return ntsBusinessStatus;
    }

    public void setNtsBusinessStatus(String ntsBusinessStatus) {
        this.ntsBusinessStatus = ntsBusinessStatus;
    }

    public Date getNtsCheckedAt() {
        return ntsCheckedAt;
    }

    public void setNtsCheckedAt(Date ntsCheckedAt) {
        this.ntsCheckedAt = ntsCheckedAt;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}
