package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
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
import com.project.oditji.order.service.OrderService;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.subscription.service.SubscriptionCalculatorService;
import com.project.oditji.wish.service.WishService;

import jakarta.servlet.http.HttpSession;

/**
 * SonarQube에 남은 MemberController의 실제 진입 가능한 조건 분기를 보완합니다.
 * 방어 로직을 제거하지 않고 공개 메서드 호출과 제한적인 private helper 호출로만 검증합니다.
 */
class MemberControllerResidualClosure5Test {

    @TempDir
    Path tempDirectory;

    private MemberService memberService;
    private NtsBusinessService ntsBusinessService;
    private MemberController controller;

    @BeforeEach
    void setUp() {
        memberService = mock(MemberService.class);
        ntsBusinessService = mock(NtsBusinessService.class);

        controller = new MemberController(
                memberService,
                mock(MemberPlatformService.class),
                mock(BusinessService.class),
                ntsBusinessService,
                mock(MailService.class),
                mock(FavoriteService.class),
                mock(WishService.class),
                mock(OrderService.class),
                mock(ReviewService.class),
                mock(SubscriptionCalculatorService.class),
                tempDirectory.resolve("profiles").toString(),
                tempDirectory.resolve("licenses").toString());
    }

    @Test
    void joinShouldCoverNonNullEmptyProfileAndExistingProfileDirectory() throws Exception {
        MockMultipartFile emptyProfile = new MockMultipartFile(
                "profileImageFile",
                "",
                "application/octet-stream",
                new byte[0]);

        assertEquals(
                "redirect:/member/login",
                controller.join(
                        new MemberVO(),
                        new BusinessVO(),
                        "USER",
                        null,
                        "Y",
                        emptyProfile,
                        null,
                        new ExtendedModelMap(),
                        new RedirectAttributesModelMap()));

        Files.createDirectories(tempDirectory.resolve("profiles"));

        MockMultipartFile validProfile = new MockMultipartFile(
                "profileImageFile",
                "profile.png",
                "image/png",
                pngBytes());

        assertEquals(
                "redirect:/member/login",
                controller.join(
                        new MemberVO(),
                        new BusinessVO(),
                        "USER",
                        null,
                        "Y",
                        validProfile,
                        null,
                        new ExtendedModelMap(),
                        new RedirectAttributesModelMap()));
    }

    @Test
    void businessJoinShouldCoverAlreadyExistingLicenseDirectory() throws Exception {
        Files.createDirectories(tempDirectory.resolve("licenses"));

        BusinessVO business = new BusinessVO();
        business.setBusinessNumber("123-45-67890");
        business.setRepresentativeName("대표자");
        business.setOpenDate("20260101");

        when(ntsBusinessService.verifyBusiness(
                "123-45-67890",
                "대표자",
                "20260101"))
                .thenReturn(new NtsBusinessVerifyVO(true, "계속사업자", "정상"));

        MockMultipartFile license = new MockMultipartFile(
                "licenseFile",
                "license.pdf",
                "application/pdf",
                new byte[] {1, 2, 3});

        assertEquals(
                "redirect:/member/login",
                controller.join(
                        new MemberVO(),
                        business,
                        "BUSINESS",
                        null,
                        "Y",
                        null,
                        license,
                        new ExtendedModelMap(),
                        new RedirectAttributesModelMap()));
    }

    @Test
    void updateShouldCoverLoginMemberWithoutNumberAndProfileValidationRethrow() {
        MemberVO noNumberMember = new MemberVO();
        MockHttpSession noNumberSession = new MockHttpSession();
        noNumberSession.setAttribute("loginMember", noNumberMember);

        assertEquals(
                "redirect:/member/login",
                controller.updateMember(
                        new MemberVO(),
                        null,
                        null,
                        null,
                        null,
                        noNumberSession,
                        new RedirectAttributesModelMap()));

        MemberVO loginMember = new MemberVO();
        loginMember.setMemberNo(10L);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginMember", loginMember);

        MockMultipartFile oversizedProfile = new MockMultipartFile(
                "profileImageFile",
                "large.png",
                "image/png",
                new byte[] {1}) {
            @Override
            public long getSize() {
                return 5L * 1024L * 1024L + 1L;
            }
        };

        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals(
                "redirect:/member/mypage",
                controller.updateMember(
                        new MemberVO(),
                        null,
                        null,
                        null,
                        oversizedProfile,
                        session,
                        redirect));
        assertEquals(
                "프로필 이미지는 5MB 이하만 등록할 수 있습니다.",
                redirect.getFlashAttributes().get("errorMessage"));
    }

    @Test
    void memberSessionDisplayNameShouldCoverBlankNameAfterNicknameFallback() {
        MemberVO blankName = new MemberVO();
        blankName.setNickname("   ");
        blankName.setMemberName("   ");
        MockHttpSession blankSession = new MockHttpSession();

        ReflectionTestUtils.invokeMethod(
                controller,
                "updateMemberSession",
                blankSession,
                blankName);

        assertEquals("회원", blankSession.getAttribute("loginDisplayName"));

        MemberVO realNameFallback = new MemberVO();
        realNameFallback.setNickname(null);
        realNameFallback.setMemberName("실명");
        MockHttpSession realNameSession = new MockHttpSession();

        ReflectionTestUtils.invokeMethod(
                controller,
                "updateMemberSession",
                realNameSession,
                realNameFallback);

        assertEquals("실명", realNameSession.getAttribute("loginDisplayName"));
    }

    @Test
    void changePasswordPostShouldHandleVerificationChangingAfterAvailabilityCheck() {
        HttpSession session = mock(HttpSession.class);
        long future = System.currentTimeMillis() + 60_000L;

        when(session.getAttribute("pwResetMemberNo"))
                .thenReturn(77L)
                .thenReturn(77L);
        when(session.getAttribute("pwResetExpiresAt"))
                .thenReturn(future);
        when(session.getAttribute("pwResetVerified"))
                .thenReturn(Boolean.TRUE)
                .thenReturn(Boolean.FALSE);

        assertEquals(
                "redirect:/member/findPw",
                controller.changePwPost(
                        "NewPassword1!",
                        "NewPassword1!",
                        session,
                        new RedirectAttributesModelMap(),
                        new ExtendedModelMap()));
    }

    @Test
    void redirectNormalizerShouldRejectQueryOnlyUrlWithBlankPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setContextPath("/oditji");

        String normalized = ReflectionTestUtils.invokeMethod(
                controller,
                "normalizeRedirectUrl",
                "?tab=all",
                request);

        assertNull(normalized);
    }

    private byte[] pngBytes() throws Exception {
        BufferedImage image = new BufferedImage(
                1,
                1,
                BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
