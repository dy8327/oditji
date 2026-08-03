package com.project.oditji.member.support;

import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Google, Kakao, Naver 로그인 컨트롤러가 공통으로 사용하는 세션 저장 로직입니다.
 */
public final class SocialLoginSessionSupport {

    private SocialLoginSessionSupport() {
    }

    public static String resolveDisplayName(
            MemberSocialJoinVO member,
            boolean nicknameFirst,
            String fallbackName) {

        String displayName = nicknameFirst
                ? member.getNickname()
                : member.getMemberName();

        if (displayName == null || displayName.isBlank()) {
            displayName = nicknameFirst
                    ? member.getMemberName()
                    : member.getNickname();
        }

        if (displayName == null || displayName.isBlank()) {
            displayName = fallbackName;
        }

        return displayName;
    }

    public static void savePendingSession(
            HttpSession session,
            MemberSocialJoinVO member,
            String displayName) {

        session.setAttribute("pendingMemberNo", member.getMemberNo());
        session.setAttribute("pendingMemberId", member.getMemberId());
        session.setAttribute("pendingMemberName", member.getMemberName());
        session.setAttribute("pendingNickname", member.getNickname());
        session.setAttribute("pendingRole", member.getRole());
        session.setAttribute("pendingProvider", member.getProvider());
        session.setAttribute("pendingDisplayName", displayName);
        session.setAttribute("pendingProfileImage", member.getProfileImage());
    }

    public static void saveLoginSession(
            HttpSession session,
            MemberVO loginMember,
            String provider,
            String displayName) {

        session.setAttribute("loginMember", loginMember);
        session.setAttribute("memberNo", loginMember.getMemberNo());
        session.setAttribute("memberId", loginMember.getMemberId());
        session.setAttribute("memberName", loginMember.getMemberName());
        session.setAttribute("nickname", loginMember.getNickname());
        session.setAttribute("role", loginMember.getRole());
        session.setAttribute("profileImage", loginMember.getProfileImage());

        session.setAttribute("loginMemberNo", loginMember.getMemberNo());
        session.setAttribute("loginMemberId", loginMember.getMemberId());
        session.setAttribute("loginMemberName", loginMember.getMemberName());
        session.setAttribute("loginNickname", loginMember.getNickname());
        session.setAttribute("loginRole", loginMember.getRole());
        session.setAttribute("loginProvider", provider);
        session.setAttribute("loginDisplayName", displayName);

        String adultVerified = loginMember.getAdultVerified();
        if (adultVerified == null || adultVerified.isBlank()) {
            adultVerified = "N";
        }

        session.setAttribute("ADULT_VERIFIED", adultVerified);
    }
    public static void saveRestoreSession(
            HttpServletRequest request,
            HttpSession session,
            long memberNo,
            String provider) {

        request.changeSessionId();
        session.setAttribute("restoreMemberNo", memberNo);
        session.setAttribute("restoreProvider", provider);
    }

}
