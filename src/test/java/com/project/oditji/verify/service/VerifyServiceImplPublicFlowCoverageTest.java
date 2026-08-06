package com.project.oditji.verify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.project.oditji.verify.dao.VerifyDAO;
import com.project.oditji.verify.vo.AdultVerifyCompleteVO;
import com.project.oditji.verify.vo.AdultVerifyReadyVO;
import com.project.oditji.verify.vo.IdentityVerifyLogVO;

/** 본인인증 준비, 포트원 성공·실패 응답과 회원 성인 플래그 갱신 흐름을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class VerifyServiceImplPublicFlowCoverageTest {

    @Mock
    private VerifyDAO verifyDAO;

    private VerifyServiceImpl service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        service = new VerifyServiceImpl(
                verifyDAO,
                "store-id",
                "easy-key",
                "sms-key",
                "api-secret");

        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.portone.io")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "PortOne api-secret");
        server = MockRestServiceServer.bindTo(builder).build();
        ReflectionTestUtils.setField(service, "restClient", builder.build());
        ReflectionTestUtils.setField(
                service,
                "clock",
                Clock.fixed(
                        Instant.parse("2026-08-06T00:00:00Z"),
                        ZoneOffset.UTC));
    }

    @Test
    void prepareAndAdultStatusShouldExposeConfiguredValues() {
        AdultVerifyReadyVO ready = service.prepareVerification(77L);

        assertEquals("store-id", ready.getStoreId());
        assertEquals("easy-key", ready.getEasyChannelKey());
        assertEquals("sms-key", ready.getSmsChannelKey());
        assertTrue(ready.getVerifyId().startsWith("oditji7720260806000000"));

        when(verifyDAO.selectMemberAdultVerified(77L)).thenReturn("Y", "N", null);
        assertTrue(service.isAdultVerified(77L));
        assertFalse(service.isAdultVerified(77L));
        assertFalse(service.isAdultVerified(77L));
    }

    @Test
    void completeVerificationShouldRejectInvalidAndDuplicateIdsWithoutHttpCall() {
        AdultVerifyCompleteVO missing = service.completeVerification(
                1L,
                " ",
                "/content/1",
                new MockHttpSession());
        assertFalse(missing.isSuccess());
        assertEquals("본인인증 요청 ID가 없습니다.", missing.getMessage());

        AdultVerifyCompleteVO invalid = service.completeVerification(
                1L,
                "invalid id!",
                "/content/1",
                new MockHttpSession());
        assertFalse(invalid.isSuccess());
        assertEquals("본인인증 요청 ID 형식이 올바르지 않습니다.", invalid.getMessage());

        when(verifyDAO.countVerifyId("verify-duplicate")).thenReturn(1);
        AdultVerifyCompleteVO duplicate = service.completeVerification(
                1L,
                "verify-duplicate",
                "/content/1",
                new MockHttpSession());
        assertFalse(duplicate.isSuccess());
        assertEquals("이미 처리된 본인인증 요청입니다.", duplicate.getMessage());
        verify(verifyDAO, never()).updateMemberAdultVerified(1L);
    }

    @Test
    void completeVerificationShouldPersistAdultResultAndUpdateSession() {
        when(verifyDAO.countVerifyId("verify-100")).thenReturn(0);
        expectVerification(
                "verify-100",
                """
                {
                  "identityVerification": {
                    "status": "VERIFIED",
                    "verifiedCustomer": {
                      "name": "홍길동",
                      "birthDate": "20000101",
                      "phoneNumber": "01012345678",
                      "gender": "MALE"
                    }
                  }
                }
                """);
        MockHttpSession session = new MockHttpSession();

        AdultVerifyCompleteVO result = service.completeVerification(
                10L,
                "verify-100",
                "/content/100?tab=review",
                session);

        assertTrue(result.isSuccess());
        assertTrue(result.isAdult());
        assertEquals("성인인증이 완료되었습니다.", result.getMessage());
        assertEquals("/content/100?tab=review", result.getRedirectUrl());
        assertEquals("Y", session.getAttribute("ADULT_VERIFIED"));
        verify(verifyDAO).updateMemberAdultVerified(10L);

        ArgumentCaptor<IdentityVerifyLogVO> captor =
                ArgumentCaptor.forClass(IdentityVerifyLogVO.class);
        verify(verifyDAO).insertVerifyLog(captor.capture());
        IdentityVerifyLogVO log = captor.getValue();
        assertEquals(10L, log.getMemberNo());
        assertEquals("verify-100", log.getVerifyId());
        assertEquals("VERIFIED", log.getVerifyStatus());
        assertEquals("홍길동", log.getName());
        assertEquals("2000-01-01", log.getBirthDate());
        assertEquals("01012345678", log.getPhoneNumber());
        assertEquals("MALE", log.getGender());
        assertEquals("Y", log.getAdultYn());
        server.verify();
    }

    @Test
    void completeVerificationShouldPersistUnderageAndIncompleteResults() {
        when(verifyDAO.countVerifyId("verify-underage")).thenReturn(0);
        when(verifyDAO.countVerifyId("verify-ready")).thenReturn(0);
        expectVerification(
                "verify-underage",
                """
                {
                  "status": "VERIFIED",
                  "customer": {
                    "name": "미성년자",
                    "birthDate": "2010-01-01",
                    "gender": "F"
                  }
                }
                """);
        expectVerification(
                "verify-ready",
                "{\"identityVerification\":{\"status\":\"READY\"}}");
        MockHttpSession underageSession = new MockHttpSession();

        AdultVerifyCompleteVO underage = service.completeVerification(
                11L,
                "verify-underage",
                "https://outside.example",
                underageSession);
        assertFalse(underage.isSuccess());
        assertEquals("성인만 이용할 수 있는 콘텐츠입니다.", underage.getMessage());
        assertNull(underageSession.getAttribute("ADULT_VERIFIED"));
        verify(verifyDAO, never()).updateMemberAdultVerified(11L);

        AdultVerifyCompleteVO ready = service.completeVerification(
                12L,
                "verify-ready",
                "/",
                new MockHttpSession());
        assertFalse(ready.isSuccess());
        assertEquals(
                "본인인증이 완료되지 않았습니다. 현재 상태: READY",
                ready.getMessage());
        server.verify();
    }

    private void expectVerification(String verifyId, String responseBody) {
        server.expect(requestTo(
                        "https://api.portone.io/identity-verifications/" + verifyId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "PortOne api-secret"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));
    }
}
