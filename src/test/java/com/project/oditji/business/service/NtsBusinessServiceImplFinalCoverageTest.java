package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
 * NTS 응답의 빈 data 및 null valid/status 값 분기를 추가로 검증합니다.
 */
class NtsBusinessServiceImplFinalCoverageTest {

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
    void nullValidationFlagShouldReturnMismatchInsteadOfThrowing() {
        HashMap<String, Object> first =
                new HashMap<String, Object>();
        first.put("valid", null);

        when(responseSpec.body(Map.class))
                .thenReturn(response(first));

        NtsBusinessVerifyVO result =
                service.verifyBusiness(
                        "123-45-67890",
                        " 대표자 ",
                        "20260101");

        assertFalse(result.isValid());
        assertEquals(
                "사업자등록번호, 대표자명 또는 개업일이 국세청 등록정보와 일치하지 않습니다.",
                result.getMessage());
    }

    @Test
    void emptyArrayListShouldUseNoDataBranch() {
        Map<String, Object> response =
                Map.of(
                        "data",
                        new ArrayList<Object>());

        when(responseSpec.body(Map.class))
                .thenReturn(response);

        NtsBusinessVerifyVO result =
                service.verifyBusiness(
                        "1234567890",
                        "대표자",
                        "20260101");

        assertFalse(result.isValid());
        assertEquals(
                "국세청 진위확인 결과가 없습니다.",
                result.getMessage());
    }

    private Map<String, Object> response(
            Map<String, Object> first) {

        return Map.of(
                "data",
                List.of(first));
    }

    @SuppressWarnings("unchecked")
    private Function<UriBuilder, java.net.URI> anyUriFunction() {
        return any(Function.class);
    }
}
