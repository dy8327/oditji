package com.project.oditji.common.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;

/**
 * JSON API 컨트롤러에서 공통으로 사용하는 성공/실패 응답을 생성합니다.
 */
public final class ApiResponseUtil {

    private ApiResponseUtil() {
        // 인스턴스 생성 방지
    }

    public static Map<String, Object> success(String message) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }

    public static Map<String, Object> failure(String message) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }

    public static Map<String, Object> loginRequired() {
        Map<String, Object> response = failure("로그인이 필요한 서비스입니다.");
        response.put("loginRequired", true);
        return response;
    }
    public static Map<String, Object> execute(
            Runnable action,
            String successMessage,
            String errorMessage,
            Logger logger,
            String logMessage,
            Consumer<Map<String, Object>> successCustomizer) {

        try {
            action.run();
            Map<String, Object> response = success(successMessage);

            if (successCustomizer != null) {
                successCustomizer.accept(response);
            }

            return response;

        } catch (IllegalArgumentException e) {
            return failure(e.getMessage());

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(logMessage, e);
            }
            return failure(errorMessage);
        }
    }

}
