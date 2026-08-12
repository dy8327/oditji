package com.project.oditji.verify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.verify.dao.VerifyDAO;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** 포트원 응답 래퍼의 MissingNode 단락 조건을 보완합니다. */
class VerifyServiceImplMissingNodeOperandCoverageTest {

    private VerifyServiceImpl service;
    private JsonMapper mapper;

    @BeforeEach
    void setUp() {
        service = new VerifyServiceImpl(
                mock(VerifyDAO.class),
                "store",
                "easy",
                "sms",
                "secret");
        mapper = JsonMapper.builder().build();
    }

    @Test
    void identityExtractorShouldReturnRootWhenWrapperFieldIsMissing() throws Exception {
        JsonNode root = mapper.readTree("{\"status\":\"VERIFIED\"}");

        JsonNode extracted = ReflectionTestUtils.invokeMethod(
                service,
                "extractIdentityVerification",
                root);

        assertSame(root, extracted);
        assertEquals("VERIFIED", extracted.path("status").asString());
    }

    @Test
    void customerExtractorShouldUseLegacyCustomerWhenVerifiedCustomerIsMissing() throws Exception {
        JsonNode identity = mapper.readTree(
                "{\"customer\":{\"name\":\"홍길동\"}}");

        JsonNode customer = ReflectionTestUtils.invokeMethod(
                service,
                "extractVerifiedCustomer",
                identity);

        assertEquals("홍길동", customer.path("name").asString());
    }
}
