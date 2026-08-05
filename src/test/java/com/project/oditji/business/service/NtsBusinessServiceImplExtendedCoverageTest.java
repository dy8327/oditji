package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

/** 국세청 진위확인·상태조회 응답의 정상, 불일치, 빈 응답과 오류 분기를 검증합니다. */
class NtsBusinessServiceImplExtendedCoverageTest {

    private NtsBusinessServiceImpl service;
    private RestClient restClient;
    private RestClient.RequestBodyUriSpec requestSpec;
    private RestClient.RequestBodySpec bodySpec;
    private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        service = new NtsBusinessServiceImpl();
        ReflectionTestUtils.setField(service, "serviceKey", "service-key");

        restClient = mock(RestClient.class);
        requestSpec = mock(RestClient.RequestBodyUriSpec.class);
        bodySpec = mock(RestClient.RequestBodySpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.post()).thenReturn(requestSpec);
        when(requestSpec.uri(anyUriFunction())).thenReturn(bodySpec);
        when(bodySpec.body(any(Map.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);

        ReflectionTestUtils.setField(service, "restClient", restClient);
    }

    @Test
    void verifiedOngoingBusinessShouldReturnSuccess() {
        when(responseSpec.body(Map.class))
                .thenReturn(response(Map.of("valid", "01")))
                .thenReturn(response(Map.of("b_stt", "계속사업자")));

        NtsBusinessVerifyVO result = service.verifyBusiness(
                "123-45-67890",
                " 대표자 ",
                "20260101");

        assertTrue(result.isValid());
        assertEquals("계속사업자", result.getBusinessStatus());
        assertTrue(result.getMessage().contains("확인되었습니다"));
    }

    @Test
    void validationMismatchShouldReturnFriendlyFailure() {
        when(responseSpec.body(Map.class))
                .thenReturn(response(Map.of("valid", "02")));

        NtsBusinessVerifyVO result = service.verifyBusiness(
                "1234567890",
                "대표자",
                "20260101");

        assertFalse(result.isValid());
        assertNull(result.getBusinessStatus());
        assertTrue(result.getMessage().contains("일치하지 않습니다"));
    }

    @Test
    void blankAndClosedStatusShouldReturnDifferentMessages() {
        when(responseSpec.body(Map.class))
                .thenReturn(response(Map.of("valid", "01")))
                .thenReturn(response(Map.of("b_stt", " ")));

        NtsBusinessVerifyVO blank = service.verifyBusiness(
                "1234567890",
                "대표자",
                "20260101");
        assertFalse(blank.isValid());
        assertNull(blank.getBusinessStatus());
        assertTrue(blank.getMessage().contains("상태를 확인할 수 없습니다"));

        when(responseSpec.body(Map.class))
                .thenReturn(response(Map.of("valid", "01")))
                .thenReturn(response(Map.of("b_stt", "폐업자")));

        NtsBusinessVerifyVO closed = service.verifyBusiness(
                "1234567890",
                "대표자",
                "20260101");
        assertFalse(closed.isValid());
        assertEquals("폐업자", closed.getBusinessStatus());
        assertTrue(closed.getMessage().contains("가입할 수 없습니다"));
    }

    @Test
    void nullStatusValueShouldBeHandledAsUnknown() {
        java.util.HashMap<String, Object> status = new java.util.HashMap<String, Object>();
        status.put("b_stt", null);
        when(responseSpec.body(Map.class))
                .thenReturn(response(Map.of("valid", "01")))
                .thenReturn(response(status));

        NtsBusinessVerifyVO result = service.verifyBusiness(
                "1234567890",
                "대표자",
                "20260101");

        assertFalse(result.isValid());
        assertNull(result.getBusinessStatus());
    }

    @Test
    void missingValidationResponseDataAndInvalidFormatShouldReturnExactMessages() {
        when(responseSpec.body(Map.class)).thenReturn(null);
        NtsBusinessVerifyVO noResponse = service.verifyBusiness(
                "1234567890",
                "대표자",
                "20260101");
        assertEquals("국세청 응답이 없습니다.", noResponse.getMessage());

        when(responseSpec.body(Map.class)).thenReturn(Map.of("data", List.of()));
        NtsBusinessVerifyVO noData = service.verifyBusiness(
                "1234567890",
                "대표자",
                "20260101");
        assertEquals("국세청 진위확인 결과가 없습니다.", noData.getMessage());

        when(responseSpec.body(Map.class)).thenReturn(Map.of("data", List.of("invalid")));
        NtsBusinessVerifyVO invalidFormat = service.verifyBusiness(
                "1234567890",
                "대표자",
                "20260101");
        assertEquals("국세청 응답 형식이 올바르지 않습니다.", invalidFormat.getMessage());
    }

    @Test
    void statusResponseFailuresShouldUseStatusSpecificMessages() {
        when(responseSpec.body(Map.class))
                .thenReturn(response(Map.of("valid", "01")))
                .thenReturn(null);
        NtsBusinessVerifyVO noResponse = service.verifyBusiness(
                "1234567890",
                "대표자",
                "20260101");
        assertEquals("사업자 상태조회 응답이 없습니다.", noResponse.getMessage());

        when(responseSpec.body(Map.class))
                .thenReturn(response(Map.of("valid", "01")))
                .thenReturn(Map.of("data", List.of()));
        NtsBusinessVerifyVO noData = service.verifyBusiness(
                "1234567890",
                "대표자",
                "20260101");
        assertEquals("사업자 상태조회 결과가 없습니다.", noData.getMessage());

        when(responseSpec.body(Map.class))
                .thenReturn(response(Map.of("valid", "01")))
                .thenReturn(Map.of("data", List.of("invalid")));
        NtsBusinessVerifyVO invalid = service.verifyBusiness(
                "1234567890",
                "대표자",
                "20260101");
        assertEquals("사업자 상태조회 응답 형식이 올바르지 않습니다.", invalid.getMessage());
    }

    @Test
    void unexpectedClientExceptionShouldReturnGenericMessage() {
        when(responseSpec.body(Map.class))
                .thenThrow(new IllegalStateException("network"));

        NtsBusinessVerifyVO result = service.verifyBusiness(
                "1234567890",
                "대표자",
                "20260101");

        assertFalse(result.isValid());
        assertEquals("국세청 사업자 확인 중 오류가 발생했습니다.", result.getMessage());
    }

    private Map<String, Object> response(Map<String, Object> first) {
        return Map.of("data", List.of(first));
    }

    @SuppressWarnings("unchecked")
    private Function<UriBuilder, java.net.URI> anyUriFunction() {
        return any(Function.class);
    }
}
