package com.project.oditji.verify.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.verify.service.VerifyService;
import com.project.oditji.verify.vo.AdultVerifyCompleteVO;
import com.project.oditji.verify.vo.AdultVerifyReadyVO;
import com.project.oditji.verify.vo.AdultVerifyRequestVO;

import jakarta.servlet.http.HttpSession;

/** 성인인증 화면, 준비·완료 처리와 복귀 URL 검증 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class VerifyControllerCoverageTest {

    @Mock
    private VerifyService verifyService;

    @Mock
    private HttpSession session;

    private VerifyController controller;

    @BeforeEach
    void setUp() {
        controller = new VerifyController(verifyService);
        ReflectionTestUtils.setField(controller, "storeId", "store-test");
        ReflectionTestUtils.setField(controller, "identity1ChannelKey", "easy-test");
        ReflectionTestUtils.setField(controller, "identity2ChannelKey", "sms-test");
    }

    @Test
    void adultPageShouldRedirectAnonymousAndStoreSafeReturnPath() {
        assertEquals(
                "redirect:/member/login",
                controller.adultVerifyPage(
                        "/content/contentDetail/10",
                        session,
                        new ExtendedModelMap()));
        verify(session).setAttribute(
                "redirectAfterLogin",
                "/verify/adult?returnUrl=/content/contentDetail/10");
    }

    @Test
    void adultPageShouldNormalizeZeroMemberAndUnsafeReturnUrl() {
        when(session.getAttribute("memberNo")).thenReturn(0L);

        assertEquals(
                "redirect:/member/login",
                controller.adultVerifyPage(
                        "https://evil.example",
                        session,
                        new ExtendedModelMap()));
        verify(session).setAttribute(
                "redirectAfterLogin",
                "/verify/adult?returnUrl=/");
    }

    @Test
    void verifiedMemberShouldSetSessionAndRedirectSafely() {
        when(session.getAttribute("memberNo")).thenReturn(1L);
        when(verifyService.isAdultVerified(1L)).thenReturn(true);

        assertEquals(
                "redirect:/goods/goodsDetail/5",
                controller.adultVerifyPage(
                        "/goods/goodsDetail/5",
                        session,
                        new ExtendedModelMap()));
        verify(session).setAttribute("ADULT_VERIFIED", "Y");
    }

    @Test
    void verifiedMemberShouldRejectExternalRedirect() {
        when(session.getAttribute("memberNo")).thenReturn(2L);
        when(verifyService.isAdultVerified(2L)).thenReturn(true);

        assertEquals(
                "redirect:/",
                controller.adultVerifyPage(
                        "http://evil.example",
                        session,
                        new ExtendedModelMap()));
    }

    @Test
    void unverifiedMemberShouldExposePortoneConfiguration() {
        when(session.getAttribute("memberNo")).thenReturn(3L);
        when(verifyService.isAdultVerified(3L)).thenReturn(false);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals(
                "verify/adultVerify",
                controller.adultVerifyPage("/content/3", session, model));
        assertEquals("store-test", model.get("storeId"));
        assertEquals("easy-test", model.get("identity1ChannelKey"));
        assertEquals("sms-test", model.get("identity2ChannelKey"));
        assertEquals("/content/3", model.get("returnUrl"));
    }

    @Test
    void prepareShouldRequireLoginAndDelegate() {
        assertThrows(
                IllegalStateException.class,
                () -> controller.prepareVerification(session));

        when(session.getAttribute("memberNo")).thenReturn(4L);
        AdultVerifyReadyVO ready =
                new AdultVerifyReadyVO("s", "e", "m", "verify-1");
        when(verifyService.prepareVerification(4L)).thenReturn(ready);

        assertSame(ready, controller.prepareVerification(session));
    }

    @Test
    void completeShouldRejectAnonymousAndBlankIds() {
        AdultVerifyRequestVO request = request("verify-1");

        AdultVerifyCompleteVO anonymous =
                controller.completeVerification(request, "/", session);
        assertFalse(anonymous.isSuccess());
        assertEquals("로그인이 필요합니다.", anonymous.getMessage());

        when(session.getAttribute("memberNo")).thenReturn(5L);

        AdultVerifyCompleteVO missing =
                controller.completeVerification(request(" "), "/", session);
        assertFalse(missing.isSuccess());
        assertEquals("본인인증 요청 ID가 없습니다.", missing.getMessage());
    }

    @Test
    void completeShouldDelegateSuccessfulVerification() {
        when(session.getAttribute("memberNo")).thenReturn(6L);
        AdultVerifyCompleteVO complete =
                AdultVerifyCompleteVO.success("/content/6");
        when(verifyService.completeVerification(
                6L,
                "verify-6",
                "/content/6",
                session))
                .thenReturn(complete);

        assertSame(
                complete,
                controller.completeVerification(
                        request("verify-6"),
                        "/content/6",
                        session));
    }

    @Test
    void completeShouldConvertUnexpectedFailureToSafeResponse() {
        when(session.getAttribute("memberNo")).thenReturn(7L);
        when(verifyService.completeVerification(
                7L,
                "verify-7",
                "/",
                session))
                .thenThrow(new IllegalStateException("portone"));

        AdultVerifyCompleteVO response =
                controller.completeVerification(
                        request("verify-7"),
                        "/",
                        session);

        assertFalse(response.isSuccess());
        assertEquals(
                "성인인증 완료 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                response.getMessage());
    }

    private AdultVerifyRequestVO request(String verifyId) {
        AdultVerifyRequestVO request = new AdultVerifyRequestVO();
        request.setVerifyId(verifyId);
        return request;
    }
}
