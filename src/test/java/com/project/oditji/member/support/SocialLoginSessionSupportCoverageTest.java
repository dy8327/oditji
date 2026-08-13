package com.project.oditji.member.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 소셜 로그인 공통 세션 helper의 표시명 fallback과 성인인증 기본값 분기를 검증합니다.
 */
class SocialLoginSessionSupportCoverageTest {

    @Test
    void displayNameShouldCoverPrimarySecondaryAndFallbackNames() {
        MemberSocialJoinVO member = new MemberSocialJoinVO();

        member.setNickname("닉네임");
        member.setMemberName("이름");

        assertEquals(
                "닉네임",
                SocialLoginSessionSupport.resolveDisplayName(
                        member,
                        true,
                        "기본값"));

        assertEquals(
                "이름",
                SocialLoginSessionSupport.resolveDisplayName(
                        member,
                        false,
                        "기본값"));

        member.setNickname("   ");

        assertEquals(
                "이름",
                SocialLoginSessionSupport.resolveDisplayName(
                        member,
                        true,
                        "기본값"));

        member.setNickname(null);
        member.setMemberName(" ");

        assertEquals(
                "기본값",
                SocialLoginSessionSupport.resolveDisplayName(
                        member,
                        true,
                        "기본값"));
    }

    @Test
    void pendingSessionShouldSaveAllSocialMemberValues() {
        MemberSocialJoinVO member = new MemberSocialJoinVO();
        member.setMemberNo(10L);
        member.setMemberId("social-10");
        member.setMemberName("회원명");
        member.setNickname("닉네임");
        member.setRole("USER");
        member.setProvider("KAKAO");
        member.setProfileImage("/profile.png");

        MockHttpSession session = new MockHttpSession();

        SocialLoginSessionSupport.savePendingSession(
                session,
                member,
                "표시명");

        assertEquals(10L, session.getAttribute("pendingMemberNo"));
        assertEquals("social-10", session.getAttribute("pendingMemberId"));
        assertEquals("회원명", session.getAttribute("pendingMemberName"));
        assertEquals("닉네임", session.getAttribute("pendingNickname"));
        assertEquals("USER", session.getAttribute("pendingRole"));
        assertEquals("KAKAO", session.getAttribute("pendingProvider"));
        assertEquals("표시명", session.getAttribute("pendingDisplayName"));
        assertEquals("/profile.png", session.getAttribute("pendingProfileImage"));
    }

    @Test
    void loginSessionShouldDefaultNullAndBlankAdultVerifiedToN() {
        MemberVO member = loginMember();
        MockHttpSession session = new MockHttpSession();

        member.setAdultVerified(null);

        SocialLoginSessionSupport.saveLoginSession(
                session,
                member,
                "GOOGLE",
                "표시명");

        assertEquals("N", session.getAttribute("ADULT_VERIFIED"));

        member.setAdultVerified("   ");

        SocialLoginSessionSupport.saveLoginSession(
                session,
                member,
                "GOOGLE",
                "표시명");

        assertEquals("N", session.getAttribute("ADULT_VERIFIED"));

        member.setAdultVerified("Y");

        SocialLoginSessionSupport.saveLoginSession(
                session,
                member,
                "GOOGLE",
                "표시명");

        assertEquals("Y", session.getAttribute("ADULT_VERIFIED"));
    }

    @Test
    void restoreSessionShouldChangeSessionIdAndSaveRestoreValues() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);

        SocialLoginSessionSupport.saveRestoreSession(
                request,
                session,
                20L,
                "NAVER");

        verify(request).changeSessionId();
        verify(session).setAttribute("restoreMemberNo", 20L);
        verify(session).setAttribute("restoreProvider", "NAVER");
    }

    private MemberVO loginMember() {
        MemberVO member = new MemberVO();
        member.setMemberNo(1L);
        member.setMemberId("member");
        member.setMemberName("회원명");
        member.setNickname("닉네임");
        member.setRole("USER");
        member.setProfileImage("/profile.png");
        return member;
    }
}
