package com.project.oditji.member.vo;

/**
 * Google 로그인 처리 결과를 전달하는 VO입니다.
 *
 * 신규 소셜 회원인지 여부와 MEMBER + MEMBER_SOCIAL 조인 결과를
 * 컨트롤러에 함께 전달합니다.
 */
public class GoogleLoginResultVO {

    private boolean newMember;
    private MemberSocialJoinVO member;

    public GoogleLoginResultVO() {
    }

    public GoogleLoginResultVO(
            boolean newMember,
            MemberSocialJoinVO member) {

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
