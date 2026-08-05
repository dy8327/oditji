package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.project.oditji.member.service.GoogleLoginService;
import com.project.oditji.member.support.SocialLoginCallbackSupport;
import com.project.oditji.member.support.SocialLoginCallbackSupport.LoginOptions;
import com.project.oditji.member.vo.SocialLoginResultVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** Google 로그인 시작과 콜백의 검증 및 예외 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class GoogleLoginControllerCoverageTest {

    @Mock
    private GoogleLoginService googleLoginService;

    @Mock
    private SocialLoginCallbackSupport callbackSupport;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private RedirectAttributes redirectAttributes;

    private final Map<String, Object> sessionValues = new HashMap<String, Object>();

    private GoogleLoginController controller;

    @BeforeEach
    void setUp() {
        controller = new GoogleLoginController(googleLoginService, callbackSupport);

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
    void loginShouldCreateStateAndRedirectToProvider() {
        when(googleLoginService.getGoogleLoginUrl(anyString()))
                .thenAnswer(invocation -> "https://google.test?state=" + invocation.getArgument(0));

        String result = controller.googleLogin(session, redirectAttributes);

        String state = (String) sessionValues.get("googleOAuthState");
        assertNotNull(state);
        assertEquals("redirect:https://google.test?state=" + state, result);
        verify(googleLoginService).getGoogleLoginUrl(state);
    }

    @Test
    void loginPreparationFailureShouldReturnLoginPage() {
        when(googleLoginService.getGoogleLoginUrl(anyString()))
                .thenThrow(new IllegalStateException("missing client"));

        assertEquals(
                "redirect:/member/login",
                controller.googleLogin(session, redirectAttributes));
        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "Google 로그인 준비 중 오류가 발생했습니다.");
    }

    @Test
    void callbackShouldRejectProviderErrorBeforeStateValidation() {
        sessionValues.put("googleOAuthState", "state");

        String result = controller.googleCallback(
                null,
                "state",
                "access_denied",
                request,
                session,
                redirectAttributes);

        assertEquals("redirect:/member/login", result);
        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "Google 로그인이 취소되었거나 승인되지 않았습니다.");
        assertEquals(null, sessionValues.get("googleOAuthState"));
    }

    @Test
    void callbackShouldRejectMissingOrDifferentState() {
        assertEquals(
                "redirect:/member/login",
                controller.googleCallback(
                        "code",
                        "different",
                        null,
                        request,
                        session,
                        redirectAttributes));
        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "Google 로그인 요청 검증에 실패했습니다. 다시 시도해주세요.");
    }

    @Test
    void callbackShouldDelegateSuccessfulLogin() {
        sessionValues.put("googleOAuthState", "state");
        SocialLoginResultVO loginResult = new SocialLoginResultVO();
        when(googleLoginService.googleLogin("code")).thenReturn(loginResult);
        when(callbackSupport.completeLogin(
                eq(loginResult),
                eq(request),
                eq(session),
                eq(redirectAttributes),
                any(LoginOptions.class)))
                .thenReturn("redirect:/");

        String result = controller.googleCallback(
                "code",
                "state",
                null,
                request,
                session,
                redirectAttributes);

        assertEquals("redirect:/", result);
        verify(callbackSupport).completeLogin(
                eq(loginResult),
                eq(request),
                eq(session),
                eq(redirectAttributes),
                any(LoginOptions.class));
    }

    @Test
    void blockedMemberShouldExposeBlockedMessage() {
        sessionValues.put("googleOAuthState", "state");
        when(googleLoginService.googleLogin("code"))
                .thenThrow(new MemberBlockedException("정지된 회원입니다."));

        assertEquals(
                "redirect:/member/login",
                controller.googleCallback(
                        "code", "state", null, request, session, redirectAttributes));
        verify(redirectAttributes).addFlashAttribute("blockedMessage", "정지된 회원입니다.");
    }

    @Test
    void withdrawnMemberShouldSaveRestoreSession() {
        sessionValues.put("googleOAuthState", "state");
        LocalDateTime withdrawnAt = LocalDateTime.now(DateTimeUtil.KOREA_ZONE);
        when(googleLoginService.googleLogin("code"))
                .thenThrow(new MemberWithdrawnException("withdrawn", 77L, withdrawnAt));

        assertEquals(
                "redirect:/member/login",
                controller.googleCallback(
                        "code", "state", null, request, session, redirectAttributes));
        verify(request).changeSessionId();
        assertEquals(77L, sessionValues.get("restoreMemberNo"));
        assertEquals("GOOGLE", sessionValues.get("restoreProvider"));
        verify(redirectAttributes).addFlashAttribute(eq("withdrawnMessage"), anyString());
    }

    @Test
    void loginProcessingFailureShouldReturnGenericMessage() {
        sessionValues.put("googleOAuthState", "state");
        when(googleLoginService.googleLogin("code"))
                .thenThrow(new IllegalStateException("token error"));

        assertEquals(
                "redirect:/member/login",
                controller.googleCallback(
                        "code", "state", null, request, session, redirectAttributes));
        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "Google 로그인 처리 중 오류가 발생했습니다.");
    }
}
