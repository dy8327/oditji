package com.project.oditji.member.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.support.SocialLoginCallbackSupport.LoginOptions;
import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.SocialLoginResultVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** 소셜 로그인 공통 콜백의 남은 OR 조건과 이름 동기화 조건을 보완합니다. */
class SocialLoginCallbackSupportResidualConditionCoverageTest {

    private MemberPlatformService platformService;
    private MemberService memberService;
    private SocialLoginCallbackSupport support;
    private HttpServletRequest request;
    private HttpSession session;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        platformService = mock(MemberPlatformService.class);
        memberService = mock(MemberService.class);
        support = new SocialLoginCallbackSupport(platformService, memberService);
        request = mock(HttpServletRequest.class);
        session = mock(HttpSession.class);
        redirectAttributes = mock(RedirectAttributes.class);
    }

    @Test
    void nonNullResultWithNullMemberShouldCoverSecondOrOperandAndNullMessage() {
        SocialLoginResultVO result = new SocialLoginResultVO(false, null);
        LoginOptions options = options(null, "번호 오류", "저장 오류", false);

        assertEquals(
                "redirect:/member/login",
                support.completeLogin(result, request, session, redirectAttributes, options));

        verify(redirectAttributes, never()).addFlashAttribute(anyString(), anyString());
    }

    @Test
    void synchronizedStoredNameNullShouldCoverFirstInnerOrOperand() {
        MemberSocialJoinVO social = socialMember(10L, "표시닉", null);
        MemberVO stored = new MemberVO();
        stored.setMemberNo(10L);
        stored.setMemberName(null);

        when(platformService.countMemberPlatform(10L)).thenReturn(1);
        when(memberService.getMemberByNo(10L)).thenReturn(stored);

        assertEquals(
                "redirect:/",
                support.completeLogin(
                        new SocialLoginResultVO(false, social),
                        request,
                        session,
                        redirectAttributes,
                        options("회원 없음", "번호 오류", "저장 오류", true)));

        assertEquals("표시닉", stored.getMemberName());
    }

    @Test
    void invalidMemberWithBlankMessageShouldCoverNonNullBlankMessageOperand() {
        MemberSocialJoinVO social = socialMember(0L, "닉", "이름");
        LoginOptions options = options("회원 없음", "   ", "저장 오류", false);

        assertEquals(
                "redirect:/member/login",
                support.completeLogin(
                        new SocialLoginResultVO(false, social),
                        request,
                        session,
                        redirectAttributes,
                        options));

        verify(redirectAttributes, never()).addFlashAttribute("errorMessage", "   ");
    }

    private LoginOptions options(
            String missingMemberMessage,
            String invalidMemberMessage,
            String missingStoredMessage,
            boolean synchronizeName) {

        return new LoginOptions(
                true,
                "소셜회원",
                synchronizeName,
                missingMemberMessage,
                invalidMemberMessage,
                missingStoredMessage);
    }

    private MemberSocialJoinVO socialMember(Long memberNo, String nickname, String name) {
        MemberSocialJoinVO member = new MemberSocialJoinVO();
        member.setMemberNo(memberNo);
        member.setNickname(nickname);
        member.setMemberName(name);
        member.setProvider("GOOGLE");
        member.setRole("USER");
        return member;
    }
}
