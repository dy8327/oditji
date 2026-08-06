package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.service.NtsBusinessService;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.favorite.service.FavoriteService;
import com.project.oditji.mail.service.MailService;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.order.service.OrderService;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.wish.service.WishService;

/** 로그인 복귀 주소, 사업자 상태, 계정 복구와 비밀번호 재설정 분기를 보완합니다. */
@ExtendWith(MockitoExtension.class)
class MemberControllerAdditionalCoverageTest {

    @Mock
    private MemberService memberService;
    @Mock
    private MemberPlatformService memberPlatformService;
    @Mock
    private BusinessService businessService;
    @Mock
    private NtsBusinessService ntsBusinessService;
    @Mock
    private MailService mailService;
    @Mock
    private FavoriteService favoriteService;
    @Mock
    private WishService wishService;
    @Mock
    private OrderService orderService;
    @Mock
    private ReviewService reviewService;

    private MemberController controller;

    @BeforeEach
    void setUp() {
        controller = new MemberController(
                memberService,
                memberPlatformService,
                businessService,
                ntsBusinessService,
                mailService,
                favoriteService,
                wishService,
                orderService,
                reviewService,
                "build/test-profile",
                "build/test-license");
    }

    @Test
    void loginFormShouldStoreOnlyUsableInternalRedirects() {
        MockHttpServletRequest request = request("/oditji");
        MockHttpSession session = new MockHttpSession();

        assertEquals(
                "member/login",
                controller.loginForm(
                        "http://localhost/oditji/recommend?page=2",
                        request,
                        session));
        assertEquals(
                "/recommend?page=2",
                session.getAttribute("redirectAfterLogin"));

        MockHttpSession blockedSession = new MockHttpSession();
        assertEquals(
                "member/login",
                controller.loginForm(
                        "https://outside.example/member/mypage",
                        request,
                        blockedSession));
        assertNull(blockedSession.getAttribute("redirectAfterLogin"));

        MockHttpSession memberPageSession = new MockHttpSession();
        assertEquals(
                "member/login",
                controller.loginForm(
                        "/member/join",
                        request,
                        memberPageSession));
        assertNull(memberPageSession.getAttribute("redirectAfterLogin"));
    }

    @Test
    void loginShouldHandleFailureWaitingRejectedAndSuccessfulMember() {
        MemberVO input = new MemberVO();
        input.setMemberId("member01");

        when(memberService.loginMember(input)).thenReturn(null);
        RedirectAttributesModelMap failedRedirect = new RedirectAttributesModelMap();
        assertEquals(
                "redirect:/member/login",
                controller.login(
                        input,
                        request("/oditji"),
                        new MockHttpSession(),
                        failedRedirect));
        assertEquals(
                "아이디 또는 비밀번호가 틀렸습니다.",
                failedRedirect.getFlashAttributes().get("message"));

        MemberVO waitingMember = member(2L, "BUSINESS", "사업자", "사업닉");
        BusinessVO waitingBusiness = business(20L, "WAITING", "대기상점");
        when(memberService.loginMember(waitingMember)).thenReturn(waitingMember);
        when(businessService.getBusinessByMemberNo(2L)).thenReturn(waitingBusiness);
        RedirectAttributesModelMap waitingRedirect = new RedirectAttributesModelMap();
        assertEquals(
                "redirect:/member/login",
                controller.login(
                        waitingMember,
                        request("/oditji"),
                        new MockHttpSession(),
                        waitingRedirect));
        assertEquals(
                "관리자 승인 대기 중인 사업자 계정입니다.",
                waitingRedirect.getFlashAttributes().get("message"));

        MemberVO rejectedMember = member(3L, "BUSINESS", "사업자", "사업닉");
        BusinessVO rejectedBusiness = business(30L, "REJECTED", "거절상점");
        rejectedBusiness.setRejectReason("서류 확인 필요");
        when(memberService.loginMember(rejectedMember)).thenReturn(rejectedMember);
        when(businessService.getBusinessByMemberNo(3L)).thenReturn(rejectedBusiness);
        RedirectAttributesModelMap rejectedRedirect = new RedirectAttributesModelMap();
        assertEquals(
                "redirect:/member/login",
                controller.login(
                        rejectedMember,
                        request("/oditji"),
                        new MockHttpSession(),
                        rejectedRedirect));
        assertTrue(rejectedRedirect.getFlashAttributes()
                .get("message")
                .toString()
                .contains("서류 확인 필요"));

        MemberVO user = member(4L, "USER", "홍길동", "길동닉");
        user.setMemberId("member04");
        when(memberService.loginMember(user)).thenReturn(user);
        when(businessService.getBusinessByMemberNo(4L)).thenReturn(null);
        MockHttpSession successSession = new MockHttpSession();
        successSession.setAttribute("redirectAfterLogin", "/content/list");
        MockHttpServletRequest successRequest = request("/oditji");
        successRequest.setSession(successSession);

        assertEquals(
                "redirect:/content/list",
                controller.login(
                        user,
                        successRequest,
                        successSession,
                        new RedirectAttributesModelMap()));
        assertEquals(user, successSession.getAttribute("loginMember"));
        assertEquals(4L, successSession.getAttribute("memberNo"));
        assertEquals("홍길동", successSession.getAttribute("loginDisplayName"));
        assertNull(successSession.getAttribute("redirectAfterLogin"));
    }

