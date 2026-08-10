package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import com.project.oditji.business.vo.NtsBusinessVerifyVO;

/**
 * 진위확인 성공 후 상태조회 단계의 일반 예외 catch 라인을 검증합니다.
 */
class NtsBusinessServiceImplStatusFailureCoverageTest {

    private NtsBusinessServiceImpl service;
    private RestClient restClient;
    private RestClient.RequestBodyUriSpec requestSpec;
    private RestClient.RequestBodySpec bodySpec;
    private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        service = new NtsBusinessServiceImpl();

        restClient = mock(RestClient.class);
        requestSpec =
                mock(RestClient.RequestBodyUriSpec.class);
        bodySpec =
                mock(RestClient.RequestBodySpec.class);
        responseSpec =
                mock(RestClient.ResponseSpec.class);

        ReflectionTestUtils.setField(
                service,
                "restClient",
                restClient);
        ReflectionTestUtils.setField(
                service,
                "serviceKey",
                "key");
    }

    @Test
    void statusRequestRuntimeFailureShouldReturnSafeStatusLookupMessage() {
        when(restClient.post())
                .thenReturn(requestSpec)
                .thenThrow(
                        new IllegalStateException(
                                "status network failure"));

        when(requestSpec.uri(anyUriFunction()))
                .thenReturn(bodySpec);
        when(bodySpec.body(any(Map.class)))
                .thenReturn(bodySpec);
        when(bodySpec.retrieve())
                .thenReturn(responseSpec);
        when(responseSpec.body(Map.class))
                .thenReturn(
                        Map.of(
                                "data",
                                List.of(
                                        Map.of(
                                                "valid",
                                                "01"))));

        NtsBusinessVerifyVO result =
                service.verifyBusiness(
                        "1234567890",
                        "대표자",
                        "20260101");

        assertFalse(result.isValid());
        assertEquals(
                "국세청 사업자 상태조회 중 오류가 발생했습니다.",
                result.getMessage());
    }

    @SuppressWarnings("unchecked")
    private Function<UriBuilder, java.net.URI> anyUriFunction() {
        return any(Function.class);
    }
}
