package com.project.oditji.verify.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.verify.dao.VerifyDAO;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** JsonNode 자체가 MissingNode/NullNode인 경우의 OR 단락평가 잔여 조건을 보완합니다. */
class VerifyServiceImplJsonNodeShortCircuitCoverageTest {

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
    void birthDateExtractorShouldCoverTopLevelMissingNodeAndNullNodeOperands() throws Exception {
        JsonNode missingNode = mapper.readTree("{}").path("missing");
        JsonNode nullNode = mapper.readTree("null");

        assertNull(invoke("extractBirthDate", missingNode));
        assertNull(invoke("extractBirthDate", nullNode));
    }

    @Test
    void stringExtractorShouldCoverTopLevelMissingNodeAndNullNodeOperands() throws Exception {
        JsonNode missingNode = mapper.readTree("{}").path("missing");
        JsonNode nullNode = mapper.readTree("null");

        assertNull(invoke("getString", missingNode, "name"));
        assertNull(invoke("getString", nullNode, "name"));
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String methodName, Object... arguments) {
        return (T) ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
    }
}
