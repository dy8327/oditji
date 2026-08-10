package com.project.oditji.verify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.verify.dao.VerifyDAO;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * VerifyServiceImpl의 remaining JsonNode OR 조건과 성인판정 경계값을 보완합니다.
 */
class VerifyServiceImplMoreConditionCoverageTest {

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

        ReflectionTestUtils.setField(
                service,
                "clock",
                Clock.fixed(
                        Instant.parse(
                                "2026-08-10T00:00:00Z"),
                        ZoneOffset.UTC));
    }

    @Test
    void verifyIdValidationShouldCoverBlankSecondOperandAndValidBoundary() {
        assertEquals(
                "본인인증 요청 ID가 없습니다.",
                invoke(
                        "validateVerifyId",
                        "   "));

        assertNull(
                invoke(
                        "validateVerifyId",
                        "a".repeat(80)));
    }

    @Test
    void identityAndCustomerExtractorsShouldCoverExplicitNullNodes() throws Exception {
        JsonNode root =
                mapper.readTree(
                        "{\"identityVerification\":null,"
                                + "\"status\":\"VERIFIED\"}");

        JsonNode identity =
                invoke(
                        "extractIdentityVerification",
                        root);

        assertEquals(
                "VERIFIED",
                identity.path("status")
                        .asString());

        JsonNode customerRoot =
                mapper.readTree(
                        "{\"verifiedCustomer\":null,"
                                + "\"customer\":{\"name\":\"홍길동\"}}");

        JsonNode customer =
                invoke(
                        "extractVerifiedCustomer",
                        customerRoot);

        assertEquals(
                "홍길동",
                customer.path("name")
                        .asString());
    }

    @Test
    void getStringShouldCoverMissingNodeAndExplicitNullAndBlankText() throws Exception {
        JsonNode missing =
                mapper.readTree("{}");

        assertNull(
                invoke(
                        "getString",
                        missing,
                        "name"));

        JsonNode values =
                mapper.readTree(
                        "{\"nil\":null,\"blank\":\"   \",\"name\":\"홍길동\"}");

        assertNull(
                invoke(
                        "getString",
                        values,
                        "nil"));
        assertNull(
                invoke(
                        "getString",
                        values,
                        "blank"));
        assertEquals(
                "홍길동",
                invoke(
                        "getString",
                        values,
                        "name"));
    }

    @Test
    void adultCheckShouldCoverFutureDateAndExactNineteenYearBoundary() {
        assertEquals(
                Boolean.TRUE,
                invoke(
                        "isAdult",
                        "2007-08-10"));

        assertEquals(
                Boolean.FALSE,
                invoke(
                        "isAdult",
                        "2007-08-11"));

        assertEquals(
                Boolean.FALSE,
                invoke(
                        "isAdult",
                        "2027-01-01"));
    }

    @Test
    void returnUrlShouldCoverEachRejectedPrefixAndLineBreakOperand() {
        assertEquals(
                "/",
                invoke(
                        "sanitizeReturnUrl",
                        "https://evil.test"));
        assertEquals(
                "/",
                invoke(
                        "sanitizeReturnUrl",
                        "//evil.test"));
        assertEquals(
                "/",
                invoke(
                        "sanitizeReturnUrl",
                        "/\\evil.test"));
        assertEquals(
                "/",
                invoke(
                        "sanitizeReturnUrl",
                        "/safe\rInjected"));
        assertEquals(
                "/",
                invoke(
                        "sanitizeReturnUrl",
                        "/safe\nInjected"));
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(
            String methodName,
            Object... arguments) {

        return (T)
                ReflectionTestUtils.invokeMethod(
                        service,
                        methodName,
                        arguments);
    }
}
