package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.service.NaverLoginService;
import com.project.oditji.member.support.SocialLoginCallbackSupport;
import com.project.oditji.member.support.SocialLoginCallbackSupport.LoginOptions;
import com.project.oditji.member.vo.SocialLoginResultVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** 네이버 로그인 시작, OAuth 검증과 회원 상태별 콜백 처리를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class NaverLoginControllerCoverageTest {

    @Mock
    private NaverLoginService naverLoginService;

    @Mock
    private SocialLoginCallbackSupport callbackSupport;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private RedirectAttributes redirectAttributes;

    private final Map<String, Object> sessionValues = new HashMap<String, Object>();

    private NaverLoginController controller;

    @BeforeEach
    void setUp() {
        controller = new NaverLoginController(naverLoginService, callbackSupport);

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
    void loginShouldSaveGeneratedStateAndRedirect() {
        when(naverLoginService.createState()).thenReturn("generated-state");
        when(naverLoginService.getNaverLoginUrl("generated-state"))
                .thenReturn("https://naver.test/login");

        assertEquals("redirect:https://naver.test/login", controller.naverLogin(session));
        assertEquals("generated-state", sessionValues.get("naverOAuthState"));
    }

    @Test
    void callbackShouldRejectInvalidState() {
        sessionValues.put("naverOAuthState", "expected");

        assertEquals(
                "redirect:/member/login",
                controller.naverCallback(
                        "code",
                        "different",
                        null,
                        null,
                        request,
                        session,
                        redirectAttributes));
        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "네이버 로그인 요청 검증에 실패했습니다. 다시 시도해주세요.");
    }

    @Test
    void callbackShouldHandleProviderErrorAndDescription() {
        sessionValues.put("naverOAuthState", "state");

        assertEquals(
                "redirect:/member/login",
                controller.naverCallback(
                        null,
                        "state",
                        "access_denied",
                        "사용자가 취소함",
                        request,
                        session,
                        redirectAttributes));
        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "네이버 로그인이 취소되었거나 실패했습니다.");
    }

    @Test
    void callbackShouldRejectBlankAuthorizationCode() {
        sessionValues.put("naverOAuthState", "state");

        assertEquals(
                "redirect:/member/login",
                controller.naverCallback(
                        " ",
                        "state",
                        null,
                        null,
                        request,
                        session,
                        redirectAttributes));
        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "네이버 인증 코드를 받지 못했습니다. 다시 시도해주세요.");
    }

    @Test
    void callbackShouldDelegateSuccessfulLogin() {
        sessionValues.put("naverOAuthState", "state");
        SocialLoginResultVO loginResult = new SocialLoginResultVO();
        when(naverLoginService.naverLogin("code", "state")).thenReturn(loginResult);
        when(callbackSupport.completeLogin(
                eq(loginResult),
                eq(request),
                eq(session),
                eq(redirectAttributes),
                any(LoginOptions.class)))
                .thenReturn("redirect:/member/platform/select");

        assertEquals(
                "redirect:/member/platform/select",
                controller.naverCallback(
                        "code", "state", null, null, request, session, redirectAttributes));
        verify(callbackSupport).completeLogin(
                eq(loginResult),
                eq(request),
                eq(session),
                eq(redirectAttributes),
                any(LoginOptions.class));
    }

    @Test
    void blockedMemberShouldReturnLoginPage() {
        sessionValues.put("naverOAuthState", "state");
        when(naverLoginService.naverLogin("code", "state"))
                .thenThrow(new MemberBlockedException("이용이 정지되었습니다."));

        assertEquals(
                "redirect:/member/login",
                controller.naverCallback(
                        "code", "state", null, null, request, session, redirectAttributes));
        verify(redirectAttributes).addFlashAttribute("blockedMessage", "이용이 정지되었습니다.");
    }

    @Test
    void withdrawnMemberShouldSaveNaverRestoreSession() {
        sessionValues.put("naverOAuthState", "state");
        when(naverLoginService.naverLogin("code", "state"))
                .thenThrow(new MemberWithdrawnException("withdrawn", 88L, new Date()));

        assertEquals(
                "redirect:/member/login",
                controller.naverCallback(
                        "code", "state", null, null, request, session, redirectAttributes));
        verify(request).changeSessionId();
        assertEquals(88L, sessionValues.get("restoreMemberNo"));
        assertEquals("NAVER", sessionValues.get("restoreProvider"));
        verify(redirectAttributes).addFlashAttribute(eq("withdrawnMessage"), anyString());
    }

    @Test
    void serviceFailuresShouldReturnGenericMessage() {
        sessionValues.put("naverOAuthState", "state");
        when(naverLoginService.naverLogin("code", "state"))
                .thenThrow(new IllegalArgumentException("invalid token"));

        assertEquals(
                "redirect:/member/login",
                controller.naverCallback(
                        "code", "state", null, null, request, session, redirectAttributes));
        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "네이버 로그인 처리 중 오류가 발생했습니다.");
    }
}
