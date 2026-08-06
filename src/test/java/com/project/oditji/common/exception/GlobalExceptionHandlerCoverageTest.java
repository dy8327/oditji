package com.project.oditji.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.ModelAndView;

/** 전역 예외 처리기의 HTML·JSON 응답 및 요청 유형 판별 분기를 검증합니다. */
class GlobalExceptionHandlerCoverageTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void notFoundShouldReturnHtml404View() {
        ModelAndView response = assertInstanceOf(
                ModelAndView.class,
                handler.handleNotFoundException(
                        new Exception("missing"),
                        htmlRequest("/missing")));

        assertEquals("error/404", response.getViewName());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertEquals(404, response.getModel().get("errorCode"));
    }

    @Test
    void accessDeniedShouldReturn403View() {
        ModelAndView response = assertInstanceOf(
                ModelAndView.class,
                handler.handleAccessDeniedException(
                        new AccessDeniedException("denied"),
                        htmlRequest("/admin/product/list")));

        assertEquals("error/403", response.getViewName());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatus());
    }

    @Test
    void apiUriShouldReturnJsonBadRequest() {
        ResponseEntity<?> response = assertInstanceOf(
                ResponseEntity.class,
                handler.handleIllegalArgumentException(
                        new IllegalArgumentException("잘못된 값"),
                        jsonRequestByUri()));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(Boolean.FALSE, body.get("success"));
        assertEquals("잘못된 값", body.get("message"));
    }

    @Test
    void ajaxHeaderShouldSelectJsonResponse() {
        MockHttpServletRequest request = htmlRequest("/member/update");
        request.addHeader("X-Requested-With", "XMLHttpRequest");

        assertInstanceOf(
                ResponseEntity.class,
                handler.handleException(new Exception("failure"), request));
    }

    @Test
    void acceptHeaderShouldSelectJsonResponse() {
        MockHttpServletRequest request = htmlRequest("/member/update");
        request.addHeader("Accept", MediaType.APPLICATION_JSON_VALUE);

        assertInstanceOf(
                ResponseEntity.class,
                handler.handleException(new Exception("failure"), request));
    }

    @Test
    void contentTypeShouldSelectJsonResponse() {
        MockHttpServletRequest request = htmlRequest("/member/update");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

        assertInstanceOf(
                ResponseEntity.class,
                handler.handleException(new Exception("failure"), request));
    }

    @Test
    void missingParameterShouldReturnBadRequestMessage() {
        MissingServletRequestParameterException exception =
                mock(MissingServletRequestParameterException.class);
        when(exception.getParameterName()).thenReturn("memberNo");

        ModelAndView response = assertInstanceOf(
                ModelAndView.class,
                handler.handleMissingParameter(exception, htmlRequest("/member")));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(
                "필수 입력값이 누락되었습니다.",
                response.getModel().get("errorMessage"));
    }

    @Test
    void unsupportedMethodShouldReturn405() {
        ModelAndView response = assertInstanceOf(
                ModelAndView.class,
                handler.handleMethodNotSupported(
                        mock(HttpRequestMethodNotSupportedException.class),
                        htmlRequest("/cart")));

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatus());
        assertEquals("error/common", response.getViewName());
    }

    @Test
    void typeMismatchShouldReturnBadRequest() {
        MethodArgumentTypeMismatchException exception =
                mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("page");

        ModelAndView response = assertInstanceOf(
                ModelAndView.class,
                handler.handleTypeMismatch(exception, htmlRequest("/order/list")));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals(
                "요청값 형식이 올바르지 않습니다.",
                response.getModel().get("errorMessage"));
    }

    @Test
    void responseStatusShouldUseReasonAndStatusSpecificView() {
        ModelAndView notFound = assertInstanceOf(
                ModelAndView.class,
                handler.handleResponseStatusException(
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "콘텐츠 없음"),
                        htmlRequest("/content/1")));

        assertEquals("error/404", notFound.getViewName());
        assertEquals("콘텐츠 없음", notFound.getModel().get("errorMessage"));

        ModelAndView badRequest = assertInstanceOf(
                ModelAndView.class,
                handler.handleResponseStatusException(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST),
                        htmlRequest("/content")));

        assertEquals("error/common", badRequest.getViewName());
        assertEquals(
                "요청을 처리할 수 없습니다.",
                badRequest.getModel().get("errorMessage"));

        ModelAndView forbidden = assertInstanceOf(
                ModelAndView.class,
                handler.handleResponseStatusException(
                        new ResponseStatusException(HttpStatus.FORBIDDEN, "권한 없음"),
                        htmlRequest("/admin/product/list")));

        assertEquals("error/403", forbidden.getViewName());
        assertEquals(HttpStatus.FORBIDDEN, forbidden.getStatus());
    }

    @Test
    void uploadDataAndMessageFailuresShouldReturnMappedStatuses() {
        ModelAndView upload = assertInstanceOf(
                ModelAndView.class,
                handler.handleMaxUploadSizeExceededException(
                        mock(MaxUploadSizeExceededException.class),
                        htmlRequest("/business/product")));
        assertEquals(HttpStatus.CONTENT_TOO_LARGE, upload.getStatus());

        ModelAndView conflict = assertInstanceOf(
                ModelAndView.class,
                handler.handleDataIntegrityViolationException(
                        new DataIntegrityViolationException("duplicate"),
                        htmlRequest("/review/write")));
        assertEquals(HttpStatus.CONFLICT, conflict.getStatus());

        ModelAndView unreadable = assertInstanceOf(
                ModelAndView.class,
                handler.handleHttpMessageNotReadableException(
                        mock(HttpMessageNotReadableException.class),
                        htmlRequest("/request")));
        assertEquals(HttpStatus.BAD_REQUEST, unreadable.getStatus());
    }

    @Test
    void genericExceptionShouldReturn500HtmlBody() {
        ModelAndView response = assertInstanceOf(
                ModelAndView.class,
                handler.handleException(
                        new IllegalStateException("failure"),
                        htmlRequest("/member/mypage")));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("error/500", response.getViewName());
        assertEquals(500, response.getModel().get("errorCode"));
        assertFalse(((String) response.getModel().get("errorMessage")).isBlank());
    }

    private MockHttpServletRequest htmlRequest(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI(uri);
        return request;
    }

    private MockHttpServletRequest jsonRequestByUri() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/test");
        return request;
    }
}
