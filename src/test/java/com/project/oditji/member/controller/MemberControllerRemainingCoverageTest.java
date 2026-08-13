package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.service.NtsBusinessService;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.NtsBusinessVerifyVO;
import com.project.oditji.favorite.service.FavoriteService;
import com.project.oditji.mail.service.MailService;
import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.order.service.OrderService;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.subscription.service.SubscriptionCalculatorService;
import com.project.oditji.wish.service.WishService;

/**
 * 기존 MemberController 커버리지 테스트에서 남기 쉬운 복합 조건의 반대 분기를 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class MemberControllerRemainingCoverageTest {

    @TempDir
    Path tempDirectory;

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

    private MemberController controller;

    @BeforeEach
    void setUp() {
        controller = newController(
                tempDirectory.resolve("profiles"),
                tempDirectory.resolve("licenses"));
    }

    @Test
    void businessJoinShouldCoverMissingEmptyAndFilenameValidationBranches() {
        BusinessVO business = business("123-45-67890", "대표자", "20260101");
        when(ntsBusinessService.verifyBusiness("123-45-67890", "대표자", "20260101"))
                .thenReturn(validBusinessVerification());

        ExtendedModelMap missingModel = new ExtendedModelMap();
        assertEquals(
                "member/join",
                controller.join(
                        new MemberVO(),
                        business,
                        "BUSINESS",
                        null,
                        "Y",
                        null,
                        null,
                        missingModel,
                        new RedirectAttributesModelMap()));
        assertEquals("사업자등록증을 첨부해주세요.", missingModel.get("errorMessage"));

        MockMultipartFile emptyLicense = new MockMultipartFile(
                "licenseFile",
                "license.pdf",
                "application/pdf",
                new byte[0]);
        ExtendedModelMap emptyModel = new ExtendedModelMap();
        assertEquals(
                "member/join",
                controller.join(
                        new MemberVO(),
                        business,
                        "BUSINESS",
                        null,
                        "Y",
                        null,
                        emptyLicense,
                        emptyModel,
                        new RedirectAttributesModelMap()));
        assertEquals("사업자등록증을 첨부해주세요.", emptyModel.get("errorMessage"));

        MultipartFile nullNameLicense = mock(MultipartFile.class);
        when(nullNameLicense.isEmpty()).thenReturn(false);
        when(nullNameLicense.getOriginalFilename()).thenReturn(null);
        ExtendedModelMap nullNameModel = new ExtendedModelMap();
        assertEquals(
                "member/join",
                controller.join(
                        new MemberVO(),
                        business,
                        "BUSINESS",
                        null,
                        "Y",
                        null,
                        nullNameLicense,
                        nullNameModel,
                        new RedirectAttributesModelMap()));
        assertEquals("사업자등록증 파일 형식이 올바르지 않습니다.", nullNameModel.get("errorMessage"));

        MockMultipartFile noDotLicense = new MockMultipartFile(
                "licenseFile",
                "license",
                "application/octet-stream",
                "x".getBytes(StandardCharsets.UTF_8));
        ExtendedModelMap noDotModel = new ExtendedModelMap();
        assertEquals(
                "member/join",
                controller.join(
                        new MemberVO(),
                        business,
                        "BUSINESS",
                        null,
                        "Y",
                        null,
                        noDotLicense,
                        noDotModel,
                        new RedirectAttributesModelMap()));
        assertEquals("사업자등록증 파일 형식이 올바르지 않습니다.", noDotModel.get("errorMessage"));
    }

    @Test
    void businessJoinShouldAcceptEveryAllowedLicenseExtension() {
        BusinessVO business = business("123-45-67890", "대표자", "20260101");
        when(ntsBusinessService.verifyBusiness("123-45-67890", "대표자", "20260101"))
                .thenReturn(validBusinessVerification());

        assertBusinessJoinSuccess(business, "license.jpg");
        assertBusinessJoinSuccess(business, "license.jpeg");
        assertBusinessJoinSuccess(business, "license.png");

        verify(memberService, times(3))
                .joinBusinessMember(any(MemberVO.class), eq(business));
    }

    @Test
    void businessJoinShouldUseDefaultVerificationMessageWhenInvalidMessageIsNull() {
        BusinessVO business = business("123", "대표", "20260101");
        NtsBusinessVerifyVO invalid = new NtsBusinessVerifyVO(false, null, null);
        when(ntsBusinessService.verifyBusiness("123", "대표", "20260101"))
                .thenReturn(invalid);

        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals(
                "member/join",
                controller.join(
                        new MemberVO(),
                        business,
                        "BUSINESS",
                        null,
                        "Y",
                        null,
                        null,
                        model,
                        new RedirectAttributesModelMap()));

        assertEquals("사업자 정보를 확인할 수 없습니다.", model.get("errorMessage"));
    }

    @Test
    void joinShouldCoverProfileSizeContentTypeAndDirectoryFailureBranches() throws Exception {
        MultipartFile oversized = mock(MultipartFile.class);
        when(oversized.isEmpty()).thenReturn(false);
        when(oversized.getSize()).thenReturn(5L * 1024 * 1024 + 1);
        ExtendedModelMap oversizedModel = new ExtendedModelMap();
        assertEquals(
                "member/join",
                controller.join(
                        new MemberVO(),
                        new BusinessVO(),
                        "USER",
                        null,
                        "Y",
                        oversized,
                        null,
                        oversizedModel,
                        new RedirectAttributesModelMap()));
        assertEquals("프로필 이미지는 5MB 이하만 등록할 수 있습니다.", oversizedModel.get("errorMessage"));

        MultipartFile wrongContentType = mock(MultipartFile.class);
        when(wrongContentType.isEmpty()).thenReturn(false);
        when(wrongContentType.getSize()).thenReturn(10L);
        when(wrongContentType.getContentType()).thenReturn("image/gif");
        ExtendedModelMap typeModel = new ExtendedModelMap();
        assertEquals(
                "member/join",
                controller.join(
                        new MemberVO(),
                        new BusinessVO(),
                        "USER",
                        null,
                        "Y",
                        wrongContentType,
                        null,
                        typeModel,
                        new RedirectAttributesModelMap()));
        assertEquals(
                "프로필 이미지는 JPG, JPEG, PNG 파일만 등록할 수 있습니다.",
                typeModel.get("errorMessage"));

        Path blockingFile = tempDirectory.resolve("profile-parent-file");
        Files.writeString(blockingFile, "block");
        MemberController blockedController = newController(
                blockingFile.resolve("child"),
                tempDirectory.resolve("licenses-other"));
        MockMultipartFile validImage = new MockMultipartFile(
                "profileImageFile",
                "profile.png",
                "image/png",
                imageBytes("png"));
        ExtendedModelMap directoryModel = new ExtendedModelMap();
        assertEquals(
                "member/join",
                blockedController.join(
                        new MemberVO(),
                        new BusinessVO(),
                        "USER",
                        null,
                        "Y",
                        validImage,
                        null,
                        directoryModel,
                        new RedirectAttributesModelMap()));
        assertEquals(
                "회원가입 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                directoryModel.get("errorMessage"));
    }

    @Test
    void businessJoinShouldExposeLicenseDirectoryCreationFailure() throws Exception {
        Path blockingFile = tempDirectory.resolve("license-parent-file");
        Files.writeString(blockingFile, "block");
        MemberController blockedController = newController(
                tempDirectory.resolve("profiles-other"),
                blockingFile.resolve("child"));

        BusinessVO business = business("222-33-44444", "대표", "20260101");
        when(ntsBusinessService.verifyBusiness("222-33-44444", "대표", "20260101"))
                .thenReturn(validBusinessVerification());
        MockMultipartFile license = new MockMultipartFile(
                "licenseFile",
                "license.pdf",
                "application/pdf",
                "pdf".getBytes(StandardCharsets.UTF_8));
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals(
                "member/join",
                blockedController.join(
                        new MemberVO(),
                        business,
                        "BUSINESS",
                        null,
                        "Y",
                        null,
                        license,
                        model,
                        new RedirectAttributesModelMap()));
        assertEquals(
                "회원가입 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                model.get("errorMessage"));
    }

    @Test
    void loginFormShouldCoverRefererRelativeRootPortAndMemberPageBranches() {
        MockHttpServletRequest refererRequest = request("/oditji", 80);
        refererRequest.addHeader("Referer", "http://localhost/oditji/content/detail?contentNo=1");
        MockHttpSession refererSession = new MockHttpSession();
        assertEquals("member/login", controller.loginForm(null, refererRequest, refererSession));
        assertEquals("/content/detail?contentNo=1", refererSession.getAttribute("redirectAfterLogin"));

        MockHttpSession relativeSession = new MockHttpSession();
        assertEquals("member/login", controller.loginForm("recommend?page=2", request("/oditji", 80), relativeSession));
        assertEquals("/recommend?page=2", relativeSession.getAttribute("redirectAfterLogin"));

        MockHttpSession rootSession = new MockHttpSession();
        assertEquals("member/login", controller.loginForm("/oditji", request("/oditji", 80), rootSession));
        assertEquals("/", rootSession.getAttribute("redirectAfterLogin"));

        MockHttpSession secureSession = new MockHttpSession();
        assertEquals(
                "member/login",
                controller.loginForm("https://localhost/oditji/secure", request("/oditji", 443), secureSession));
        assertEquals("/secure", secureSession.getAttribute("redirectAfterLogin"));

        MockHttpSession mismatchPortSession = new MockHttpSession();
        assertEquals(
                "member/login",
                controller.loginForm("http://localhost:8080/oditji/content", request("/oditji", 80), mismatchPortSession));
        assertNull(mismatchPortSession.getAttribute("redirectAfterLogin"));

        MockHttpSession hostlessSession = new MockHttpSession();
        assertEquals(
                "member/login",
                controller.loginForm("mailto:test@example.com", request("/oditji", 80), hostlessSession));
        assertNull(hostlessSession.getAttribute("redirectAfterLogin"));

        MockHttpSession emptyPathSession = new MockHttpSession();
        assertEquals("member/login", controller.loginForm("?page=1", request("/oditji", 80), emptyPathSession));
        assertNull(emptyPathSession.getAttribute("redirectAfterLogin"));

        for (String path : List.of(
                "/member/login?x=1",
                "/member/logout",
                "/member/join",
                "/member/findId",
                "/member/findPw")) {
            MockHttpSession memberPageSession = new MockHttpSession();
            assertEquals("member/login", controller.loginForm(path, request("/oditji", 80), memberPageSession));
            assertNull(memberPageSession.getAttribute("redirectAfterLogin"));
        }
    }

    @Test
    void loginShouldCoverBlockedWithdrawnIllegalStateAndOtherBusinessStatuses() {
        MemberVO blockedInput = new MemberVO();
        when(memberService.loginMember(blockedInput))
                .thenThrow(new MemberBlockedException("정지된 회원입니다."));
        RedirectAttributesModelMap blockedRedirect = new RedirectAttributesModelMap();
        assertEquals(
                "redirect:/member/login",
                controller.login(blockedInput, request("/oditji", 80), new MockHttpSession(), blockedRedirect));
        assertEquals("정지된 회원입니다.", blockedRedirect.getFlashAttributes().get("blockedMessage"));

        MemberVO withdrawnInput = new MemberVO();
        when(memberService.loginMember(withdrawnInput))
                .thenThrow(new MemberWithdrawnException(
                        "탈퇴 회원",
                        91L,
                        LocalDateTime.now().minusDays(1)));
        MockHttpSession withdrawnSession = new MockHttpSession();
        RedirectAttributesModelMap withdrawnRedirect = new RedirectAttributesModelMap();
        assertEquals(
                "redirect:/member/login",
                controller.login(withdrawnInput, request("/oditji", 80), withdrawnSession, withdrawnRedirect));
        assertEquals(91L, withdrawnSession.getAttribute("restoreMemberNo"));
        assertEquals("LOCAL", withdrawnSession.getAttribute("restoreProvider"));
        assertTrue(withdrawnRedirect.getFlashAttributes().containsKey("withdrawnMessage"));

        MemberVO errorInput = new MemberVO();
        when(memberService.loginMember(errorInput))
                .thenThrow(new IllegalStateException("로그인 저장소 오류"));
        RedirectAttributesModelMap errorRedirect = new RedirectAttributesModelMap();
        assertEquals(
                "redirect:/member/login",
                controller.login(errorInput, request("/oditji", 80), new MockHttpSession(), errorRedirect));
        assertEquals(
                "로그인 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                errorRedirect.getFlashAttributes().get("errorMessage"));

        MemberVO rejectedMember = member(92L, "BUSINESS", "대표", "닉네임");
        when(memberService.loginMember(rejectedMember)).thenReturn(rejectedMember);
        BusinessVO rejected = businessState(920L, "REJECTED", "거절상점");
        rejected.setRejectReason("   ");
        when(businessService.getBusinessByMemberNo(92L)).thenReturn(rejected);
        MockHttpSession rejectedSession = new MockHttpSession();
        MockHttpServletRequest rejectedRequest = request("/oditji", 80);
        rejectedRequest.setSession(rejectedSession);
        assertEquals(
                "redirect:/business/main",
                controller.login(
                        rejectedMember,
                        rejectedRequest,
                        rejectedSession,
                        new RedirectAttributesModelMap()));
        assertEquals("REJECTED", rejectedSession.getAttribute("businessStatus"));

        MemberVO unknownMember = member(93L, "BUSINESS", "대표", "닉네임");
        when(memberService.loginMember(unknownMember)).thenReturn(unknownMember);
        when(businessService.getBusinessByMemberNo(93L))
                .thenReturn(businessState(930L, "SUSPENDED", "상태상점"));
        MockHttpSession unknownSession = new MockHttpSession();
        MockHttpServletRequest unknownRequest = request("/oditji", 80);
        unknownRequest.setSession(unknownSession);
        assertEquals(
                "redirect:/business/main",
                controller.login(
                        unknownMember,
                        unknownRequest,
                        unknownSession,
                        new RedirectAttributesModelMap()));
        assertEquals("SUSPENDED", unknownSession.getAttribute("businessStatus"));
    }

    @Test
    void successfulLoginShouldCoverApprovedBusinessAndDisplayNameFallbacks() {
        MemberVO businessMember = member(101L, "BUSINESS", "회원이름", "회원닉");
        businessMember.setMemberId("business101");
        BusinessVO approved = businessState(1001L, "APPROVED", " ");
        when(memberService.loginMember(businessMember)).thenReturn(businessMember);
        when(businessService.getBusinessByMemberNo(101L)).thenReturn(approved);
        MockHttpSession businessSession = new MockHttpSession();
        MockHttpServletRequest businessRequest = request("/oditji", 80);
        businessRequest.setSession(businessSession);

        assertEquals(
                "redirect:/",
                controller.login(
                        businessMember,
                        businessRequest,
                        businessSession,
                        new RedirectAttributesModelMap()));
        assertEquals(1001L, businessSession.getAttribute("businessNo"));
        assertEquals("APPROVED", businessSession.getAttribute("businessStatus"));
        assertEquals("회원", businessSession.getAttribute("loginDisplayName"));

        MemberVO nicknameMember = member(102L, "USER", " ", "닉네임표시");
        nicknameMember.setMemberId("user102");
        when(memberService.loginMember(nicknameMember)).thenReturn(nicknameMember);
        MockHttpSession nicknameSession = new MockHttpSession();
        MockHttpServletRequest nicknameRequest = request("/oditji", 80);
        nicknameRequest.setSession(nicknameSession);
        assertEquals(
                "redirect:/",
                controller.login(
                        nicknameMember,
                        nicknameRequest,
                        nicknameSession,
                        new RedirectAttributesModelMap()));
        assertEquals("닉네임표시", nicknameSession.getAttribute("loginDisplayName"));

        MemberVO anonymousNameMember = member(103L, "USER", null, " ");
        anonymousNameMember.setMemberId("user103");
        when(memberService.loginMember(anonymousNameMember)).thenReturn(anonymousNameMember);
        MockHttpSession anonymousSession = new MockHttpSession();
        MockHttpServletRequest anonymousRequest = request("/oditji", 80);
        anonymousRequest.setSession(anonymousSession);
        assertEquals(
                "redirect:/",
                controller.login(
                        anonymousNameMember,
                        anonymousRequest,
                        anonymousSession,
                        new RedirectAttributesModelMap()));
        assertEquals("회원", anonymousSession.getAttribute("loginDisplayName"));
    }

    @Test
    void mypageAndMemberUpdateChecksShouldCoverNullMemberNumbersAndOppositeAvailability() {
        MemberVO noNumber = new MemberVO();
        noNumber.setRole("USER");
        MockHttpSession noNumberSession = session(noNumber);
        assertEquals("redirect:/member/login", controller.mypage(noNumberSession, new ExtendedModelMap()));
        assertEquals("N", controller.checkUpdateNickname("nick", noNumberSession));
        assertEquals("N", controller.checkUpdateEmail("mail@test.com", noNumberSession));
        assertEquals(
                "redirect:/member/login",
                controller.updateOtt(List.of("1"), noNumberSession, new RedirectAttributesModelMap()));
        assertEquals("redirect:/member/login", controller.withdrawMember(noNumberSession));

        MemberVO normal = member(104L, "USER", "회원", "닉");
        MockHttpSession normalSession = session(normal);
        assertEquals("N", controller.checkUpdateNickname(null, normalSession));
        assertEquals("N", controller.checkUpdateEmail(null, normalSession));
        when(memberService.checkUpdateNickname(104L, "usedNick")).thenReturn(false);
        when(memberService.checkUpdateEmail(104L, "free@test.com")).thenReturn(true);
        assertEquals("N", controller.checkUpdateNickname(" usedNick ", normalSession));
        assertEquals("Y", controller.checkUpdateEmail(" free@test.com ", normalSession));
    }

    @Test
    void mypageShouldCoverNonSocialRegularMemberBranch() {
        MemberVO normal = member(105L, "USER", "회원", "닉");
        MockHttpSession normalSession = session(normal);
        when(favoriteService.getFavoriteCount(105L)).thenReturn(0);
        when(wishService.getWishCount(105L)).thenReturn(0);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("member/mypage", controller.mypage(normalSession, model));
        assertEquals(Boolean.FALSE, model.get("socialMember"));
        assertNull(model.get("loginProvider"));
        assertEquals(0, model.get("favoriteCount"));
    }

    @Test
    void findPasswordShouldCoverMailSuccessMaskVariantsAndMailFailure() {
        MemberVO shortEmailMember = member(201L, "USER", "회원", "닉");
        shortEmailMember.setEmail("ab@test.com");
        when(memberService.findPw(any(MemberVO.class))).thenReturn(shortEmailMember);
        MockHttpSession shortSession = new MockHttpSession();
        ExtendedModelMap shortModel = new ExtendedModelMap();
        assertEquals(
                "member/findPw",
                controller.findPwPost(" member ", " name ", " ab@test.com ", shortSession, shortModel));
        assertEquals(Boolean.TRUE, shortModel.get("verificationStep"));
        assertEquals("a*@test.com", shortModel.get("maskedEmail"));
        assertEquals(Boolean.FALSE, shortSession.getAttribute("pwResetVerified"));
        verify(mailService).sendPasswordResetCode(eq("ab@test.com"), anyString());

        MemberVO longEmailMember = member(202L, "USER", "회원", "닉");
        longEmailMember.setEmail("long@test.com");
        when(memberService.findPw(any(MemberVO.class))).thenReturn(longEmailMember);
        ExtendedModelMap longModel = new ExtendedModelMap();
        assertEquals(
                "member/findPw",
                controller.findPwPost(
                        "member2",
                        "name2",
                        "long@test.com",
                        new MockHttpSession(),
                        longModel));
        assertEquals("lo**@test.com", longModel.get("maskedEmail"));

        MemberVO invalidEmailMember = member(203L, "USER", "회원", "닉");
        invalidEmailMember.setEmail("invalid-email");
        when(memberService.findPw(any(MemberVO.class))).thenReturn(invalidEmailMember);
        ExtendedModelMap invalidModel = new ExtendedModelMap();
        assertEquals(
                "member/findPw",
                controller.findPwPost(
                        "member3",
                        "name3",
                        "invalid-email",
                        new MockHttpSession(),
                        invalidModel));
        assertEquals("", invalidModel.get("maskedEmail"));

        MemberVO mailFailureMember = member(204L, "USER", "회원", "닉");
        mailFailureMember.setEmail("fail@test.com");
        when(memberService.findPw(any(MemberVO.class))).thenReturn(mailFailureMember);
        doThrow(new IllegalStateException("메일 발송 실패"))
                .when(mailService)
                .sendPasswordResetCode(eq("fail@test.com"), anyString());
        MockHttpSession failureSession = new MockHttpSession();
        ExtendedModelMap failureModel = new ExtendedModelMap();
        assertEquals(
                "member/findPw",
                controller.findPwPost(
                        "member4",
                        "name4",
                        "fail@test.com",
                        failureSession,
                        failureModel));
        assertEquals("메일 발송 실패", failureModel.get("errorMessage"));
        assertNull(failureSession.getAttribute("pwResetMemberNo"));
        assertNull(failureSession.getAttribute("pwResetCode"));
    }

    @Test
    void verifyPasswordCodeShouldCoverIndividualMissingAttemptAndNullCodeBranches() {
        long future = System.currentTimeMillis() + 60_000;

        MockHttpSession missingMember = new MockHttpSession();
        missingMember.setAttribute("pwResetCode", "123456");
        missingMember.setAttribute("pwResetExpiresAt", future);
        assertEquals(
                "member/findPw",
                controller.verifyPwCode("123456", missingMember, new ExtendedModelMap()));

        MockHttpSession missingCode = new MockHttpSession();
        missingCode.setAttribute("pwResetMemberNo", 301L);
        missingCode.setAttribute("pwResetExpiresAt", future);
        assertEquals(
                "member/findPw",
                controller.verifyPwCode("123456", missingCode, new ExtendedModelMap()));

        MockHttpSession missingExpiry = new MockHttpSession();
        missingExpiry.setAttribute("pwResetMemberNo", 302L);
        missingExpiry.setAttribute("pwResetCode", "123456");
        assertEquals(
                "member/findPw",
                controller.verifyPwCode("123456", missingExpiry, new ExtendedModelMap()));

        MockHttpSession maxAttempts = passwordSession(303L, "123456", future, 5, "user@test.com", false);
        ExtendedModelMap maxModel = new ExtendedModelMap();
        assertEquals("member/findPw", controller.verifyPwCode("123456", maxAttempts, maxModel));
        assertEquals(
                "인증번호 입력 횟수를 초과했습니다. 다시 진행해 주세요.",
                maxModel.get("errorMessage"));

        MockHttpSession lastWrong = passwordSession(304L, "123456", future, 4, "user@test.com", false);
        ExtendedModelMap lastWrongModel = new ExtendedModelMap();
        assertEquals("member/findPw", controller.verifyPwCode("000000", lastWrong, lastWrongModel));
        assertEquals(
                "인증번호 입력 횟수를 초과했습니다. 다시 진행해 주세요.",
                lastWrongModel.get("errorMessage"));
        assertNull(lastWrong.getAttribute("pwResetMemberNo"));

        MockHttpSession nullAttemptAndCode = passwordSession(305L, "123456", future, null, "invalid-email", false);
        ExtendedModelMap nullCodeModel = new ExtendedModelMap();
        assertEquals("member/findPw", controller.verifyPwCode(null, nullAttemptAndCode, nullCodeModel));
        assertEquals(1, nullAttemptAndCode.getAttribute("pwResetAttempts"));
        assertEquals("", nullCodeModel.get("maskedEmail"));
        assertTrue(nullCodeModel.get("errorMessage").toString().contains("남은 횟수: 4회"));
    }

    @Test
    void changePasswordAvailabilityShouldCoverEveryShortCircuitCondition() {
        long future = System.currentTimeMillis() + 60_000;

        MockHttpSession noMember = new MockHttpSession();
        noMember.setAttribute("pwResetVerified", true);
        noMember.setAttribute("pwResetExpiresAt", future);
        assertEquals("redirect:/member/findPw", controller.changePw(noMember));

        MockHttpSession unverified = new MockHttpSession();
        unverified.setAttribute("pwResetMemberNo", 401L);
        unverified.setAttribute("pwResetVerified", false);
        unverified.setAttribute("pwResetExpiresAt", future);
        assertEquals("redirect:/member/findPw", controller.changePw(unverified));

        MockHttpSession noExpiry = new MockHttpSession();
        noExpiry.setAttribute("pwResetMemberNo", 402L);
        noExpiry.setAttribute("pwResetVerified", true);
        assertEquals("redirect:/member/findPw", controller.changePw(noExpiry));

        MockHttpSession expired = new MockHttpSession();
        expired.setAttribute("pwResetMemberNo", 403L);
        expired.setAttribute("pwResetVerified", true);
        expired.setAttribute("pwResetExpiresAt", System.currentTimeMillis() - 1);
        assertEquals("redirect:/member/findPw", controller.changePw(expired));

        MockHttpSession valid = new MockHttpSession();
        valid.setAttribute("pwResetMemberNo", 404L);
        valid.setAttribute("pwResetVerified", true);
        valid.setAttribute("pwResetExpiresAt", future);
        assertEquals("member/changePw", controller.changePw(valid));
    }

    @Test
    void updateMemberShouldCoverBlankPasswordEmptyImageAndMemberNameDisplay() {
        MemberVO loginMember = member(501L, "USER", "기존", "기존닉");
        MockHttpSession session = session(loginMember);
        MemberVO updated = member(501L, "USER", "새이름", "새닉");
        when(memberService.getMemberByNo(501L)).thenReturn(updated);
        MockMultipartFile emptyImage = new MockMultipartFile(
                "profileImageFile",
                "",
                "application/octet-stream",
                new byte[0]);
        MemberVO input = new MemberVO();

        assertEquals(
                "redirect:/member/mypage",
                controller.updateMember(
                        input,
                        " ",
                        "   ",
                        "different",
                        emptyImage,
                        session,
                        new RedirectAttributesModelMap()));
        assertNull(input.getMemberPw());
        assertEquals("새이름", session.getAttribute("loginDisplayName"));
        verify(memberService, never()).checkPassword(any(), anyString());
    }

    @Test
    void restoreMemberShouldAlsoHandleIllegalArgumentFailure() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("restoreMemberNo", 601L);
        session.setAttribute("restoreProvider", "LOCAL");
        doThrow(new IllegalArgumentException("복구 대상이 아닙니다."))
                .when(memberService)
                .restoreMember(601L);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals("redirect:/member/login", controller.restoreMember(session, redirect));
        assertEquals("복구 대상이 아닙니다.", redirect.getFlashAttributes().get("errorMessage"));
        assertNull(session.getAttribute("restoreMemberNo"));
        assertNull(session.getAttribute("restoreProvider"));
    }

    private void assertBusinessJoinSuccess(BusinessVO business, String filename) {
        MockMultipartFile license = new MockMultipartFile(
                "licenseFile",
                filename,
                "application/octet-stream",
                "license".getBytes(StandardCharsets.UTF_8));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        ExtendedModelMap model = new ExtendedModelMap();

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
                        model,
                        redirect));
        assertEquals(
                "사업자 회원가입 신청이 완료되었습니다. 관리자 승인 후 이용할 수 있습니다.",
                redirect.getFlashAttributes().get("message"));
        assertFalse(business.getLicenseFilePath().isBlank());
    }

    private MemberController newController(Path profilePath, Path licensePath) {
        return new MemberController(
                memberService,
                memberPlatformService,
                businessService,
                ntsBusinessService,
                mailService,
                favoriteService,
                wishService,
                orderService,
                reviewService,
                subscriptionCalculatorService,
                profilePath.toString(),
                licensePath.toString());
    }

    private NtsBusinessVerifyVO validBusinessVerification() {
        return new NtsBusinessVerifyVO(true, "계속사업자", "정상");
    }

    private BusinessVO business(String number, String representative, String openDate) {
        BusinessVO business = new BusinessVO();
        business.setBusinessNumber(number);
        business.setRepresentativeName(representative);
        business.setOpenDate(openDate);
        return business;
    }

    private BusinessVO businessState(Long businessNo, String status, String businessName) {
        BusinessVO business = new BusinessVO();
        business.setBusinessNo(businessNo);
        business.setStatus(status);
        business.setBusinessName(businessName);
        return business;
    }

    private MemberVO member(Long memberNo, String role, String name, String nickname) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        member.setRole(role);
        member.setMemberName(name);
        member.setNickname(nickname);
        return member;
    }

    private MockHttpSession session(MemberVO member) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginMember", member);
        return session;
    }

    private MockHttpServletRequest request(String contextPath, int port) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme(port == 443 ? "https" : "http");
        request.setServerName("localhost");
        request.setServerPort(port);
        request.setContextPath(contextPath);
        return request;
    }

    private MockHttpSession passwordSession(
            Long memberNo,
            String code,
            long expiresAt,
            Integer attempts,
            String email,
            boolean verified) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("pwResetMemberNo", memberNo);
        if (code != null) {
            session.setAttribute("pwResetCode", code);
        }
        session.setAttribute("pwResetExpiresAt", expiresAt);
        if (attempts != null) {
            session.setAttribute("pwResetAttempts", attempts);
        }
        session.setAttribute("pwResetEmail", email);
        session.setAttribute("pwResetVerified", verified);
        return session;
    }

    private byte[] imageBytes(String format) throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(image, format, output)) {
            throw new IllegalStateException("테스트 이미지 생성에 실패했습니다.");
        }
        return output.toByteArray();
    }
}
