package com.project.oditji.common.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 서블릿 컨테이너와 Spring Security 필터 단계에서 발생한 오류를
 * ODITJI 공통 안내 화면 또는 JSON 응답으로 변환한다.
 *
 * Controller 내부 예외는 GlobalExceptionHandler가 처리하고,
 * 매핑되지 않은 경로·CSRF 거부·인터셉터 sendError·JSP 렌더링 오류처럼
 * /error로 전달되는 오류는 이 컨트롤러가 처리한다.
 */
@Controller
public class CustomErrorController implements ErrorController {

    private static final Logger log = LoggerFactory.getLogger(CustomErrorController.class);
    private static final String VIEW_ERROR_COMMON = "error/common";

    @RequestMapping(
            path = "/error",
            method = {
                    RequestMethod.GET,
                    RequestMethod.HEAD,
                    RequestMethod.POST,
                    RequestMethod.PUT,
                    RequestMethod.PATCH,
                    RequestMethod.DELETE,
                    RequestMethod.OPTIONS
            })
    public Object handleError(HttpServletRequest request) {
        HttpStatus status = resolveStatus(request);
        String message = resolveMessage(status);
        String originalUri = resolveOriginalUri(request);

        if (status.is5xxServerError()) {
            if (log.isErrorEnabled()) {
                Object error = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
                Throwable exception = error instanceof Throwable throwable ? throwable : null;
                log.error("서버 오류 응답 - status={}, uri={}", status.value(), originalUri, exception);
            }
        } else if (log.isWarnEnabled()) {
            log.warn("요청 오류 응답 - status={}, uri={}", status.value(), originalUri);
        }

        if (isJsonRequest(request, originalUri)) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("success", false);
            body.put("status", status.value());
            body.put("message", message);
            return ResponseEntity.status(status).body(body);
        }

        ModelAndView modelAndView = new ModelAndView(resolveViewName(status));
        modelAndView.setStatus(status);
        modelAndView.addObject("errorCode", status.value());
        modelAndView.addObject("errorMessage", message);
        return modelAndView;
    }

    private HttpStatus resolveStatus(HttpServletRequest request) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (statusCode instanceof Integer code) {
            HttpStatus resolved = HttpStatus.resolve(code);
            if (resolved != null) {
                return resolved;
            }
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveViewName(HttpStatus status) {
        if (status == HttpStatus.FORBIDDEN) {
            return "error/403";
        }
        if (status == HttpStatus.NOT_FOUND) {
            return "error/404";
        }
        if (status.is5xxServerError()) {
            return "error/500";
        }
        return VIEW_ERROR_COMMON;
    }

    private String resolveMessage(HttpStatus status) {
        if (status == HttpStatus.FORBIDDEN) {
            return "해당 페이지에 접근할 수 있는 권한이 없습니다.";
        }
        if (status == HttpStatus.NOT_FOUND) {
            return "요청하신 페이지가 존재하지 않거나 이동되었습니다.";
        }
        if (status.is5xxServerError()) {
            return "요청 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
        if (status == HttpStatus.BAD_REQUEST) {
            return "요청 정보가 올바르지 않습니다.";
        }
        if (status == HttpStatus.METHOD_NOT_ALLOWED) {
            return "허용되지 않은 요청 방식입니다.";
        }
        return "요청을 처리할 수 없습니다.";
    }

    private String resolveOriginalUri(HttpServletRequest request) {
        Object originalUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        return originalUri == null ? request.getRequestURI() : originalUri.toString();
    }

    private boolean isJsonRequest(HttpServletRequest request, String originalUri) {
        String accept = request.getHeader("Accept");
        String contentType = request.getContentType();
        String requestedWith = request.getHeader("X-Requested-With");

        return originalUri.contains("/api/")
                || "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)
                || contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE);
    }
}