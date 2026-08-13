package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.project.oditji.business.vo.NtsBusinessVerifyVO;

/** RestClient URI 빌더 람다 자체를 실행하여 남은 URI 구성 라인을 검증합니다. */
class NtsBusinessServiceImplUriLambdaCoverageTest {

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

        ReflectionTestUtils.setField(service, "restClient", restClient);
        ReflectionTestUtils.setField(service, "serviceKey", "service-key");

        when(restClient.post()).thenReturn(requestSpec);
        when(requestSpec.uri(anyUriFunction())).thenAnswer(invocation -> {
            Function<UriBuilder, URI> uriFunction = invocation.getArgument(0);
            URI uri = uriFunction.apply(UriComponentsBuilder.newInstance());

            assertEquals("https", uri.getScheme());
            assertEquals("api.odcloud.kr", uri.getHost());
            assertEquals("/api/nts-businessman/v1/validate", uri.getPath());
            assertTrue(uri.getQuery().contains("serviceKey=service-key"));
            return bodySpec;
        });
        when(bodySpec.body(any(Map.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Map.class)).thenReturn(
                Map.of("data", List.of(Map.of("valid", "02"))));
    }

    @Test
    void verifyBusinessShouldExecuteConfiguredUriBuilderFunction() {
        NtsBusinessVerifyVO result = service.verifyBusiness(
                "123-45-67890",
                "대표자",
                "20260101");

        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("일치하지 않습니다"));
    }

    @SuppressWarnings("unchecked")
    private Function<UriBuilder, URI> anyUriFunction() {
        return any(Function.class);
    }
}
