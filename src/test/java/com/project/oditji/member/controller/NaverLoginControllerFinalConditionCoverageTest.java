package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.member.service.NaverLoginService;
import com.project.oditji.member.support.SocialLoginCallbackSupport;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** 네이버 콜백 로그 guard의 남은 false 조건을 보완합니다. */
class NaverLoginControllerFinalConditionCoverageTest {

    private Logger targetLogger;
    private Level originalLevel;

    @BeforeEach
    void setUpLogger() {
        targetLogger = (Logger) LoggerFactory.getLogger(NaverLoginController.class);
        originalLevel = targetLogger.getLevel();
        targetLogger.setLevel(Level.OFF);
    }

    @AfterEach
    void restoreLogger() {
        targetLogger.setLevel(originalLevel);
    }

    @Test
    void callbackErrorDescriptionShouldSkipWarnWhenLoggingIsDisabled() {
        NaverLoginService naverLoginService = mock(NaverLoginService.class);
        SocialLoginCallbackSupport callbackSupport = mock(SocialLoginCallbackSupport.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        NaverLoginController controller = new NaverLoginController(naverLoginService, callbackSupport);

        when(session.getAttribute("naverOAuthState")).thenReturn("state");

        String result = controller.naverCallback(
                null,
                "state",
                "access_denied",
                "사용자가 취소함",
                request,
                session,
                redirectAttributes);

        assertEquals("redirect:/member/login", result);
        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "네이버 로그인이 취소되었거나 실패했습니다.");
    }

    @Test
    void callbackServiceFailureShouldSkipWarnWhenLoggingIsDisabled() {
        NaverLoginService naverLoginService = mock(NaverLoginService.class);
        SocialLoginCallbackSupport callbackSupport = mock(SocialLoginCallbackSupport.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        NaverLoginController controller = new NaverLoginController(naverLoginService, callbackSupport);

        when(session.getAttribute("naverOAuthState")).thenReturn("state");
        when(naverLoginService.naverLogin("code", "state"))
                .thenThrow(new IllegalStateException("oauth failure"));

        String result = controller.naverCallback(
                "code",
                "state",
                null,
                null,
                request,
                session,
                redirectAttributes);

        assertEquals("redirect:/member/login", result);
        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "네이버 로그인 처리 중 오류가 발생했습니다.");
    }
}
