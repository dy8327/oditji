package com.project.oditji.member.vo;

import java.util.Date;

public class MemberSocialJoinVO {

    private long memberNo;
    private String memberId;
    private String memberName;
    private String nickname;
    private String email;
    private String phone;
    private String profileImage;
    private String role;
    private String status;
    private Date withdrawnAt;

    private int socialNo;
    private String provider;
    private String providerUserId;
    private Date createdAt;

    public long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(long memberNo) {
        this.memberNo = memberNo;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 탈퇴 시각.
     * STATUS가 WITHDRAWN일 때 복구 가능 기한(7일) 계산에 사용된다.
     */
    public Date getWithdrawnAt() {
        return withdrawnAt;
    }

    public void setWithdrawnAt(Date withdrawnAt) {
        this.withdrawnAt = withdrawnAt;
    }

    public int getSocialNo() {
        return socialNo;
    }

    public void setSocialNo(int socialNo) {
        this.socialNo = socialNo;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
