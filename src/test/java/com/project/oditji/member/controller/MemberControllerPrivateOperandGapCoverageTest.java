package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

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

/** 회원 컨트롤러 private helper의 null/blank 복합 조건을 보완합니다. */
class MemberControllerPrivateOperandGapCoverageTest {

    private MemberService memberService;
    private MemberController controller;

    @BeforeEach
    void setUp() {
        memberService = mock(MemberService.class);
        controller = new MemberController(
                memberService,
                mock(MemberPlatformService.class),
                mock(BusinessService.class),
                mock(NtsBusinessService.class),
                mock(MailService.class),
                mock(FavoriteService.class),
                mock(WishService.class),
                mock(OrderService.class),
                mock(ReviewService.class),
                "build/test-profile",
                "build/test-license");
    }

    @Test
    void loginDisplayNameShouldCoverBusinessAndMemberNullNameOperands() {
        MemberVO member = new MemberVO();
        member.setMemberName(null);
        member.setNickname("닉네임");

        assertEquals(
                "닉네임",
                invokeString("getLoginDisplayName", member, null));

        BusinessVO business = new BusinessVO();
        business.setBusinessName(null);

        assertEquals(
                "회원",
                invokeString("getLoginDisplayName", member, business));

        member.setMemberName("   ");
        member.setNickname("   ");

        assertEquals(
                "회원",
                invokeString("getLoginDisplayName", member, null));
    }

    @Test
    void loginSuccessRedirectShouldTreatBlankAsHomeAndKeepUsableValue() {
        MockHttpSession blankSession = new MockHttpSession();
        blankSession.setAttribute("redirectAfterLogin", "   ");

        assertEquals(
                "redirect:/",
                invokeString("getLoginSuccessRedirect", blankSession));
        assertNull(blankSession.getAttribute("redirectAfterLogin"));

        MockHttpSession valueSession = new MockHttpSession();
        valueSession.setAttribute("redirectAfterLogin", "/recommend");

        assertEquals(
                "redirect:/recommend",
                invokeString("getLoginSuccessRedirect", valueSession));
    }

    @Test
    void passwordChangeShouldCoverNullBlankAndBlankCurrentPasswordOperands() {
        MemberVO member = new MemberVO();

        ReflectionTestUtils.invokeMethod(
                controller,
                "applyPasswordChange",
                member,
                1L,
                "current",
                null,
                null);
        assertNull(member.getMemberPw());

        ReflectionTestUtils.invokeMethod(
                controller,
                "applyPasswordChange",
                member,
                1L,
                "current",
                "   ",
                "   ");
        assertNull(member.getMemberPw());

        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        controller,
                        "applyPasswordChange",
                        member,
                        1L,
                        "   ",
                        "NewPassword1!",
                        "NewPassword1!"));
    }

    @Test
    void memberSessionUpdateShouldCoverNullNameWithNicknameAndBlankNicknameFallback() {
        MemberVO nicknameMember = new MemberVO();
        nicknameMember.setMemberName(null);
        nicknameMember.setNickname("세션닉네임");
        MockHttpSession nicknameSession = new MockHttpSession();

        ReflectionTestUtils.invokeMethod(
                controller,
                "updateMemberSession",
                nicknameSession,
                nicknameMember);

        assertEquals(
                "세션닉네임",
                nicknameSession.getAttribute("loginDisplayName"));

        MemberVO fallbackMember = new MemberVO();
        fallbackMember.setMemberName(null);
        fallbackMember.setNickname("   ");
        MockHttpSession fallbackSession = new MockHttpSession();

        ReflectionTestUtils.invokeMethod(
                controller,
                "updateMemberSession",
                fallbackSession,
                fallbackMember);

        assertEquals(
                "회원",
                fallbackSession.getAttribute("loginDisplayName"));
    }

    private String invokeString(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(
                controller,
                methodName,
                arguments);
    }
}
