package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
 * 기존 국세청 서비스 테스트에서 남은 입력 검증 및 예외 분기를 보완합니다.
 */
class NtsBusinessServiceImplRemainingCoverageTest {

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
    void requiredValueValidationShouldCoverNullAndBlankSides() {
        IllegalArgumentException nullBusinessNumber = assertThrows(
                IllegalArgumentException.class,
                () -> service.verifyBusiness(null, "대표자", "20260101"));
        assertEquals(
                "사업자등록번호를 입력해주세요.",
                nullBusinessNumber.getMessage());

        IllegalArgumentException blankRepresentative = assertThrows(
                IllegalArgumentException.class,
                () -> service.verifyBusiness("1234567890", "   ", "20260101"));
        assertEquals(
                "대표자명을 입력해주세요.",
                blankRepresentative.getMessage());

        IllegalArgumentException nullOpenDate = assertThrows(
                IllegalArgumentException.class,
                () -> service.verifyBusiness("1234567890", "대표자", null));
        assertEquals(
                "개업일은 YYYYMMDD 형식으로 입력해주세요.",
                nullOpenDate.getMessage());
    }

    @Test
    void nonListValidationDataShouldUseNoDataMessage() {
        when(responseSpec.body(Map.class))
                .thenReturn(Map.of("data", "invalid"));

        NtsBusinessVerifyVO result = service.verifyBusiness(
                "1234567890",
                "대표자",
                "20260101");

        assertFalse(result.isValid());
        assertEquals(
                "국세청 진위확인 결과가 없습니다.",
                result.getMessage());
    }

    @Test
    void nonListStatusDataShouldUseStatusNoDataMessage() {
        when(responseSpec.body(Map.class))
                .thenReturn(response(Map.of("valid", "01")))
                .thenReturn(Map.of("data", "invalid"));

        NtsBusinessVerifyVO result = service.verifyBusiness(
                "1234567890",
                "대표자",
                "20260101");

        assertFalse(result.isValid());
        assertEquals(
                "사업자 상태조회 결과가 없습니다.",
                result.getMessage());
    }

    @Test
    void unexpectedStatusLookupExceptionShouldUseStatusGenericMessage() {
        when(responseSpec.body(Map.class))
                .thenReturn(response(Map.of("valid", "01")))
                .thenThrow(new IllegalStateException("status network"));

        NtsBusinessVerifyVO result = service.verifyBusiness(
                "1234567890",
                "대표자",
                "20260101");

        assertFalse(result.isValid());
        assertEquals(
                "국세청 사업자 상태조회 중 오류가 발생했습니다.",
                result.getMessage());
    }

    @Test
    void missingDataKeyShouldAlsoUseNoDataMessage() {
        HashMap<String, Object> response = new HashMap<String, Object>();

        when(responseSpec.body(Map.class)).thenReturn(response);

        NtsBusinessVerifyVO result = service.verifyBusiness(
                "1234567890",
                "대표자",
                "20260101");

        assertFalse(result.isValid());
        assertEquals(
                "국세청 진위확인 결과가 없습니다.",
                result.getMessage());
    }

    private Map<String, Object> response(Map<String, Object> first) {
        return Map.of("data", List.of(first));
    }

    @SuppressWarnings("unchecked")
    private Function<UriBuilder, java.net.URI> anyUriFunction() {
        return any(Function.class);
    }
}
