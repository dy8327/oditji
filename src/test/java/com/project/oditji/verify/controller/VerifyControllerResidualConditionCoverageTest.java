package com.project.oditji.verify.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.verify.service.VerifyService;
import com.project.oditji.verify.vo.AdultVerifyRequestVO;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.servlet.http.HttpSession;

/** 성인인증 컨트롤러의 returnUrl OR 체인과 logger guard false 분기를 보완합니다. */
class VerifyControllerResidualConditionCoverageTest {

    private VerifyService verifyService;
    private VerifyController controller;

    @BeforeEach
    void setUp() {
        verifyService = mock(VerifyService.class);
        controller = new VerifyController(verifyService);
        ReflectionTestUtils.setField(controller, "storeId", "store");
        ReflectionTestUtils.setField(controller, "identity1ChannelKey", "identity-1");
        ReflectionTestUtils.setField(controller, "identity2ChannelKey", "identity-2");
    }

    @Test
    void sanitizeReturnUrlShouldCoverEveryRejectedConditionAndSafePath() {
        assertEquals("/", sanitize(null));
        assertEquals("/", sanitize("   "));
        assertEquals("/", sanitize("http://example.com"));
        assertEquals("/", sanitize("https://example.com"));
        assertEquals("/", sanitize("relative/path"));
        assertEquals("/safe/path", sanitize("/safe/path"));
    }

    @Test
    void adultPageShouldCoverMemberNumberZeroSecondOperand() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("memberNo")).thenReturn(0L);

        String result = controller.adultVerifyPage("/return", session, new ExtendedModelMap());
        assertEquals("redirect:/member/login", result);
    }

    @Test
    void completeShouldCoverNonNullBlankVerifyId() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("memberNo")).thenReturn(1L);
        AdultVerifyRequestVO request = new AdultVerifyRequestVO();
        request.setVerifyId("   ");

        var result = controller.completeVerification(request, "/", session);
        assertFalse(result.isSuccess());
        assertEquals("본인인증 요청 ID가 없습니다.", result.getMessage());
    }

    @Test
    void loggerDisabledShouldCoverCompleteFailureGuard() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("memberNo")).thenReturn(2L);
        AdultVerifyRequestVO request = new AdultVerifyRequestVO();
        request.setVerifyId("verify-id");
        when(verifyService.completeVerification(2L, "verify-id", "/", session))
                .thenThrow(new RuntimeException("boom"));

        Logger logger = (Logger) LoggerFactory.getLogger(VerifyController.class);
        Level original = logger.getLevel();
        try {
            logger.setLevel(Level.OFF);
            var result = controller.completeVerification(request, "/", session);
            assertFalse(result.isSuccess());
        } finally {
            logger.setLevel(original);
        }
    }

    private String sanitize(String value) {
        return ReflectionTestUtils.invokeMethod(controller, "sanitizeReturnUrl", value);
    }
}
