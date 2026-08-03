package com.project.oditji.member.vo;

/**
 * 구글, 카카오, 네이버 소셜 로그인 처리 결과를 공통으로 전달합니다.
 *
 * 신규 자동가입 여부와 MEMBER_SOCIAL을 통해 조회한 최종 회원 정보를
 * 각 소셜 로그인 컨트롤러로 반환합니다.
 */
public class SocialLoginResultVO {

    private boolean newMember;
    private MemberSocialJoinVO member;

    public SocialLoginResultVO() {
    }

    public SocialLoginResultVO(boolean newMember, MemberSocialJoinVO member) {
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
