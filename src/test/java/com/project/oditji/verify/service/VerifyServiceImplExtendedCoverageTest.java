package com.project.oditji.verify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.verify.dao.VerifyDAO;
import com.project.oditji.verify.vo.IdentityVerifyLogVO;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** 성인 판정, 포트원 JSON 추출, 문자열 정규화와 내부 경로 검증 분기를 보완합니다. */
@ExtendWith(MockitoExtension.class)
class VerifyServiceImplExtendedCoverageTest {

    @Mock
    private VerifyDAO verifyDAO;

    private VerifyServiceImpl service;
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        service = new VerifyServiceImpl(
                verifyDAO,
                "store-id",
                "easy-key",
                "sms-key",
                "api-secret");
        jsonMapper = JsonMapper.builder().build();
        ReflectionTestUtils.setField(
                service,
                "clock",
                Clock.fixed(
                        Instant.parse("2026-08-05T00:00:00Z"),
                        ZoneOffset.UTC));
    }

    @Test
    void adultCheckShouldHandleBoundaryFutureBlankAndInvalidDates() {
        assertTrue(invokeBoolean("isAdult", "2007-08-05"));
        assertFalse(invokeBoolean("isAdult", "2007-08-06"));
        assertFalse(invokeBoolean("isAdult", "2027-01-01"));
        assertFalse(invokeBoolean("isAdult", "invalid"));
        assertFalse(invokeBoolean("isAdult", " "));
        assertFalse(invokeBoolean("isAdult", (Object) null));
    }

    @Test
    void identityAndCustomerExtractionShouldSupportWrappedAndLegacyResponses() throws Exception {
        JsonNode wrapped = jsonMapper.readTree(
                "{\"identityVerification\":{\"status\":\"VERIFIED\","
                        + "\"verifiedCustomer\":{\"name\":\"홍길동\"}}}");
        JsonNode identity = ReflectionTestUtils.invokeMethod(
                service,
                "extractIdentityVerification",
                wrapped);
        assertEquals("VERIFIED", identity.path("status").asString());

        JsonNode customer = ReflectionTestUtils.invokeMethod(
                service,
                "extractVerifiedCustomer",
                identity);
        assertEquals("홍길동", customer.path("name").asString());

        JsonNode legacy = jsonMapper.readTree(
                "{\"status\":\"VERIFIED\",\"customer\":{\"name\":\"레거시\"}}");
        JsonNode legacyIdentity = ReflectionTestUtils.invokeMethod(
                service,
                "extractIdentityVerification",
                legacy);
        assertSame(legacy, legacyIdentity);

        JsonNode legacyCustomer = ReflectionTestUtils.invokeMethod(
                service,
                "extractVerifiedCustomer",
                legacyIdentity);
        assertEquals("레거시", legacyCustomer.path("name").asString());
    }

    @Test
    void birthDateExtractionShouldNormalizeEightDigitsAndPreserveOtherValues() throws Exception {
        JsonNode dashed = jsonMapper.readTree("{\"birthDate\":\"2000-01-02\"}");
        JsonNode compact = jsonMapper.readTree("{\"birthDate\":\"20000102\"}");
        JsonNode other = jsonMapper.readTree("{\"birthDate\":\"2000/01/02\"}");
        JsonNode blank = jsonMapper.readTree("{\"birthDate\":\" \"}");
        JsonNode missing = jsonMapper.readTree("{}");

        assertEquals("2000-01-02", invokeString("extractBirthDate", dashed));
        assertEquals("2000-01-02", invokeString("extractBirthDate", compact));
        assertEquals("2000/01/02", invokeString("extractBirthDate", other));
        assertNull(invokeString("extractBirthDate", blank));
        assertNull(invokeString("extractBirthDate", missing));
        assertNull(invokeString("extractBirthDate", (Object) null));
    }

    @Test
    void genderAndStringExtractionShouldHandleAllSupportedShapes() throws Exception {
        assertEquals("MALE", invokeString("normalizeGender", "male"));
        assertEquals("MALE", invokeString("normalizeGender", " M "));
        assertEquals("FEMALE", invokeString("normalizeGender", "female"));
        assertEquals("FEMALE", invokeString("normalizeGender", "f"));
        assertNull(invokeString("normalizeGender", "unknown"));
        assertNull(invokeString("normalizeGender", " "));
        assertNull(invokeString("normalizeGender", (Object) null));

        JsonNode values = jsonMapper.readTree(
                "{\"name\":\"홍길동\",\"blank\":\" \",\"nil\":null}");
        assertEquals("홍길동", ReflectionTestUtils.<String>invokeMethod(
                service,
                "getString",
                values,
                "name"));
        assertNull(ReflectionTestUtils.<String>invokeMethod(
                service,
                "getString",
                values,
                "blank"));
        assertNull(ReflectionTestUtils.<String>invokeMethod(
                service,
                "getString",
                values,
                "nil"));
        assertNull(ReflectionTestUtils.<String>invokeMethod(
                service,
                "getString",
                values,
                "missing"));
        assertNull(ReflectionTestUtils.<String>invokeMethod(
                service,
                "getString",
                null,
                "name"));
    }

    @Test
    void returnUrlSanitizerShouldAllowOnlySafeInternalPaths() {
        assertEquals("/content/1?tab=review", invokeString(
                "sanitizeReturnUrl",
                " /content/1?tab=review "));
        assertEquals("/", invokeString("sanitizeReturnUrl", (Object) null));
        assertEquals("/", invokeString("sanitizeReturnUrl", " "));
        assertEquals("/", invokeString("sanitizeReturnUrl", "https://evil.test"));
        assertEquals("/", invokeString("sanitizeReturnUrl", "//evil.test"));
        assertEquals("/", invokeString("sanitizeReturnUrl", "/\\evil.test"));
        assertEquals("/", invokeString("sanitizeReturnUrl", "/safe\r\nLocation: evil"));
    }

    @Test
    void defaultStringAndVerifyIdValidationShouldCoverBlankAndValidValues() {
        assertEquals("DEFAULT", ReflectionTestUtils.<String>invokeMethod(
                service,
                "defaultString",
                null,
                "DEFAULT"));
        assertEquals("DEFAULT", ReflectionTestUtils.<String>invokeMethod(
                service,
                "defaultString",
                " ",
                "DEFAULT"));
        assertEquals("VALUE", ReflectionTestUtils.<String>invokeMethod(
                service,
                "defaultString",
                "VALUE",
                "DEFAULT"));

        assertEquals("본인인증 요청 ID가 없습니다.", invokeString(
                "validateVerifyId",
                (Object) null));
        assertEquals("본인인증 요청 ID가 없습니다.", invokeString(
                "validateVerifyId",
                " "));
        assertEquals("본인인증 요청 ID 형식이 올바르지 않습니다.", invokeString(
                "validateVerifyId",
                "invalid id!"));
        assertNull(invokeString("validateVerifyId", "valid_id-123"));
    }

    @Test
    void baseAndFailedLogsShouldUseSafeDefaults() {
        IdentityVerifyLogVO base = ReflectionTestUtils.invokeMethod(
                service,
                "createBaseLog",
                10L,
                "verify-1",
                "READY");
        assertEquals(10L, base.getMemberNo());
        assertEquals("verify-1", base.getVerifyId());
        assertEquals("READY", base.getRawStatus());
        assertEquals("FAILED", base.getVerifyStatus());
        assertEquals("N", base.getAdultYn());

        ReflectionTestUtils.invokeMethod(
                service,
                "insertFailedLog",
                11L,
                "verify-2",
                "CANCELED");

        ArgumentCaptor<IdentityVerifyLogVO> captor = ArgumentCaptor.forClass(
                IdentityVerifyLogVO.class);
        verify(verifyDAO).insertVerifyLog(captor.capture());
        assertEquals(11L, captor.getValue().getMemberNo());
        assertEquals("verify-2", captor.getValue().getVerifyId());
        assertEquals("CANCELED", captor.getValue().getRawStatus());
    }

    @Test
    void generatedVerifyIdShouldUseInjectedClockAndSafeCharacters() {
        String verifyId = invokeString("makeVerifyId", 77L);

        assertTrue(verifyId.startsWith("oditji7720260805000000"));
        assertTrue(verifyId.matches("^[A-Za-z0-9_-]{1,80}$"));
    }

    private boolean invokeBoolean(String methodName, Object... arguments) {
        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
        return Boolean.TRUE.equals(result);
    }

    private String invokeString(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
    }
}