    @Test
    void restoreMemberShouldClearRestoreStateOnSuccessAndFailure() {
        RedirectAttributesModelMap missingRedirect = new RedirectAttributesModelMap();
        assertEquals(
                "redirect:/member/login",
                controller.restoreMember(new MockHttpSession(), missingRedirect));
        assertEquals(
                "복구 요청 정보가 없습니다. 다시 로그인해주세요.",
                missingRedirect.getFlashAttributes().get("errorMessage"));

        MockHttpSession successSession = restoreSession(10L);
        RedirectAttributesModelMap successRedirect = new RedirectAttributesModelMap();
        assertEquals(
                "redirect:/member/login",
                controller.restoreMember(successSession, successRedirect));
        verify(memberService).restoreMember(10L);
        assertNull(successSession.getAttribute("restoreMemberNo"));
        assertEquals(
                "계정이 복구되었습니다. 다시 로그인해주세요.",
                successRedirect.getFlashAttributes().get("restoredMessage"));

        MockHttpSession failureSession = restoreSession(11L);
        doThrow(new IllegalStateException("복구할 수 없는 계정입니다."))
                .when(memberService)
                .restoreMember(11L);
        RedirectAttributesModelMap failureRedirect = new RedirectAttributesModelMap();
        assertEquals(
                "redirect:/member/login",
                controller.restoreMember(failureSession, failureRedirect));
        assertNull(failureSession.getAttribute("restoreMemberNo"));
        assertEquals(
                "복구할 수 없는 계정입니다.",
                failureRedirect.getFlashAttributes().get("errorMessage"));
    }

    @Test
    void passwordCodeShouldCoverMissingExpiredWrongAndSuccessBranches() {
        ExtendedModelMap missingModel = new ExtendedModelMap();
        assertEquals(
                "member/findPw",
                controller.verifyPwCode(
                        "123456",
                        new MockHttpSession(),
                        missingModel));
        assertEquals(
                "인증 요청 정보가 없습니다. 다시 진행해 주세요.",
                missingModel.get("errorMessage"));

        MockHttpSession expiredSession = passwordSession(
                20L,
                "123456",
                System.currentTimeMillis() - 1,
                0,
                false);
        ExtendedModelMap expiredModel = new ExtendedModelMap();
        assertEquals(
                "member/findPw",
                controller.verifyPwCode("123456", expiredSession, expiredModel));
        assertEquals(
                "인증번호가 만료되었습니다. 다시 요청해 주세요.",
                expiredModel.get("errorMessage"));

        MockHttpSession wrongSession = passwordSession(
                21L,
                "123456",
                System.currentTimeMillis() + 60_000,
                0,
                false);
        ExtendedModelMap wrongModel = new ExtendedModelMap();
        assertEquals(
                "member/findPw",
                controller.verifyPwCode("000000", wrongSession, wrongModel));
        assertEquals(1, wrongSession.getAttribute("pwResetAttempts"));
        assertTrue(wrongModel.get("errorMessage").toString().contains("남은 횟수: 4회"));

        MockHttpSession successSession = passwordSession(
                22L,
                "654321",
                System.currentTimeMillis() + 60_000,
                1,
                false);
        assertEquals(
                "redirect:/member/changePw",
                controller.verifyPwCode(
                        " 654321 ",
                        successSession,
                        new ExtendedModelMap()));
        assertEquals(Boolean.TRUE, successSession.getAttribute("pwResetVerified"));
        assertNull(successSession.getAttribute("pwResetCode"));
        assertNull(successSession.getAttribute("pwResetAttempts"));
    }

