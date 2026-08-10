package com.project.oditji.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 전역 예외 처리기의 JSON 요청 OR 조건과 오류 화면 선택 조건을 직접 보완합니다.
 */
class GlobalExceptionHandlerAdditionalRemainingCoverageTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void jsonRequestPredicateShouldCoverEveryOrOperandAndAllFalseCase() {
        MockHttpServletRequest api = request("/api/content");
        assertTrue(isJsonRequest(api));

        MockHttpServletRequest ajax = request("/member/update");
        ajax.addHeader(
                "X-Requested-With",
                "xmlhttprequest");
        assertTrue(isJsonRequest(ajax));

        MockHttpServletRequest accept = request("/member/update");
        accept.addHeader(
                "Accept",
                "text/html, "
                        + MediaType.APPLICATION_JSON_VALUE);
        assertTrue(isJsonRequest(accept));

        MockHttpServletRequest contentType = request("/member/update");
        contentType.setContentType(
                MediaType.APPLICATION_JSON_VALUE
                        + ";charset=UTF-8");
        assertTrue(isJsonRequest(contentType));

        MockHttpServletRequest html = request("/member/update");
        html.addHeader("Accept", "text/html");
        html.setContentType("text/plain");
        html.addHeader("X-Requested-With", "fetch");
        assertFalse(isJsonRequest(html));
    }

    @Test
    void resolveViewNameShouldCoverForbiddenNotFoundServerAndCommonStatuses() {
        assertEquals(
                "error/403",
                resolveViewName(HttpStatus.FORBIDDEN));
        assertEquals(
                "error/404",
                resolveViewName(HttpStatus.NOT_FOUND));
        assertEquals(
                "error/500",
                resolveViewName(HttpStatus.BAD_GATEWAY));
        assertEquals(
                "error/common",
                resolveViewName(HttpStatus.BAD_REQUEST));
    }

    private boolean isJsonRequest(
            MockHttpServletRequest request) {

        Boolean result = ReflectionTestUtils.invokeMethod(
                handler,
                "isJsonRequest",
                request);

        return Boolean.TRUE.equals(result);
    }

    private String resolveViewName(
            HttpStatus status) {

        return ReflectionTestUtils.invokeMethod(
                handler,
                "resolveViewName",
                status);
    }

    private MockHttpServletRequest request(
            String uri) {

        MockHttpServletRequest request =
                new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI(uri);
        return request;
    }
}
