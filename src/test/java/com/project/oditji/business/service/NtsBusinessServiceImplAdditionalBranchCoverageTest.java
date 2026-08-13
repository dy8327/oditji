package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
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
 * 국세청 응답의 null/잘못된 첫 항목/상태값 분기를 추가 검증합니다.
 */
class NtsBusinessServiceImplAdditionalBranchCoverageTest {

    private NtsBusinessServiceImpl service;
    private RestClient restClient;
    private RestClient.RequestBodyUriSpec requestSpec;
    private RestClient.RequestBodySpec bodySpec;
    private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        service = new NtsBusinessServiceImpl();

        restClient = mock(RestClient.class);
        requestSpec = mock(RestClient.RequestBodyUriSpec.class);
        bodySpec = mock(RestClient.RequestBodySpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.post()).thenReturn(requestSpec);
        when(requestSpec.uri(anyUriFunction())).thenReturn(bodySpec);
        when(bodySpec.body(any(Map.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);

        ReflectionTestUtils.setField(service, "restClient", restClient);
        ReflectionTestUtils.setField(service, "serviceKey", "key");
    }

    @Test
    void nullTopLevelResponseShouldUseConfiguredMessage() {
        when(responseSpec.body(Map.class))
                .thenReturn(null);

        NtsBusinessVerifyVO result =
                service.verifyBusiness(
                        "1234567890",
                        "대표자",
                        "20260101");

        assertFalse(result.isValid());
        assertEquals(
                "국세청 응답이 없습니다.",
                result.getMessage());
    }

    @Test
    void nonMapFirstDataEntryShouldUseInvalidFormatMessage() {
        when(responseSpec.body(Map.class))
                .thenReturn(
                        Map.of(
                                "data",
                                List.of("invalid")));

        NtsBusinessVerifyVO result =
                service.verifyBusiness(
                        "1234567890",
                        "대표자",
                        "20260101");

        assertFalse(result.isValid());
        assertEquals(
                "국세청 응답 형식이 올바르지 않습니다.",
                result.getMessage());
    }

    @Test
    void nullBusinessStatusShouldUseUnknownStatusMessage() {
        HashMap<String, Object> valid =
                new HashMap<String, Object>();
        valid.put("valid", "01");

        HashMap<String, Object> status =
                new HashMap<String, Object>();
        status.put("b_stt", null);

        when(responseSpec.body(Map.class))
                .thenReturn(
                        response(valid),
                        response(status));

        NtsBusinessVerifyVO result =
                service.verifyBusiness(
                        "123-45-67890",
                        " 대표자 ",
                        "20260101");

        assertFalse(result.isValid());
        assertEquals(
                "국세청에서 사업자 상태를 확인할 수 없습니다.",
                result.getMessage());
    }

    @Test
    void nonContinuingStatusShouldReturnOriginalTrimmedStatus() {
        when(responseSpec.body(Map.class))
                .thenReturn(
                        response(Map.of("valid", "01")),
                        response(Map.of("b_stt", " 휴업자 ")));

        NtsBusinessVerifyVO result =
                service.verifyBusiness(
                        "1234567890",
                        "대표자",
                        "20260101");

        assertFalse(result.isValid());
        assertEquals("휴업자", result.getBusinessStatus());
        assertTrueMessage(result.getMessage(), "휴업자");
    }

    private void assertTrueMessage(String message, String token) {
        org.junit.jupiter.api.Assertions.assertTrue(message.contains(token));
    }

    private Map<String, Object> response(Map<String, Object> first) {
        return Map.of("data", List.of(first));
    }

    @SuppressWarnings("unchecked")
    private Function<UriBuilder, java.net.URI> anyUriFunction() {
        return any(Function.class);
    }
}
