package com.project.oditji.member.vo;

import java.time.LocalDateTime;

public class MemberSocialVO {

    private int socialNo;
    private long memberNo;
    private String provider;
    private String providerUserId;
    private LocalDateTime createdAt;

    public int getSocialNo() {
        return socialNo;
    }

    public void setSocialNo(int socialNo) {
        this.socialNo = socialNo;
    }

    public long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(long memberNo) {
        this.memberNo = memberNo;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}