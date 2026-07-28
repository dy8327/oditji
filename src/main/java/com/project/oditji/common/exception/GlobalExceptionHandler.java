package com.project.oditji.common.exception;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 존재하지 않는 페이지 및 리소스
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public Object handleNotFoundException(Exception e, HttpServletRequest request) {
        return createErrorResponse(request, HttpStatus.NOT_FOUND, "요청한 페이지를 찾을 수 없습니다.", "error/404");
    }

    // 처리되지 않은 전체 예외
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request) {
        if (log.isErrorEnabled()) {
            log.error("처리되지 않은 오류 발생 - {} {}", request.getMethod(), request.getRequestURI(), e);
        }
        return createErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR,
                "요청 처리 중 오류가 발생했습니다.", "error/500");
    }

    private Object createErrorResponse(
            HttpServletRequest request,
            HttpStatus status,
            String message,
            String viewName) {

        if (isJsonRequest(request)) {
            return ResponseEntity.status(status).body(Map.of(
                    "success", false,
                    "message", message
            ));
        }

        ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.setStatus(status);
        return modelAndView;
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");

        return uri.contains("/api/")
                || "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE);
    }
}