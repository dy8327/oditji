package com.project.oditji.member.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.support.SocialLoginCallbackSupport.LoginOptions;
import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.SocialLoginResultVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 소셜 로그인 공통 콜백의 회원 검증, OTT 선택, 이름 동기화 조건을 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class SocialLoginCallbackSupportRemainingCoverageTest {

    @Mock
    private MemberPlatformService memberPlatformService;

    @Mock
    private MemberService memberService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private RedirectAttributes redirectAttributes;

    private SocialLoginCallbackSupport support;

    @BeforeEach
    void setUp() {
        support = new SocialLoginCallbackSupport(
                memberPlatformService,
                memberService);
    }

    @Test
    void missingResultAndBlankMessageShouldReturnLoginWithoutFlashMessage() {
        LoginOptions options = options(
                true,
                false,
                "   ",
                "번호 오류",
                "저장 회원 오류");

        assertEquals(
                "redirect:/member/login",
                support.completeLogin(
                        null,
                        request,
                        session,
                        redirectAttributes,
                        options));

        verify(redirectAttributes, never())
                .addFlashAttribute(
                        anyString(),
                        anyString());
    }

    @Test
    void resultWithoutMemberAndInvalidMemberNumberShouldUseConfiguredMessages() {
        LoginOptions options = options(
                true,
                false,
                "회원 없음",
                "번호 오류",
                "저장 회원 오류");

        SocialLoginResultVO noMember =
                new SocialLoginResultVO(false, null);

        assertEquals(
                "redirect:/member/login",
                support.completeLogin(
                        noMember,
                        request,
                        session,
                        redirectAttributes,
                        options));

        MemberSocialJoinVO invalidMember = socialMember(
                0L,
                "닉네임",
                "이름",
                "GOOGLE");

        assertEquals(
                "redirect:/member/login",
                support.completeLogin(
                        new SocialLoginResultVO(false, invalidMember),
                        request,
                        session,
                        redirectAttributes,
                        options));

        verify(redirectAttributes)
                .addFlashAttribute(
                        "errorMessage",
                        "회원 없음");
        verify(redirectAttributes)
                .addFlashAttribute(
                        "errorMessage",
                        "번호 오류");
    }

    @Test
    void newMemberShouldSavePendingSessionEvenWhenPlatformAlreadyExists() {
        MemberSocialJoinVO member = socialMember(
                3L,
                "신규닉네임",
                "신규이름",
                "NAVER");

        when(memberPlatformService.countMemberPlatform(3L))
                .thenReturn(2);

        String result = support.completeLogin(
                new SocialLoginResultVO(true, member),
                request,
                session,
                redirectAttributes,
                options(
                        true,
                        false,
                        "회원 없음",
                        "번호 오류",
                        "저장 회원 오류"));

        assertEquals(
                "redirect:/member/platform/select",
                result);

        verify(request).changeSessionId();
        verify(session).setAttribute(
                "pendingMemberNo",
                3L);
        verify(session).setAttribute(
                "pendingDisplayName",
                "신규닉네임");
        verify(memberService, never())
                .getMemberByNo(3L);
    }

    @Test
    void existingMemberWithZeroPlatformCountShouldAlsoUsePendingFlow() {
        MemberSocialJoinVO member = socialMember(
                4L,
                null,
                "회원이름",
                "KAKAO");

        when(memberPlatformService.countMemberPlatform(4L))
                .thenReturn(0);

        assertEquals(
                "redirect:/member/platform/select",
                support.completeLogin(
                        new SocialLoginResultVO(false, member),
                        request,
                        session,
                        redirectAttributes,
                        options(
                                true,
                                false,
                                "회원 없음",
                                "번호 오류",
                                "저장 회원 오류")));

        verify(session).setAttribute(
                "pendingDisplayName",
                "회원이름");
    }

    @Test
    void missingStoredMemberShouldReturnLoginAndConfiguredMessage() {
        MemberSocialJoinVO member = socialMember(
                5L,
                "닉네임",
                "이름",
                "GOOGLE");

        when(memberPlatformService.countMemberPlatform(5L))
                .thenReturn(1);
        when(memberService.getMemberByNo(5L))
                .thenReturn(null);

        assertEquals(
                "redirect:/member/login",
                support.completeLogin(
                        new SocialLoginResultVO(false, member),
                        request,
                        session,
                        redirectAttributes,
                        options(
                                true,
                                false,
                                "회원 없음",
                                "번호 오류",
                                "저장 회원 오류")));

        verify(redirectAttributes)
                .addFlashAttribute(
                        "errorMessage",
                        "저장 회원 오류");
    }

    @Test
    void synchronizedNameShouldFillBothSocialAndStoredMemberNames() {
        MemberSocialJoinVO member = socialMember(
                6L,
                "동기화닉네임",
                null,
                "NAVER");

        MemberVO stored = new MemberVO();
        stored.setMemberNo(6L);
        stored.setMemberName("   ");
        stored.setNickname("저장닉네임");
        stored.setAdultVerified(null);

        when(memberPlatformService.countMemberPlatform(6L))
                .thenReturn(1);
        when(memberService.getMemberByNo(6L))
                .thenReturn(stored);

        String result = support.completeLogin(
                new SocialLoginResultVO(false, member),
                request,
                session,
                redirectAttributes,
                options(
                        true,
                        true,
                        "회원 없음",
                        "번호 오류",
                        "저장 회원 오류"));

        assertEquals("redirect:/", result);
        assertEquals(
                "동기화닉네임",
                member.getMemberName());
        assertEquals(
                "동기화닉네임",
                stored.getMemberName());

        verify(session).setAttribute(
                "loginMember",
                stored);
        verify(session).setAttribute(
                "ADULT_VERIFIED",
                "N");
    }

    @Test
    void synchronizationDisabledShouldKeepStoredNameAndUseNameFirstFallback() {
        MemberSocialJoinVO member = socialMember(
                7L,
                "닉네임",
                "회원이름",
                "GOOGLE");

        MemberVO stored = new MemberVO();
        stored.setMemberNo(7L);
        stored.setMemberName("저장이름");
        stored.setAdultVerified("Y");

        when(memberPlatformService.countMemberPlatform(7L))
                .thenReturn(1);
        when(memberService.getMemberByNo(7L))
                .thenReturn(stored);

        assertEquals(
                "redirect:/",
                support.completeLogin(
                        new SocialLoginResultVO(false, member),
                        request,
                        session,
                        redirectAttributes,
                        options(
                                false,
                                false,
                                "회원 없음",
                                "번호 오류",
                                "저장 회원 오류")));

        assertEquals(
                "회원이름",
                member.getMemberName());
        assertEquals(
                "저장이름",
                stored.getMemberName());

        verify(session).setAttribute(
                "loginDisplayName",
                "회원이름");
        verify(session).setAttribute(
                "ADULT_VERIFIED",
                "Y");
    }

    private LoginOptions options(
            boolean nicknameFirst,
            boolean synchronizeMemberName,
            String missingMemberMessage,
            String invalidMemberNoMessage,
            String missingStoredMemberMessage) {

        return new LoginOptions(
                nicknameFirst,
                "소셜회원",
                synchronizeMemberName,
                missingMemberMessage,
                invalidMemberNoMessage,
                missingStoredMemberMessage);
    }

    private MemberSocialJoinVO socialMember(
            Long memberNo,
            String nickname,
            String memberName,
            String provider) {

        MemberSocialJoinVO member =
                new MemberSocialJoinVO();
        member.setMemberNo(memberNo);
        member.setNickname(nickname);
        member.setMemberName(memberName);
        member.setProvider(provider);
        member.setMemberId("social-id");
        member.setRole("USER");

        return member;
    }
}
