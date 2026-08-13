package com.project.oditji.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

/**
 * 전역 예외 처리기의 비표준 상태 코드와 JSON 요청 판별의 남은 조건을 보완합니다.
 */
class GlobalExceptionHandlerRemainingCoverageTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void unknownHttpStatusCodeShouldFallBackToInternalServerErrorView() {
        ResponseStatusException exception =
                new ResponseStatusException(HttpStatusCode.valueOf(599));

        ModelAndView result = assertInstanceOf(
                ModelAndView.class,
                handler.handleResponseStatusException(
                        exception,
                        htmlRequest("/unknown-status")));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatus());
        assertEquals("error/500", result.getViewName());
        assertEquals(
                "요청을 처리할 수 없습니다.",
                result.getModel().get("errorMessage"));
    }

    @Test
    void nonJsonAcceptAndContentTypeShouldRemainHtmlResponse() {
        MockHttpServletRequest request = htmlRequest("/member/update");
        request.addHeader("Accept", "text/html");
        request.setContentType("text/plain");
        request.addHeader("X-Requested-With", "fetch");

        ModelAndView result = assertInstanceOf(
                ModelAndView.class,
                handler.handleIllegalArgumentException(
                        new IllegalArgumentException("잘못된 요청"),
                        request));

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
        assertEquals("error/common", result.getViewName());
    }

    @Test
    void lowercaseAjaxHeaderShouldStillProduceJsonResponse() {
        MockHttpServletRequest request = htmlRequest("/member/update");
        request.addHeader("X-Requested-With", "xmlhttprequest");

        ResponseEntity<?> result = assertInstanceOf(
                ResponseEntity.class,
                handler.handleIllegalArgumentException(
                        new IllegalArgumentException("ajax"),
                        request));

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());

        Map<?, ?> body = (Map<?, ?>) result.getBody();
        assertEquals(Boolean.FALSE, body.get("success"));
        assertEquals("ajax", body.get("message"));
    }

    private MockHttpServletRequest htmlRequest(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI(uri);
        return request;
    }
}
