package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.service.NtsBusinessService;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.NtsBusinessVerifyVO;
import com.project.oditji.favorite.service.FavoriteService;
import com.project.oditji.mail.service.MailService;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.PlatformVO;
import com.project.oditji.order.service.OrderService;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.subscription.service.SubscriptionCalculatorService;
import com.project.oditji.wish.service.WishService;


/** 회원 기본 화면, 중복 확인, 마이페이지와 계정 관리 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class MemberControllerBasicCoverageTest {

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
    @Mock
    private SubscriptionCalculatorService subscriptionCalculatorService;

    /**
     * 실제 컨트롤러 생성자 인자 변경에 테스트가 직접 결합되지 않도록
     * Mockito가 현재 생성자를 선택하여 Mock을 주입하게 합니다.
     */
    @InjectMocks
    private MemberController controller;

    @Test
    void joinFormAndBusinessVerificationShouldDelegate() {
        List<PlatformVO> platforms = List.of(new PlatformVO());
        when(memberPlatformService.findPlatformList()).thenReturn(platforms);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("member/join", controller.joinForm(model));
        assertEquals(platforms, model.get("platformList"));

        when(businessService.isBusinessNumberAvailable("123-45-67890"))
                .thenReturn(true, false);
        assertEquals("Y", controller.checkBusinessNumber("123-45-67890"));
        assertEquals("N", controller.checkBusinessNumber("123-45-67890"));

        NtsBusinessVerifyVO result = new NtsBusinessVerifyVO();
        when(ntsBusinessService.verifyBusiness("123", "대표", "20260101"))
                .thenReturn(result);
        assertEquals(result, controller.verifyBusiness("123", "대표", "20260101"));
    }

    @Test
    void duplicateChecksShouldReturnExpectedFlags() {
        when(memberService.isDuplicateId("id")).thenReturn(true, false);
        when(memberService.isDuplicateEmail("mail@test.com")).thenReturn(true, false);
        when(memberService.isDuplicateNickname("nick")).thenReturn(true, false);

        assertEquals("N", controller.checkId("id"));
        assertEquals("Y", controller.checkId("id"));
        assertEquals("N", controller.checkEmail("mail@test.com"));
        assertEquals("Y", controller.checkEmail("mail@test.com"));
        assertEquals("N", controller.checkNickname("nick"));
        assertEquals("Y", controller.checkNickname("nick"));
    }

    @Test
    void mypageShouldRedirectGuestAdminAndBusinessMember() {
        assertEquals(
                "redirect:/member/login",
                controller.mypage(new MockHttpSession(), new ExtendedModelMap()));

        MemberVO admin = member(1L, "ADMIN");
        assertEquals(
                "redirect:/admin/main",
                controller.mypage(session(admin), new ExtendedModelMap()));

        MemberVO businessMember = member(2L, "BUSINESS");
        when(businessService.getBusinessByMemberNo(2L)).thenReturn(new BusinessVO());
        assertEquals(
                "redirect:/business/main",
                controller.mypage(session(businessMember), new ExtendedModelMap()));
    }

    @Test
    void mypageShouldLoadRegularMemberActivityAndSocialContext() {
        MemberVO member = member(3L, "USER");
        MockHttpSession session = session(member);
        session.setAttribute("loginProvider", "GOOGLE");
        List<PlatformVO> selected = List.of(new PlatformVO());
        List<PlatformVO> all = List.of(new PlatformVO(), new PlatformVO());
        when(memberPlatformService.findMemberPlatformList(3L)).thenReturn(selected);
        when(memberPlatformService.findPlatformList()).thenReturn(all);
        when(favoriteService.getFavoriteCount(3L)).thenReturn(2);
        when(wishService.getWishCount(3L)).thenReturn(1);
        when(orderService.getOrderCount(3L)).thenReturn(4);
        when(reviewService.getMyReviewCount(3L)).thenReturn(5);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.mypage(session, model);

        assertEquals("member/mypage", view);
        assertEquals(Boolean.TRUE, model.get("socialMember"));
        assertEquals("GOOGLE", model.get("loginProvider"));
        assertEquals(selected, model.get("ottList"));
        assertEquals(all, model.get("platformList"));
        assertEquals(3, model.get("favoriteCount"));
        assertEquals(4, model.get("orderCount"));
        assertEquals(5, model.get("reviewCount"));
    }

    @Test
    void updateDuplicateChecksShouldRequireSessionAndTrimValues() {
        MockHttpSession guest = new MockHttpSession();
        assertEquals("N", controller.checkUpdateNickname("nick", guest));
        assertEquals("N", controller.checkUpdateEmail("mail@test.com", guest));

        MockHttpSession loggedIn = session(member(4L, "USER"));
        assertEquals("N", controller.checkUpdateNickname("  ", loggedIn));
        assertEquals("N", controller.checkUpdateEmail("  ", loggedIn));
        when(memberService.checkUpdateNickname(4L, "newNick")).thenReturn(true);
        when(memberService.checkUpdateEmail(4L, "new@test.com")).thenReturn(false);

        assertEquals("Y", controller.checkUpdateNickname(" newNick ", loggedIn));
        assertEquals("N", controller.checkUpdateEmail(" new@test.com ", loggedIn));
        verify(memberService).checkUpdateNickname(4L, "newNick");
        verify(memberService).checkUpdateEmail(4L, "new@test.com");
    }

    @Test
    void updateOttShouldRequireLoginAndDelegateForMember() {
        assertEquals(
                "redirect:/member/login",
                controller.updateOtt(
                        List.of("1"),
                        new MockHttpSession(),
                        new RedirectAttributesModelMap()));

        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        String view = controller.updateOtt(
                List.of("1", "2"),
                session(member(5L, "USER")),
                redirect);

        assertEquals("redirect:/member/mypage", view);
        verify(memberService).updateMemberOtt(5L, List.of("1", "2"));
        assertEquals("OTT 정보가 수정되었습니다.", redirect.getFlashAttributes().get("message"));
    }

    @Test
    void withdrawShouldRequireLoginAndInvalidateSuccessfulSession() {
        assertEquals(
                "redirect:/member/login",
                controller.withdrawMember(new MockHttpSession()));

        MockHttpSession session = session(member(6L, "USER"));
        String view = controller.withdrawMember(session);

        assertEquals("redirect:/", view);
        verify(memberService).withdrawMember(6L);
        assertThrows(
                IllegalStateException.class,
                () -> session.getAttribute("loginMember"));
    }

    @Test
    void findIdShouldExposeSuccessOrFailureResult() {
        assertEquals("member/findId", controller.findId());

        MemberVO found = new MemberVO();
        found.setMemberId("member01");
        when(memberService.findId(any(MemberVO.class)))
                .thenReturn(found)
                .thenAnswer(invocation -> null);
        ExtendedModelMap successModel = new ExtendedModelMap();
        ExtendedModelMap failureModel = new ExtendedModelMap();

        assertEquals("member/findId", controller.findIdPost("홍길동", "a@test.com", successModel));
        assertEquals("member01", successModel.get("findIdResult"));
        assertEquals("member/findId", controller.findIdPost("홍길동", "none@test.com", failureModel));
        assertEquals("일치하는 회원 정보가 없습니다.", failureModel.get("errorMessage"));
    }

    @Test
    void findPasswordFormShouldClearOldStateAndMissingMemberShouldNotSendMail() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("pwResetMemberNo", 1L);
        session.setAttribute("pwResetCode", "123456");
        assertEquals("member/findPw", controller.findPw(session));
        assertNull(session.getAttribute("pwResetMemberNo"));
        assertNull(session.getAttribute("pwResetCode"));

        when(memberService.findPw(any(MemberVO.class))).thenReturn(null);
        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.findPwPost(
                " member ",
                " 홍길동 ",
                " mail@test.com ",
                session,
                model);

        assertEquals("member/findPw", view);
        assertEquals("일치하는 회원 정보가 없습니다.", model.get("errorMessage"));
        verify(mailService, never()).sendPasswordResetCode(any(), any());
    }

    private MemberVO member(Long memberNo, String role) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        member.setRole(role);
        return member;
    }

    private MockHttpSession session(MemberVO member) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginMember", member);
        return session;
    }
}
