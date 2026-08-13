package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.common.util.DateTimeUtil;
import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.service.KakaoLoginService;
import com.project.oditji.member.support.SocialLoginCallbackSupport;
import com.project.oditji.member.support.SocialLoginCallbackSupport.LoginOptions;
import com.project.oditji.member.vo.SocialLoginResultVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** 카카오 로그인 시작, 성공 콜백과 회원 상태별 예외 처리를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class KakaoLoginControllerCoverageTest {

    @Mock
    private KakaoLoginService kakaoLoginService;

    @Mock
    private SocialLoginCallbackSupport callbackSupport;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private RedirectAttributes redirectAttributes;

    private final Map<String, Object> sessionValues = new HashMap<String, Object>();

    private KakaoLoginController controller;

    @BeforeEach
    void setUp() {
        controller = new KakaoLoginController(kakaoLoginService, callbackSupport);

        lenient().when(session.getAttribute(anyString()))
                .thenAnswer(invocation -> sessionValues.get(invocation.getArgument(0)));
        lenient().doAnswer(invocation -> {
            sessionValues.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(session).setAttribute(anyString(), any());
        lenient().doAnswer(invocation -> {
            sessionValues.remove(invocation.getArgument(0));
            return null;
        }).when(session).removeAttribute(anyString());
    }

    @Test
    void loginShouldRedirectToConfiguredProviderUrl() {
        when(kakaoLoginService.getKakaoLoginUrl())
                .thenReturn("https://kauth.kakao.com/oauth/authorize");

        assertEquals(
                "redirect:https://kauth.kakao.com/oauth/authorize",
                controller.kakaoLogin());
    }

    @Test
    void callbackShouldDelegateSuccessfulLogin() {
        SocialLoginResultVO result = new SocialLoginResultVO();
        when(kakaoLoginService.kakaoLogin("code")).thenReturn(result);
        when(callbackSupport.completeLogin(
                eq(result),
                eq(request),
                eq(session),
                eq(redirectAttributes),
                any(LoginOptions.class)))
                .thenReturn("redirect:/");

        assertEquals(
                "redirect:/",
                controller.kakaoCallback(
                        "code",
                        request,
                        session,
                        redirectAttributes));

        verify(callbackSupport).completeLogin(
                eq(result),
                eq(request),
                eq(session),
                eq(redirectAttributes),
                any(LoginOptions.class));
    }

    @Test
    void blockedMemberShouldExposeBlockedMessage() {
        when(kakaoLoginService.kakaoLogin("code"))
                .thenThrow(new MemberBlockedException("정지된 회원입니다."));

        assertEquals(
                "redirect:/member/login",
                controller.kakaoCallback(
                        "code",
                        request,
                        session,
                        redirectAttributes));
        verify(redirectAttributes)
                .addFlashAttribute("blockedMessage", "정지된 회원입니다.");
    }

    @Test
    void withdrawnMemberShouldSaveRestoreSession() {
        when(kakaoLoginService.kakaoLogin("code"))
                .thenThrow(new MemberWithdrawnException(
                        "withdrawn",
                        77L,
                        LocalDateTime.now(DateTimeUtil.KOREA_ZONE)));

        assertEquals(
                "redirect:/member/login",
                controller.kakaoCallback(
                        "code",
                        request,
                        session,
                        redirectAttributes));

        verify(request).changeSessionId();
        assertEquals(77L, sessionValues.get("restoreMemberNo"));
        assertEquals("KAKAO", sessionValues.get("restoreProvider"));
        verify(redirectAttributes)
                .addFlashAttribute(eq("withdrawnMessage"), anyString());
    }

    @Test
    void processingFailureShouldReturnGenericLoginMessage() {
        when(kakaoLoginService.kakaoLogin("code"))
                .thenThrow(new IllegalStateException("token"));

        assertEquals(
                "redirect:/member/login",
                controller.kakaoCallback(
                        "code",
                        request,
                        session,
                        redirectAttributes));
        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "카카오 로그인 처리 중 오류가 발생했습니다.");
    }
}
