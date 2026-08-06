package com.project.oditji.common.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.RequestDispatcher;

/** /error로 전달되는 403·404·500 및 API JSON 응답을 검증합니다. */
class CustomErrorControllerCoverageTest {

    private CustomErrorController controller;

    @BeforeEach
    void setUp() {
        controller = new CustomErrorController();
    }

    @Test
    void forbiddenShouldReturn403View() {
        ModelAndView response = assertInstanceOf(
                ModelAndView.class,
                controller.handleError(errorRequest(403, "/admin/product/list")));

        assertEquals("error/403", response.getViewName());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatus());
        assertEquals(403, response.getModel().get("errorCode"));
    }

    @Test
    void notFoundShouldReturn404View() {
        ModelAndView response = assertInstanceOf(
                ModelAndView.class,
                controller.handleError(errorRequest(404, "/missing-page")));

        assertEquals("error/404", response.getViewName());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
    }

    @Test
    void serverErrorShouldReturn500View() {
        ModelAndView response = assertInstanceOf(
                ModelAndView.class,
                controller.handleError(errorRequest(500, "/member/mypage")));

        assertEquals("error/500", response.getViewName());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
    }

    @Test
    void apiErrorShouldReturnJson() {
        MockHttpServletRequest request = errorRequest(403, "/chat/api/rooms");

        ResponseEntity<?> response = assertInstanceOf(
                ResponseEntity.class,
                controller.handleError(request));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(Boolean.FALSE, body.get("success"));
        assertEquals(403, body.get("status"));
    }

    private MockHttpServletRequest errorRequest(int status, String originalUri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/oditji/error");
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, status);
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, originalUri);
        return request;
    }
}
