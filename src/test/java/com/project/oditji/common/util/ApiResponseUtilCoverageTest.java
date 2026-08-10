package com.project.oditji.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

/**
 * 공통 API 응답 유틸의 성공, 커스터마이저, 검증 예외, 일반 예외를 검증합니다.
 */
class ApiResponseUtilCoverageTest {

    @Test
    void basicSuccessFailureAndLoginRequiredResponsesShouldContainExpectedFlags() {
        Map<String, Object> success =
                ApiResponseUtil.success(
                        "성공");
        Map<String, Object> failure =
                ApiResponseUtil.failure(
                        "실패");
        Map<String, Object> login =
                ApiResponseUtil.loginRequired();

        assertEquals(
                Boolean.TRUE,
                success.get("success"));
        assertEquals(
                "성공",
                success.get("message"));

        assertEquals(
                Boolean.FALSE,
                failure.get("success"));

        assertEquals(
                Boolean.FALSE,
                login.get("success"));
        assertEquals(
                Boolean.TRUE,
                login.get("loginRequired"));
    }

    @Test
    void executeShouldRunCustomizerWhenPresent() {
        AtomicBoolean actionCalled =
                new AtomicBoolean(false);

        Map<String, Object> result =
                ApiResponseUtil.execute(
                        () -> actionCalled.set(true),
                        "완료",
                        "실패",
                        mock(Logger.class),
                        "로그",
                        response ->
                                response.put(
                                        "value",
                                        7));

        assertTrue(
                actionCalled.get());
        assertEquals(
                Boolean.TRUE,
                result.get("success"));
        assertEquals(
                7,
                result.get("value"));
    }

    @Test
    void executeShouldAllowNullCustomizer() {
        Map<String, Object> result =
                ApiResponseUtil.execute(
                        () -> {
                        },
                        "완료",
                        "실패",
                        mock(Logger.class),
                        "로그",
                        null);

        assertEquals(
                Boolean.TRUE,
                result.get("success"));
    }

    @Test
    void illegalArgumentShouldReturnOriginalMessageWithoutLoggingGenericError() {
        Logger logger =
                mock(Logger.class);

        Map<String, Object> result =
                ApiResponseUtil.execute(
                        () -> {
                            throw new IllegalArgumentException(
                                    "검증 실패");
                        },
                        "완료",
                        "일반 실패",
                        logger,
                        "로그",
                        null);

        assertEquals(
                Boolean.FALSE,
                result.get("success"));
        assertEquals(
                "검증 실패",
                result.get("message"));
    }

    @Test
    void genericExceptionShouldCoverEnabledAndDisabledErrorLoggingBranches() {
        Logger disabledLogger =
                mock(Logger.class);
        when(disabledLogger.isErrorEnabled())
                .thenReturn(false);

        Map<String, Object> disabled =
                ApiResponseUtil.execute(
                        () -> {
                            throw new IllegalStateException(
                                    "boom");
                        },
                        "완료",
                        "일반 실패",
                        disabledLogger,
                        "로그",
                        null);

        assertFalse(
                (Boolean)
                        disabled.get(
                                "success"));

        Logger enabledLogger =
                mock(Logger.class);
        when(enabledLogger.isErrorEnabled())
                .thenReturn(true);

        Map<String, Object> enabled =
                ApiResponseUtil.execute(
                        () -> {
                            throw new IllegalStateException(
                                    "boom");
                        },
                        "완료",
                        "일반 실패",
                        enabledLogger,
                        "로그",
                        null);

        assertEquals(
                "일반 실패",
                enabled.get(
                        "message"));

        verify(enabledLogger)
                .error(
                        org.mockito.ArgumentMatchers.eq(
                                "로그"),
                        org.mockito.ArgumentMatchers.any(
                                IllegalStateException.class));
    }
}
