package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.member.service.GoogleLoginService;
import com.project.oditji.member.support.SocialLoginCallbackSupport;
import com.project.oditji.member.support.SocialLoginCallbackSupport.LoginOptions;
import com.project.oditji.member.vo.SocialLoginResultVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Google 콜백의 null state, blank error, null code 전달 조건을 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class GoogleLoginControllerRemainingCoverageTest {

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

    private final Map<String, Object> sessionValues =
            new HashMap<String, Object>();

    private GoogleLoginController controller;

    @BeforeEach
    void setUp() {
        controller = new GoogleLoginController(
                googleLoginService,
                callbackSupport);

        lenient().when(session.getAttribute(anyString()))
                .thenAnswer(invocation ->
                        sessionValues.get(
                                invocation.getArgument(0)));

        lenient().doAnswer(invocation -> {
            sessionValues.remove(
                    invocation.getArgument(0));
            return null;
        }).when(session).removeAttribute(anyString());
    }

    @Test
    void nullReturnedStateShouldFailAfterStoredStateExists() {
        sessionValues.put(
                "googleOAuthState",
                "stored-state");

        assertEquals(
                "redirect:/member/login",
                controller.googleCallback(
                        "code",
                        null,
                        null,
                        request,
                        session,
                        redirectAttributes));

        verify(redirectAttributes)
                .addFlashAttribute(
                        "errorMessage",
                        "Google 로그인 요청 검증에 실패했습니다. 다시 시도해주세요.");
    }

    @Test
    void differentNonNullStateShouldReachEqualsFalseBranch() {
        sessionValues.put(
                "googleOAuthState",
                "stored-state");

        assertEquals(
                "redirect:/member/login",
                controller.googleCallback(
                        "code",
                        "other-state",
                        null,
                        request,
                        session,
                        redirectAttributes));
    }

    @Test
    void blankProviderErrorShouldFallThroughAndDelegateLogin() {
        sessionValues.put(
                "googleOAuthState",
                "state");

        SocialLoginResultVO loginResult =
                new SocialLoginResultVO();

        when(googleLoginService.googleLogin("code"))
                .thenReturn(loginResult);

        when(callbackSupport.completeLogin(
                eq(loginResult),
                eq(request),
                eq(session),
                eq(redirectAttributes),
                any(LoginOptions.class)))
                .thenReturn("redirect:/");

        assertEquals(
                "redirect:/",
                controller.googleCallback(
                        "code",
                        "state",
                        "   ",
                        request,
                        session,
                        redirectAttributes));
    }

    @Test
    void nullAuthorizationCodeIsPassedToServiceWhenStateIsValid() {
        sessionValues.put(
                "googleOAuthState",
                "state");

        SocialLoginResultVO loginResult =
                new SocialLoginResultVO();

        when(googleLoginService.googleLogin(null))
                .thenReturn(loginResult);

        when(callbackSupport.completeLogin(
                eq(loginResult),
                eq(request),
                eq(session),
                eq(redirectAttributes),
                any(LoginOptions.class)))
                .thenReturn("redirect:/");

        assertEquals(
                "redirect:/",
                controller.googleCallback(
                        null,
                        "state",
                        null,
                        request,
                        session,
                        redirectAttributes));

        verify(googleLoginService).googleLogin(null);
    }
}
