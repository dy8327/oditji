package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.member.service.GoogleLoginService;
import com.project.oditji.member.support.SocialLoginCallbackSupport;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Google 로그인 컨트롤러의 WARN 로그 가드 false 분기를 검증합니다.
 */
class GoogleLoginControllerLoggingGuardCoverageTest {

    private GoogleLoginService googleLoginService;
    private SocialLoginCallbackSupport callbackSupport;
    private GoogleLoginController controller;
    private Logger controllerLogger;
    private Level previousLevel;

    @BeforeEach
    void setUp() {
        googleLoginService = mock(GoogleLoginService.class);
        callbackSupport = mock(SocialLoginCallbackSupport.class);
        controller = new GoogleLoginController(googleLoginService, callbackSupport);

        controllerLogger = (Logger) LoggerFactory.getLogger(GoogleLoginController.class);
        previousLevel = controllerLogger.getLevel();
        controllerLogger.setLevel(Level.OFF);
    }

    @AfterEach
    void tearDown() {
        controllerLogger.setLevel(previousLevel);
    }

    @Test
    void loginPreparationFailureShouldSkipWarnLogWhenDisabled() {
        HttpSession session = mock(HttpSession.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        when(googleLoginService.getGoogleLoginUrl(anyString()))
                .thenThrow(new IllegalStateException("oauth disabled"));

        assertEquals(
                "redirect:/member/login",
                controller.googleLogin(session, redirectAttributes));
    }

    @Test
    void callbackFailureShouldSkipWarnLogWhenDisabled() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        when(session.getAttribute("googleOAuthState")).thenReturn("state");
        when(googleLoginService.googleLogin("code"))
                .thenThrow(new IllegalStateException("login failed"));

        assertEquals(
                "redirect:/member/login",
                controller.googleCallback(
                        "code",
                        "state",
                        null,
                        request,
                        session,
                        redirectAttributes));
    }
}
