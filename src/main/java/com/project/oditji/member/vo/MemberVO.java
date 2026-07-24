package com.project.oditji.member.vo;

import java.util.Date;
import java.io.Serial;
import java.io.Serializable;

/**
 * 회원 정보를 전달하는 VO 클래스입니다.
 *
 * 로그인한 회원 객체가 HttpSession에 저장되며,
 * 인터셉터와 여러 컨트롤러에서 재사용되므로
 * 세션 직렬화를 지원하기 위해 Serializable을 구현합니다.
 */

public class MemberVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long memberNo;
    private String memberId;
    private String memberPw;
    private String memberName;
    private String nickname;
    private String email;
    private String phone;
    private String profileImage;
    private String role;
    private String status;
    private String adultVerified;
    private Date createdAt;
    private Date updatedAt;
    private Date withdrawnAt;

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

    public String getMemberPw() {
        return memberPw;
    }

    public void setMemberPw(String memberPw) {
        this.memberPw = memberPw;
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

    public String getAdultVerified() {
        return adultVerified;
    }

    public void setAdultVerified(String adultVerified) {
        this.adultVerified = adultVerified;
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

    public Date getWithdrawnAt() {
        return withdrawnAt;
    }

    public void setWithdrawnAt(Date withdrawnAt) {
        this.withdrawnAt = withdrawnAt;
    }
}
