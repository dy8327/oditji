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

import com.project.oditji.member.service.NaverLoginService;
import com.project.oditji.member.support.SocialLoginCallbackSupport;
import com.project.oditji.member.support.SocialLoginCallbackSupport.LoginOptions;
import com.project.oditji.member.vo.SocialLoginResultVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 네이버 OAuth 콜백의 null/blank 조건과 IllegalStateException 분기를 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class NaverLoginControllerRemainingCoverageTest {

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

    private final Map<String, Object> sessionValues =
            new HashMap<String, Object>();

    private NaverLoginController controller;

    @BeforeEach
    void setUp() {
        controller = new NaverLoginController(
                naverLoginService,
                callbackSupport);

        lenient().when(session.getAttribute(anyString()))
                .thenAnswer(invocation ->
                        sessionValues.get(invocation.getArgument(0)));

        lenient().doAnswer(invocation -> {
            sessionValues.remove(invocation.getArgument(0));
            return null;
        }).when(session).removeAttribute(anyString());
    }

    @Test
    void callbackShouldRejectNullSavedStateAndNullReturnedState() {
        assertEquals(
                "redirect:/member/login",
                controller.naverCallback(
                        "code",
                        "state",
                        null,
                        null,
                        request,
                        session,
                        redirectAttributes));

        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "네이버 로그인 요청 검증에 실패했습니다. 다시 시도해주세요.");

        sessionValues.put("naverOAuthState", "state");

        assertEquals(
                "redirect:/member/login",
                controller.naverCallback(
                        "code",
                        null,
                        null,
                        null,
                        request,
                        session,
                        redirectAttributes));
    }

    @Test
    void blankProviderErrorShouldFallThroughToSuccessfulLogin() {
        sessionValues.put("naverOAuthState", "state");

        SocialLoginResultVO result =
                new SocialLoginResultVO();

        when(naverLoginService.naverLogin(
                "code",
                "state"))
                .thenReturn(result);

        when(callbackSupport.completeLogin(
                eq(result),
                eq(request),
                eq(session),
                eq(redirectAttributes),
                any(LoginOptions.class)))
                .thenReturn("redirect:/");

        assertEquals(
                "redirect:/",
                controller.naverCallback(
                        "code",
                        "state",
                        "   ",
                        null,
                        request,
                        session,
                        redirectAttributes));
    }

    @Test
    void providerErrorShouldHandleNullAndBlankDescriptions() {
        sessionValues.put("naverOAuthState", "state");

        assertEquals(
                "redirect:/member/login",
                controller.naverCallback(
                        null,
                        "state",
                        "access_denied",
                        null,
                        request,
                        session,
                        redirectAttributes));

        sessionValues.put("naverOAuthState", "state");

        assertEquals(
                "redirect:/member/login",
                controller.naverCallback(
                        null,
                        "state",
                        "access_denied",
                        "   ",
                        request,
                        session,
                        redirectAttributes));
    }

    @Test
    void nullAuthorizationCodeShouldBeRejected() {
        sessionValues.put("naverOAuthState", "state");

        assertEquals(
                "redirect:/member/login",
                controller.naverCallback(
                        null,
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
    void illegalStateExceptionShouldUseGenericLoginFailureMessage() {
        sessionValues.put("naverOAuthState", "state");

        when(naverLoginService.naverLogin(
                "code",
                "state"))
                .thenThrow(
                        new IllegalStateException(
                                "service state failure"));

        assertEquals(
                "redirect:/member/login",
                controller.naverCallback(
                        "code",
                        "state",
                        null,
                        null,
                        request,
                        session,
                        redirectAttributes));

        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "네이버 로그인 처리 중 오류가 발생했습니다.");
    }
}
