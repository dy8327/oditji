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
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 존재하지 않는 페이지 및 리소스
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public Object handleNotFoundException(Exception e, HttpServletRequest request) {
        return createErrorResponse(request, HttpStatus.NOT_FOUND, "요청한 페이지를 찾을 수 없습니다.", "error/404");
    }

    // 잘못된 요청 및 입력값 예외
    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {

        if (log.isWarnEnabled()) {
            log.warn("잘못된 요청 - {} {} : {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        }

        return createErrorResponse(request, HttpStatus.BAD_REQUEST, e.getMessage(),"error/common");
    }

    // 필수 요청값 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object handleMissingParameter(MissingServletRequestParameterException e, HttpServletRequest request) {
        if (log.isWarnEnabled()) {
            log.warn("필수 요청값 누락 - {} {} : {}", request.getMethod(), request.getRequestURI(), e.getParameterName());
        }
        return createErrorResponse(request, HttpStatus.BAD_REQUEST, "필수 입력값이 누락되었습니다.", "error/common");
    }

    // 허용되지 않은 요청 방식
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {

        if (log.isWarnEnabled()) {
            log.warn("허용되지 않은 요청 방식 - {} {}", request.getMethod(), request.getRequestURI());
        }

        return createErrorResponse(request, HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 요청 방식입니다.", "error/common");
    }

    // 요청값 형식 오류
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Object handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {

        if (log.isWarnEnabled()) {
            log.warn("요청값 형식 오류 - {} {} : {}",request.getMethod(), request.getRequestURI(), e.getName());
        }

        return createErrorResponse(request, HttpStatus.BAD_REQUEST, "요청값 형식이 올바르지 않습니다.", "error/common");
    }

    // 상태 코드가 지정된 요청 예외
    @ExceptionHandler(ResponseStatusException.class)
    public Object handleResponseStatusException(ResponseStatusException e, HttpServletRequest request) {

        if (log.isWarnEnabled()) {
            log.warn("요청 처리 실패 - {} {} : {}", request.getMethod(), request.getRequestURI(), e.getReason());
        }

        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        String message = e.getReason() == null ? "요청을 처리할 수 없습니다." : e.getReason();
        String viewName = status == HttpStatus.NOT_FOUND ? "error/404" : "error/500";

        return createErrorResponse(request, status, message, viewName);
    }

    // 업로드 파일 용량 초과
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {

        if (log.isWarnEnabled()) {
            log.warn("파일 업로드 용량 초과 - {} {}", request.getMethod(), request.getRequestURI());
        }

        return createErrorResponse(request, HttpStatus.CONTENT_TOO_LARGE, "파일은 한 개당 최대 10MB까지 업로드할 수 있습니다.",  "error/common");
    }

    // 데이터 무결성 및 중복 오류
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Object handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        if (log.isErrorEnabled()) {
            log.error("데이터 처리 오류 - {} {}", request.getMethod(), request.getRequestURI(), e);
        }
        return createErrorResponse(request, HttpStatus.CONFLICT,
                "이미 등록된 정보이거나 처리할 수 없는 데이터입니다.", "error/common");
    }

    // 요청 본문 형식 오류(JSON형식 오류 처리)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Object handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        if (log.isWarnEnabled()) {
            log.warn("요청 본문 형식 오류 - {} {}", request.getMethod(), request.getRequestURI());
        }
        return createErrorResponse(request, HttpStatus.BAD_REQUEST,
                "요청 데이터 형식이 올바르지 않습니다.", "error/common");
    }

    // 처리되지 않은 전체 예외
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request) {
        if (log.isErrorEnabled()) {
            log.error("처리되지 않은 오류 발생 - {} {}", request.getMethod(), request.getRequestURI(), e);
        }
        return createErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR,"요청 처리 중 오류가 발생했습니다.", "error/500");
    }

    private Object createErrorResponse(HttpServletRequest request, HttpStatus status, String message, String viewName) {

        if (isJsonRequest(request)) {
            return ResponseEntity.status(status).body(Map.of("success", false, "message", message));
        }

        ModelAndView modelAndView = new ModelAndView(viewName);
        modelAndView.setStatus(status);
        modelAndView.addObject("errorCode", status.value());
        modelAndView.addObject("errorMessage", message);
        return modelAndView;
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        String contentType = request.getContentType();
        String requestedWith = request.getHeader("X-Requested-With");

        return uri.contains("/api/")
                || "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)
                || contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE);
    }
}