    @Test
    void changePasswordShouldRequireVerificationAndHandleMismatchFailureAndSuccess() {
        assertEquals(
                "redirect:/member/findPw",
                controller.changePw(new MockHttpSession()));

        MockHttpSession verifiedSession = passwordSession(
                30L,
                null,
                System.currentTimeMillis() + 60_000,
                null,
                true);
        assertEquals("member/changePw", controller.changePw(verifiedSession));

        ExtendedModelMap mismatchModel = new ExtendedModelMap();
        assertEquals(
                "member/changePw",
                controller.changePwPost(
                        "Password1!",
                        "Password2!",
                        verifiedSession,
                        new RedirectAttributesModelMap(),
                        mismatchModel));
        assertEquals("비밀번호가 일치하지 않습니다.", mismatchModel.get("errorMessage"));

        doThrow(new IllegalArgumentException("비밀번호 형식 오류"))
                .when(memberService)
                .updatePassword(30L, "short");
        ExtendedModelMap failureModel = new ExtendedModelMap();
        assertEquals(
                "member/changePw",
                controller.changePwPost(
                        "short",
                        "short",
                        verifiedSession,
                        new RedirectAttributesModelMap(),
                        failureModel));
        assertEquals("비밀번호 형식 오류", failureModel.get("errorMessage"));

        MockHttpSession successSession = passwordSession(
                31L,
                null,
                System.currentTimeMillis() + 60_000,
                null,
                true);
        RedirectAttributesModelMap successRedirect = new RedirectAttributesModelMap();
        assertEquals(
                "redirect:/member/login",
                controller.changePwPost(
                        "Password1!",
                        "Password1!",
                        successSession,
                        successRedirect,
                        new ExtendedModelMap()));
        verify(memberService).updatePassword(31L, "Password1!");
        assertNull(successSession.getAttribute("pwResetMemberNo"));
        assertEquals(
                "비밀번호가 변경되었습니다.",
                successRedirect.getFlashAttributes().get("message"));
    }

    private MockHttpServletRequest request(String contextPath) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(80);
        request.setContextPath(contextPath);
        return request;
    }

    private MemberVO member(
            Long memberNo,
            String role,
            String memberName,
            String nickname) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        member.setRole(role);
        member.setMemberName(memberName);
        member.setNickname(nickname);
        return member;
    }

    private BusinessVO business(
            Long businessNo,
            String status,
            String businessName) {
        BusinessVO business = new BusinessVO();
        business.setBusinessNo(businessNo);
        business.setStatus(status);
        business.setBusinessName(businessName);
        return business;
    }

    private MockHttpSession restoreSession(long memberNo) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("restoreMemberNo", memberNo);
        session.setAttribute("restoreProvider", "LOCAL");
        return session;
    }

    private MockHttpSession passwordSession(
            Long memberNo,
            String code,
            long expiresAt,
            Integer attempts,
            boolean verified) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("pwResetMemberNo", memberNo);
        session.setAttribute("pwResetEmail", "member@example.com");
        if (code != null) {
            session.setAttribute("pwResetCode", code);
        }
        session.setAttribute("pwResetExpiresAt", expiresAt);
        if (attempts != null) {
            session.setAttribute("pwResetAttempts", attempts);
        }
        session.setAttribute("pwResetVerified", verified);
        return session;
    }
}
