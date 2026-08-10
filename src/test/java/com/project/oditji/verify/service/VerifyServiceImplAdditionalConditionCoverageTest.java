package com.project.oditji.verify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.verify.dao.VerifyDAO;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * 본인인증 문자열/JSON helper의 null, blank, 형식 변환, 안전한 returnUrl 조건을 보완합니다.
 */
class VerifyServiceImplAdditionalConditionCoverageTest {

    private VerifyServiceImpl service;
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        service = new VerifyServiceImpl(
                mock(VerifyDAO.class),
                "store",
                "easy",
                "sms",
                "secret");

        jsonMapper = JsonMapper.builder().build();
    }

    @Test
    void birthDateExtractorShouldCoverNullBlankEightDigitAndUnusualFormats() throws Exception {
        assertNull(extractBirthDate(null));

        JsonNode nullNode = jsonMapper.readTree("null");
        assertNull(extractBirthDate(nullNode));

        JsonNode blank =
                jsonMapper.readTree(
                        "{\"birthDate\":\"   \"}");
        assertNull(extractBirthDate(blank));

        JsonNode iso =
                jsonMapper.readTree(
                        "{\"birthDate\":\"2000-01-02\"}");
        assertEquals(
                "2000-01-02",
                extractBirthDate(iso));

        JsonNode digits =
                jsonMapper.readTree(
                        "{\"birthDate\":\"20000102\"}");
        assertEquals(
                "2000-01-02",
                extractBirthDate(digits));

        JsonNode other =
                jsonMapper.readTree(
                        "{\"birthDate\":\"2000/01/02\"}");
        assertEquals(
                "2000/01/02",
                extractBirthDate(other));
    }

    @Test
    void genderNormalizerShouldCoverNullBlankAliasesAndUnknownValues() {
        assertNull(normalizeGender(null));
        assertNull(normalizeGender(" "));

        assertEquals("MALE", normalizeGender(" male "));
        assertEquals("MALE", normalizeGender("M"));
        assertEquals("FEMALE", normalizeGender("female"));
        assertEquals("FEMALE", normalizeGender(" F "));
        assertNull(normalizeGender("UNKNOWN"));
    }

    @Test
    void stringExtractorShouldCoverNullMissingNullBlankAndTextValues() throws Exception {
        assertNull(getString(null, "name"));

        JsonNode root =
                jsonMapper.readTree(
                        "{\"nil\":null,"
                                + "\"blank\":\" \","
                                + "\"name\":\" 홍길동 \"}");

        assertNull(getString(root, "missing"));
        assertNull(getString(root, "nil"));
        assertNull(getString(root, "blank"));
        assertEquals(
                " 홍길동 ",
                getString(root, "name"));
    }

    @Test
    void returnUrlSanitizerShouldRejectExternalProtocolLikeAndControlCharacterValues() {
        assertEquals("/", sanitize(null));
        assertEquals("/", sanitize(" "));
        assertEquals("/", sanitize("https://evil.example"));
        assertEquals("/", sanitize("//evil.example"));
        assertEquals("/", sanitize("/\\evil"));
        assertEquals("/", sanitize("/safe\rInjected"));
        assertEquals("/", sanitize("/safe\nInjected"));
        assertEquals("/content/1", sanitize(" /content/1 "));
    }

    @Test
    void defaultStringAndAdultVerifiedShouldCoverBothBooleanSides() {
        assertEquals("default", defaultString(null, "default"));
        assertEquals("default", defaultString("   ", "default"));
        assertEquals("value", defaultString("value", "default"));

        when(
                ((VerifyDAO) ReflectionTestUtils.getField(
                        service,
                        "verifyDAO"))
                        .selectMemberAdultVerified(1L))
                .thenReturn("Y", "N");

        assertTrue(service.isAdultVerified(1L));
        assertFalse(service.isAdultVerified(1L));
    }

    private String extractBirthDate(JsonNode customer) {
        return ReflectionTestUtils.invokeMethod(
                service,
                "extractBirthDate",
                customer);
    }

    private String normalizeGender(String value) {
        return ReflectionTestUtils.invokeMethod(
                service,
                "normalizeGender",
                value);
    }

    private String getString(JsonNode node, String field) {
        return ReflectionTestUtils.invokeMethod(
                service,
                "getString",
                node,
                field);
    }

    private String sanitize(String value) {
        return ReflectionTestUtils.invokeMethod(
                service,
                "sanitizeReturnUrl",
                value);
    }

    private String defaultString(String value, String defaultValue) {
        return ReflectionTestUtils.invokeMethod(
                service,
                "defaultString",
                value,
                defaultValue);
    }
}
