package com.project.oditji.member.vo;

public class KakaoLoginResultVO {

    private boolean newMember;
    private MemberSocialJoinVO member;

    public KakaoLoginResultVO() {
    }

    public KakaoLoginResultVO(boolean newMember, MemberSocialJoinVO member) {
        this.newMember = newMember;
        this.member = member;
    }

    public boolean isNewMember() {
        return newMember;
    }

    public void setNewMember(boolean newMember) {
        this.newMember = newMember;
    }

    public MemberSocialJoinVO getMember() {
        return member;
    }

    public void setMember(MemberSocialJoinVO member) {
        this.member = member;
    }
}