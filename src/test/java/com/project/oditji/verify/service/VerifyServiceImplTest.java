package com.project.oditji.verify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.verify.dao.VerifyDAO;
import com.project.oditji.verify.vo.AdultVerifyCompleteVO;
import com.project.oditji.verify.vo.AdultVerifyReadyVO;

import jakarta.servlet.http.HttpSession;

/** 성인인증 준비 정보, 요청 ID 검증, 중복 방지와 인증 상태 조회를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class VerifyServiceImplTest {

    @Mock
    private VerifyDAO verifyDAO;

    @Mock
    private HttpSession session;

    private VerifyServiceImpl verifyService;

    @BeforeEach
    void setUp() {
        verifyService = new VerifyServiceImpl(
                verifyDAO,
                "store-id",
                "easy-key",
                "sms-key",
                "api-secret");
    }

    @Test
    void prepareVerificationShouldReturnConfiguredChannelsAndUniqueId() {
        AdultVerifyReadyVO first = verifyService.prepareVerification(10L);
        AdultVerifyReadyVO second = verifyService.prepareVerification(10L);

        assertEquals("store-id", first.getStoreId());
        assertEquals("easy-key", first.getEasyChannelKey());
        assertEquals("sms-key", first.getSmsChannelKey());
        assertTrue(first.getVerifyId().startsWith("oditji10"));
        assertTrue(first.getVerifyId().matches("^[A-Za-z0-9_-]{1,80}$"));
        org.junit.jupiter.api.Assertions.assertNotEquals(
                first.getVerifyId(),
                second.getVerifyId());
    }

    @Test
    void completeVerificationShouldRejectMissingAndMalformedIdsBeforeApiCall() {
        AdultVerifyCompleteVO missing = verifyService.completeVerification(
                10L,
                null,
                "/",
                session);
        AdultVerifyCompleteVO blank = verifyService.completeVerification(
                10L,
                " ",
                "/",
                session);
        AdultVerifyCompleteVO malformed = verifyService.completeVerification(
                10L,
                "invalid id!",
                "/",
                session);

        assertFalse(missing.isSuccess());
        assertFalse(blank.isSuccess());
        assertFalse(malformed.isSuccess());
        assertTrue(missing.getMessage().contains("요청 ID가 없습니다"));
        assertTrue(malformed.getMessage().contains("형식이 올바르지"));
        verify(verifyDAO, never())
                .countVerifyId(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void completeVerificationShouldRejectAlreadyProcessedRequest() {
        when(verifyDAO.countVerifyId("valid_id-1")).thenReturn(1);

        AdultVerifyCompleteVO result = verifyService.completeVerification(
                10L,
                "valid_id-1",
                "/content/1",
                session);

        assertFalse(result.isSuccess());
        assertEquals("이미 처리된 본인인증 요청입니다.",
                result.getMessage());
    }

    @Test
    void adultVerifiedShouldMatchOnlyExactYValue() {
        when(verifyDAO.selectMemberAdultVerified(10L))
                .thenReturn("Y")
                .thenReturn("N")
                .thenReturn(null)
                .thenReturn("y");

        assertTrue(verifyService.isAdultVerified(10L));
        assertFalse(verifyService.isAdultVerified(10L));
        assertFalse(verifyService.isAdultVerified(10L));
        assertFalse(verifyService.isAdultVerified(10L));
    }
}
