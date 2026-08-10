package com.project.oditji.verify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.project.oditji.verify.dao.VerifyDAO;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;

/**
 * 본인인증 서비스의 명시적 Json null과 PortOne null 응답 분기를 보완합니다.
 */
class VerifyServiceImplRemainingCoverageTest {

    private VerifyServiceImpl service;
    private JsonMapper jsonMapper;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        service =
                new VerifyServiceImpl(
                        mock(VerifyDAO.class),
                        "store",
                        "easy",
                        "sms",
                        "secret");

        jsonMapper =
                JsonMapper.builder()
                        .build();

        ReflectionTestUtils.setField(
                service,
                "clock",
                Clock.fixed(
                        Instant.parse(
                                "2026-08-10T00:00:00Z"),
                        ZoneOffset.UTC));

        RestClient.Builder builder =
                RestClient.builder()
                        .baseUrl(
                                "https://api.portone.io")
                        .defaultHeader(
                                HttpHeaders.AUTHORIZATION,
                                "PortOne secret");

        server =
                MockRestServiceServer.bindTo(
                        builder)
                        .build();

        ReflectionTestUtils.setField(
                service,
                "restClient",
                builder.build());
    }

    @Test
    void explicitNullIdentityAndCustomerNodesShouldUseFallbackBranches() throws Exception {
        JsonNode identityNullRoot =
                jsonMapper.readTree(
                        "{\"identityVerification\":null,"
                                + "\"status\":\"READY\"}");

        JsonNode identity =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "extractIdentityVerification",
                        identityNullRoot);

        assertEquals(
                "READY",
                identity.path(
                        "status")
                        .asString());

        JsonNode customerNullRoot =
                jsonMapper.readTree(
                        "{\"verifiedCustomer\":null,"
                                + "\"customer\":{\"name\":\"레거시\"}}");

        JsonNode customer =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "extractVerifiedCustomer",
                        customerNullRoot);

        assertEquals(
                "레거시",
                customer.path(
                        "name")
                        .asString());

        JsonNode jsonNull =
                jsonMapper.readTree(
                        "null");

        String value =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "getString",
                        jsonNull,
                        "name");

        assertNull(value);
    }

    @Test
    void verifyIdValidationShouldCoverLengthBoundary() {
        String maxLength =
                "a".repeat(80);
        String overLength =
                "a".repeat(81);

        String valid =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "validateVerifyId",
                        maxLength);
        String invalid =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "validateVerifyId",
                        overLength);

        assertNull(valid);
        assertEquals(
                "본인인증 요청 ID 형식이 올바르지 않습니다.",
                invalid);
    }

    @Test
    void identityVerificationLookupShouldRejectNullHttpBody() {
        server.expect(
                requestTo(
                        "https://api.portone.io/identity-verifications/null-body"))
                .andExpect(
                        method(
                                HttpMethod.GET))
                .andExpect(
                        header(
                                HttpHeaders.AUTHORIZATION,
                                "PortOne secret"))
                .andRespond(
                        withNoContent());

        assertThrows(
                IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "getIdentityVerification",
                        "null-body"));

        server.verify();
    }

    @Test
    void adultCheckShouldCoverExactlyNineteenAndOneDayTooYoungWithFixedClock() {
        Boolean adult =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "isAdult",
                        "2007-08-10");

        Boolean tooYoung =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "isAdult",
                        "2007-08-11");

        assertEquals(
                Boolean.TRUE,
                adult);
        assertEquals(
                Boolean.FALSE,
                tooYoung);
    }
}
