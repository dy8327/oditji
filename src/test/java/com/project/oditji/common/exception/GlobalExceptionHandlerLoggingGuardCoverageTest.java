package com.project.oditji.common.exception;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * GlobalExceptionHandler의 로그 레벨 guard가 false인 조건을 보완합니다.
 */
class GlobalExceptionHandlerLoggingGuardCoverageTest {

    private GlobalExceptionHandler handler;
    private Logger targetLogger;
    private Level originalLevel;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        targetLogger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        originalLevel = targetLogger.getLevel();
        targetLogger.setLevel(Level.OFF);
    }

    @AfterEach
    void tearDown() {
        targetLogger.setLevel(originalLevel);
    }

    @Test
    void warnAndErrorLogGuardsShouldAlsoCoverDisabledBranches() {
        MockHttpServletRequest request = htmlRequest("/coverage/log-disabled");

        assertNotNull(handler.handleAccessDeniedException(
                new AccessDeniedException("denied"), request));
        assertNotNull(handler.handleIllegalArgumentException(
                new IllegalArgumentException("bad request"), request));
        assertNotNull(handler.handleMissingParameter(
                mock(MissingServletRequestParameterException.class), request));
        assertNotNull(handler.handleMethodNotSupported(
                mock(HttpRequestMethodNotSupportedException.class), request));
        assertNotNull(handler.handleTypeMismatch(
                mock(MethodArgumentTypeMismatchException.class), request));
        assertNotNull(handler.handleResponseStatusException(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "bad"), request));
        assertNotNull(handler.handleMaxUploadSizeExceededException(
                mock(MaxUploadSizeExceededException.class), request));
        assertNotNull(handler.handleDataIntegrityViolationException(
                new DataIntegrityViolationException("conflict"), request));
        assertNotNull(handler.handleHttpMessageNotReadableException(
                mock(HttpMessageNotReadableException.class), request));
        assertNotNull(handler.handleException(
                new Exception("unexpected"), request));
    }

    private MockHttpServletRequest htmlRequest(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI(uri);
        return request;
    }
}
