package com.project.oditji.member.vo;

/**
 * 네이버 로그인 처리 결과 VO입니다.
 *
 * 신규 자동가입 여부와 MEMBER_SOCIAL을 통해 조회한
 * 최종 회원 정보를 컨트롤러로 전달합니다.
 */
public class NaverLoginResultVO {

    private boolean newMember;
    private MemberSocialJoinVO member;

    public NaverLoginResultVO() {
    }

    public NaverLoginResultVO(boolean newMember, MemberSocialJoinVO member) {
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
