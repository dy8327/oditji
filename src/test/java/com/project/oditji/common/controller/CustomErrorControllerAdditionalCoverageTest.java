package com.project.oditji.common.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.RequestDispatcher;

/** 공통 오류 컨트롤러의 상태 해석과 JSON 요청 판별의 남은 분기를 검증합니다. */
class CustomErrorControllerAdditionalCoverageTest {

    private CustomErrorController controller;

    @BeforeEach
    void setUp() {
        controller = new CustomErrorController();
    }

    @Test
    void clientStatusesShouldUseExpectedMessagesAndCommonView() {
        ModelAndView badRequest = view(errorRequest(400, "/member/edit"));
        assertEquals("error/common", badRequest.getViewName());
        assertEquals("요청 정보가 올바르지 않습니다.", badRequest.getModel().get("errorMessage"));

        ModelAndView methodNotAllowed = view(errorRequest(405, "/member/edit"));
        assertEquals("error/common", methodNotAllowed.getViewName());
        assertEquals("허용되지 않은 요청 방식입니다.", methodNotAllowed.getModel().get("errorMessage"));

        ModelAndView teapot = view(errorRequest(418, "/coffee"));
        assertEquals("error/common", teapot.getViewName());
        assertEquals("요청을 처리할 수 없습니다.", teapot.getModel().get("errorMessage"));
    }

    @Test
    void missingOrUnknownStatusShouldFallbackToInternalServerError() {
        MockHttpServletRequest missing = new MockHttpServletRequest();
        missing.setRequestURI("/oditji/error");
        ModelAndView missingStatus = view(missing);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, missingStatus.getStatus());
        assertEquals("error/500", missingStatus.getViewName());

        MockHttpServletRequest unknown = new MockHttpServletRequest();
        unknown.setRequestURI("/oditji/error");
        unknown.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 999);
        ModelAndView unknownStatus = view(unknown);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, unknownStatus.getStatus());
    }

    @Test
    void nonIntegerStatusAndMissingOriginalUriShouldUseRequestUri() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/oditji/fallback");
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, "404");

        ModelAndView response = view(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("error/500", response.getViewName());
    }

    @Test
    void jsonDetectionShouldCoverAjaxAcceptAndContentTypeHeaders() {
        MockHttpServletRequest ajax = errorRequest(400, "/member/edit");
        ajax.addHeader("X-Requested-With", "xmlhttprequest");
        assertEquals(HttpStatus.BAD_REQUEST, json(ajax).getStatusCode());

        MockHttpServletRequest accept = errorRequest(405, "/member/edit");
        accept.addHeader("Accept", "text/plain, " + MediaType.APPLICATION_JSON_VALUE);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, json(accept).getStatusCode());

        MockHttpServletRequest contentType = errorRequest(418, "/member/edit");
        contentType.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        ResponseEntity<?> response = json(contentType);
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(418, body.get("status"));
        assertEquals("요청을 처리할 수 없습니다.", body.get("message"));
    }

    private MockHttpServletRequest errorRequest(int status, String originalUri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/oditji/error");
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, status);
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, originalUri);
        return request;
    }

    private ModelAndView view(MockHttpServletRequest request) {
        return assertInstanceOf(ModelAndView.class, controller.handleError(request));
    }

    private ResponseEntity<?> json(MockHttpServletRequest request) {
        return assertInstanceOf(ResponseEntity.class, controller.handleError(request));
    }
}
