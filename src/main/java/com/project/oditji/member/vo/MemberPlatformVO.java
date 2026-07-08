package com.project.oditji.member.vo;

import java.util.Date;

public class MemberPlatformVO {

    private int memberPlatformNo;
    private Long memberNo;
    private Long platformNo;
    private Date createdAt;

    public int getMemberPlatformNo() {
        return memberPlatformNo;
    }

    public void setMemberPlatformNo(int memberPlatformNo) {
        this.memberPlatformNo = memberPlatformNo;
    }

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

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
}