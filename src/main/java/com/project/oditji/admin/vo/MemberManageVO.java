package com.project.oditji.admin.vo;

import java.util.Date;

public class MemberManageVO {

    private Long memberNo;
    private String memberId;
    private String memberName;
    private String nickname;
    private String email;
    private String phone;
    private String role;        // USER, BUSINESS, ADMIN
    private String status;      // ACTIVE, BLOCKED, WITHDRAWN
    private Date createdAt;
    private String snsYn;       // SNS 연동 계정 여부 (Y/N, MEMBER_SOCIAL 존재 여부)

    // 회원 본인이 직접 탈퇴(STATUS = 'WITHDRAWN')한 시각. 그 외 상태에서는 NULL.
    private Date withdrawnAt;

    // WITHDRAWN 상태인 회원에 한해, 자동삭제까지 남은 일수(0 이상)를 서비스 계층에서 계산해 채운다.
    // 그 외 상태이거나 계산 전에는 NULL.
    private Integer remainingDeleteDays;

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getSnsYn() {
        return snsYn;
    }

    public void setSnsYn(String snsYn) {
        this.snsYn = snsYn;
    }

    public Date getWithdrawnAt() {
        return withdrawnAt;
    }

    public void setWithdrawnAt(Date withdrawnAt) {
        this.withdrawnAt = withdrawnAt;
    }

    public Integer getRemainingDeleteDays() {
        return remainingDeleteDays;
    }

    public void setRemainingDeleteDays(Integer remainingDeleteDays) {
        this.remainingDeleteDays = remainingDeleteDays;
    }
}
