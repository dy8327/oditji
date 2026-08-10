package com.project.oditji.verify.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.project.oditji.verify.vo.AdultVerifyRequestVO;

import jakarta.servlet.http.HttpSession;

/**
 * 성인인증 컨트롤러의 returnUrl blank/상대경로와 verifyId null 조건을 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class VerifyControllerAdditionalRemainingCoverageTest {

    @Mock
    private VerifyService verifyService;

    @Mock
    private HttpSession session;

    private VerifyController controller;

    @BeforeEach
    void setUp() {
        controller = new VerifyController(verifyService);
        ReflectionTestUtils.setField(controller, "storeId", "store");
        ReflectionTestUtils.setField(controller, "identity1ChannelKey", "easy");
        ReflectionTestUtils.setField(controller, "identity2ChannelKey", "sms");
    }

    @Test
    void verifiedMemberShouldNormalizeBlankHttpsAndNonSlashReturnUrls() {
        when(session.getAttribute("memberNo"))
                .thenReturn(1L);
        when(verifyService.isAdultVerified(1L))
                .thenReturn(true);

        assertEquals(
                "redirect:/",
                controller.adultVerifyPage(
                        "   ",
                        session,
                        new ExtendedModelMap()));

        assertEquals(
                "redirect:/",
                controller.adultVerifyPage(
                        "https://evil.example",
                        session,
                        new ExtendedModelMap()));

        assertEquals(
                "redirect:/",
                controller.adultVerifyPage(
                        "content/1",
                        session,
                        new ExtendedModelMap()));
    }

    @Test
    void completeShouldRejectNullVerifyId() {
        when(session.getAttribute("memberNo"))
                .thenReturn(2L);

        AdultVerifyRequestVO request =
                new AdultVerifyRequestVO();
        request.setVerifyId(null);

        AdultVerifyCompleteVO response =
                controller.completeVerification(
                        request,
                        "/",
                        session);

        assertFalse(response.isSuccess());
        assertEquals(
                "본인인증 요청 ID가 없습니다.",
                response.getMessage());
    }
}
