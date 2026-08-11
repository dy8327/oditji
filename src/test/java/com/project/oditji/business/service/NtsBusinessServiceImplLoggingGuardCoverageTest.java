package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import com.project.oditji.business.vo.NtsBusinessVerifyVO;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/** 국세청 API 일반 예외 처리의 ERROR 로그 비활성 분기를 보완합니다. */
class NtsBusinessServiceImplLoggingGuardCoverageTest {

    @Test
    void validationRequestFailureShouldUseSafeMessageWithErrorLoggingDisabled() {
        NtsBusinessServiceImpl service = new NtsBusinessServiceImpl();
        RestClient restClient = mock(RestClient.class);
        when(restClient.post()).thenThrow(new IllegalStateException("validation failure"));

        ReflectionTestUtils.setField(service, "restClient", restClient);
        ReflectionTestUtils.setField(service, "serviceKey", "key");

        Logger logger = (Logger) LoggerFactory.getLogger(NtsBusinessServiceImpl.class);
        Level originalLevel = logger.getLevel();

        try {
            logger.setLevel(Level.OFF);
            NtsBusinessVerifyVO result = service.verifyBusiness(
                    "123-45-67890",
                    "대표자",
                    "20260101");

            assertFalse(result.isValid());
            assertEquals("국세청 사업자 확인 중 오류가 발생했습니다.", result.getMessage());
        } finally {
            logger.setLevel(originalLevel);
        }
    }

    @Test
    void statusRequestFailureShouldUseSafeMessageWithErrorLoggingDisabled() {
        NtsBusinessServiceImpl service = new NtsBusinessServiceImpl();
        RestClient restClient = mock(RestClient.class);
        RestClient.RequestBodyUriSpec requestSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.post())
                .thenReturn(requestSpec)
                .thenThrow(new IllegalStateException("status failure"));
        when(requestSpec.uri(anyUriFunction())).thenReturn(bodySpec);
        when(bodySpec.body(any(Map.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Map.class)).thenReturn(
                Map.of("data", List.of(Map.of("valid", "01"))));

        ReflectionTestUtils.setField(service, "restClient", restClient);
        ReflectionTestUtils.setField(service, "serviceKey", "key");

        Logger logger = (Logger) LoggerFactory.getLogger(NtsBusinessServiceImpl.class);
        Level originalLevel = logger.getLevel();

        try {
            logger.setLevel(Level.OFF);
            NtsBusinessVerifyVO result = service.verifyBusiness(
                    "1234567890",
                    "대표자",
                    "20260101");

            assertFalse(result.isValid());
            assertEquals("국세청 사업자 상태조회 중 오류가 발생했습니다.", result.getMessage());
        } finally {
            logger.setLevel(originalLevel);
        }
    }

    @SuppressWarnings("unchecked")
    private Function<UriBuilder, java.net.URI> anyUriFunction() {
        return any(Function.class);
    }
}
