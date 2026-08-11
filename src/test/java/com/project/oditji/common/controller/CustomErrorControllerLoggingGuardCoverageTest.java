package com.project.oditji.common.controller;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.servlet.RequestDispatcher;

/**
 * /error 처리의 WARN/ERROR 로그 가드가 비활성화된 분기를 검증합니다.
 */
class CustomErrorControllerLoggingGuardCoverageTest {

    private CustomErrorController controller;
    private Logger controllerLogger;
    private Level previousLevel;

    @BeforeEach
    void setUp() {
        controller = new CustomErrorController();
        controllerLogger = (Logger) LoggerFactory.getLogger(CustomErrorController.class);
        previousLevel = controllerLogger.getLevel();
        controllerLogger.setLevel(Level.OFF);
    }

    @AfterEach
    void tearDown() {
        controllerLogger.setLevel(previousLevel);
    }

    @Test
    void disabledLoggingShouldCoverClientAndServerGuardFalseBranches() {
        assertInstanceOf(
                ModelAndView.class,
                controller.handleError(errorRequest(400, "/member/edit")));

        assertInstanceOf(
                ModelAndView.class,
                controller.handleError(errorRequest(500, "/internal/failure")));
    }

    private MockHttpServletRequest errorRequest(int status, String originalUri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/oditji/error");
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, status);
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, originalUri);
        return request;
    }
}
