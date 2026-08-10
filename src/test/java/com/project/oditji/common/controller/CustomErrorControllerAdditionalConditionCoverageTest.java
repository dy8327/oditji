package com.project.oditji.common.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.RequestDispatcher;

/**
 * CustomErrorController의 서버 예외 객체 ternary 양쪽과 상태코드 타입 분기를 보완합니다.
 */
class CustomErrorControllerAdditionalConditionCoverageTest {

    private CustomErrorController controller;

    @BeforeEach
    void setUp() {
        controller = new CustomErrorController();
    }

    @Test
    void serverErrorShouldAcceptThrowableErrorAttribute() {
        MockHttpServletRequest request =
                request(500);
        request.setAttribute(
                RequestDispatcher.ERROR_EXCEPTION,
                new IllegalStateException("boom"));

        ModelAndView result =
                assertInstanceOf(
                        ModelAndView.class,
                        controller.handleError(request));

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                result.getStatus());
    }

    @Test
    void serverErrorShouldIgnoreNonThrowableErrorAttribute() {
        MockHttpServletRequest request =
                request(500);
        request.setAttribute(
                RequestDispatcher.ERROR_EXCEPTION,
                "not-throwable");

        ModelAndView result =
                assertInstanceOf(
                        ModelAndView.class,
                        controller.handleError(request));

        assertEquals(
                "error/500",
                result.getViewName());
    }

    @Test
    void integerUnknownAndNonIntegerStatusesShouldBothFallbackTo500() {
        MockHttpServletRequest unknown =
                new MockHttpServletRequest();
        unknown.setRequestURI("/oditji/error");
        unknown.setAttribute(
                RequestDispatcher.ERROR_STATUS_CODE,
                999);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                assertInstanceOf(
                        ModelAndView.class,
                        controller.handleError(unknown))
                        .getStatus());

        MockHttpServletRequest nonInteger =
                new MockHttpServletRequest();
        nonInteger.setRequestURI("/oditji/error");
        nonInteger.setAttribute(
                RequestDispatcher.ERROR_STATUS_CODE,
                "403");

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                assertInstanceOf(
                        ModelAndView.class,
                        controller.handleError(nonInteger))
                        .getStatus());
    }

    private MockHttpServletRequest request(int status) {
        MockHttpServletRequest request =
                new MockHttpServletRequest();
        request.setRequestURI("/oditji/error");
        request.setAttribute(
                RequestDispatcher.ERROR_STATUS_CODE,
                status);
        request.setAttribute(
                RequestDispatcher.ERROR_REQUEST_URI,
                "/member/page");
        return request;
    }
}
