package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import com.project.oditji.business.vo.NtsBusinessVerifyVO;

/**
 * 국세청 진위확인 첫 API 호출의 일반 예외 catch 라인을 검증합니다.
 */
class NtsBusinessServiceImplGenericFailureCoverageTest {

    private NtsBusinessServiceImpl service;
    private RestClient restClient;

    @BeforeEach
    void setUp() {
        service = new NtsBusinessServiceImpl();
        restClient = mock(RestClient.class);

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
    void genericValidateRequestFailureShouldReturnSafeGenericMessage() {
        when(restClient.post())
                .thenThrow(
                        new IllegalStateException(
                                "network failure"));

        NtsBusinessVerifyVO result =
                service.verifyBusiness(
                        "123-45-67890",
                        " 대표자 ",
                        "20260101");

        assertFalse(result.isValid());
        assertEquals(
                "국세청 사업자 확인 중 오류가 발생했습니다.",
                result.getMessage());
    }
}
