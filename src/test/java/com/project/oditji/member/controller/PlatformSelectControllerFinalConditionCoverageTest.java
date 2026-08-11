package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.servlet.http.HttpSession;

/** OTT 선택 컨트롤러의 세션 타입 변환과 ERROR 로그 guard를 보완합니다. */
class PlatformSelectControllerFinalConditionCoverageTest {

    private Logger targetLogger;
    private Level originalLevel;

    @BeforeEach
    void setUpLogger() {
        targetLogger = (Logger) LoggerFactory.getLogger(PlatformSelectController.class);
        originalLevel = targetLogger.getLevel();
    }

    @AfterEach
    void restoreLogger() {
        targetLogger.setLevel(originalLevel);
    }

    @Test
    void sessionValueShouldCoverNullLongIntegerAndStringConversions() {
        PlatformSelectController controller = new PlatformSelectController(
                mock(MemberPlatformService.class),
                mock(MemberService.class));
        HttpSession session = mock(HttpSession.class);

        when(session.getAttribute("nullKey")).thenReturn(null);
        when(session.getAttribute("longKey")).thenReturn(10L);
        when(session.getAttribute("intKey")).thenReturn(20);
        when(session.getAttribute("stringKey")).thenReturn("30");

        assertNull(invokeLong(controller, session, "nullKey"));
        assertEquals(Long.valueOf(10L), invokeLong(controller, session, "longKey"));
        assertEquals(Long.valueOf(20L), invokeLong(controller, session, "intKey"));
        assertEquals(Long.valueOf(30L), invokeLong(controller, session, "stringKey"));
    }

    @Test
    void unexpectedFailureShouldSkipErrorLoggingWhenDisabled() {
        MemberPlatformService memberPlatformService = mock(MemberPlatformService.class);
        MemberService memberService = mock(MemberService.class);
        HttpSession session = mock(HttpSession.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        PlatformSelectController controller = new PlatformSelectController(
                memberPlatformService,
                memberService);

        targetLogger.setLevel(Level.OFF);
        when(session.getAttribute("pendingMemberNo")).thenReturn(null);
        when(session.getAttribute("loginMemberNo")).thenReturn(70L);
        when(memberService.getMemberByNo(70L)).thenThrow(new IllegalStateException("db"));

        String result = controller.savePlatform(
                null,
                "Y",
                null,
                session,
                redirectAttributes);

        assertEquals("redirect:/member/platform/select", result);
        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "OTT 정보 저장 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }

    private Long invokeLong(
            PlatformSelectController controller,
            HttpSession session,
            String key) {

        return (Long) ReflectionTestUtils.invokeMethod(
                controller,
                "getLongSessionValue",
                session,
                key);
    }
}
