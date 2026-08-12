package com.project.oditji.member.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
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

/** 이름 동기화가 켜져 있지만 저장 이름이 이미 정상인 마지막 AND/OR 분기를 보완합니다. */
class SocialLoginCallbackSupportStoredNameOperandCoverageTest {

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
    void synchronizedLoginShouldKeepExistingNonBlankStoredMemberName() {
        MemberSocialJoinVO social = new MemberSocialJoinVO();
        social.setMemberNo(20L);
        social.setNickname("소셜닉");
        social.setMemberName(null);
        social.setProvider("NAVER");
        social.setRole("USER");

        MemberVO stored = new MemberVO();
        stored.setMemberNo(20L);
        stored.setMemberName("기존이름");
        stored.setAdultVerified("Y");

        when(platformService.countMemberPlatform(20L)).thenReturn(1);
        when(memberService.getMemberByNo(20L)).thenReturn(stored);

        LoginOptions options = new LoginOptions(
                true,
                "소셜회원",
                true,
                "회원 없음",
                "번호 오류",
                "저장 회원 오류");

        assertEquals(
                "redirect:/",
                support.completeLogin(
                        new SocialLoginResultVO(false, social),
                        request,
                        session,
                        redirectAttributes,
                        options));

        assertEquals("소셜닉", social.getMemberName());
        assertEquals("기존이름", stored.getMemberName());
    }
}
