package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
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
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.order.service.OrderService;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.subscription.service.SubscriptionCalculatorService;
import com.project.oditji.wish.service.WishService;

/** 회원가입, 로그아웃, 회원정보 수정의 파일·세션·검증 분기를 보완합니다. */
@ExtendWith(MockitoExtension.class)
class MemberControllerWriteCoverageTest {

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
                subscriptionCalculatorService,
                tempDirectory.resolve("profiles").toString(),
                tempDirectory.resolve("licenses").toString());
    }

    @Test
    void regularJoinShouldSaveProfileAndDelegateWithOttSelection() throws Exception {
        MemberVO member = new MemberVO();
        BusinessVO business = new BusinessVO();
        MockMultipartFile profile = new MockMultipartFile(
                "profileImageFile",
                "profile.png",
                "image/png",
                pngBytes());
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals(
                "redirect:/member/login",
                controller.join(
                        member,
                        business,
                        "USER",
                        List.of("1", "2"),
                        "N",
                        profile,
                        null,
                        new ExtendedModelMap(),
                        redirect));

        verify(memberService).joinMember(member, List.of("1", "2"), "N");
        assertNotNull(member.getProfileImage());
        assertTrue(member.getProfileImage().endsWith(".png"));
        assertTrue(Files.exists(tempDirectory.resolve("profiles").resolve(member.getProfileImage())));
        assertEquals("회원가입이 완료되었습니다.", redirect.getFlashAttributes().get("message"));
    }

    @Test
    void businessJoinShouldVerifyStatusSaveLicenseAndDelegate() {
        MemberVO member = new MemberVO();
        BusinessVO business = business("123-45-67890", "대표자", "20260101");
        NtsBusinessVerifyVO verification = new NtsBusinessVerifyVO(
                true,
                "계속사업자",
                "정상");
        when(ntsBusinessService.verifyBusiness("123-45-67890", "대표자", "20260101"))
                .thenReturn(verification);
        MockMultipartFile license = new MockMultipartFile(
                "licenseFile",
                "license.PDF",
                "application/pdf",
                "pdf".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals(
                "redirect:/member/login",
                controller.join(
                        member,
                        business,
                        "business",
                        null,
                        "Y",
                        null,
                        license,
                        new ExtendedModelMap(),
                        redirect));

        ArgumentCaptor<BusinessVO> captor = ArgumentCaptor.forClass(BusinessVO.class);
        verify(memberService).joinBusinessMember(any(MemberVO.class), captor.capture());
        BusinessVO savedBusiness = captor.getValue();
        assertEquals("계속사업자", savedBusiness.getNtsBusinessStatus());
        assertNotNull(savedBusiness.getLicenseFilePath());
        assertTrue(savedBusiness.getLicenseFilePath().endsWith(".pdf"));
        assertTrue(Files.exists(tempDirectory.resolve("licenses").resolve(savedBusiness.getLicenseFilePath())));
        assertEquals(
                "사업자 회원가입 신청이 완료되었습니다. 관리자 승인 후 이용할 수 있습니다.",
                redirect.getFlashAttributes().get("message"));
    }

    @Test
    void businessJoinShouldExposeVerificationAndLicenseValidationMessages() {
        MemberVO member = new MemberVO();
        BusinessVO business = business("123", "대표", "20260101");

        when(ntsBusinessService.verifyBusiness("123", "대표", "20260101"))
                .thenReturn(null);
        ExtendedModelMap missingVerifyModel = new ExtendedModelMap();
        assertEquals(
                "member/join",
                controller.join(
                        member,
                        business,
                        "BUSINESS",
                        null,
                        "Y",
                        null,
                        null,
                        missingVerifyModel,
                        new RedirectAttributesModelMap()));
        assertEquals(
                "사업자 정보를 확인할 수 없습니다.",
                missingVerifyModel.get("errorMessage"));

        NtsBusinessVerifyVO invalid = new NtsBusinessVerifyVO(false, null, "진위 확인 실패");
        when(ntsBusinessService.verifyBusiness("123", "대표", "20260101"))
                .thenReturn(invalid);
        ExtendedModelMap invalidVerifyModel = new ExtendedModelMap();
        controller.join(
                new MemberVO(),
                business,
                "BUSINESS",
                null,
                "Y",
                null,
                null,
                invalidVerifyModel,
                new RedirectAttributesModelMap());
        assertEquals("진위 확인 실패", invalidVerifyModel.get("errorMessage"));

        NtsBusinessVerifyVO closed = new NtsBusinessVerifyVO(true, "폐업자", "확인됨");
        when(ntsBusinessService.verifyBusiness("123", "대표", "20260101"))
                .thenReturn(closed);
        ExtendedModelMap closedModel = new ExtendedModelMap();
        controller.join(
                new MemberVO(),
                business,
                "BUSINESS",
                null,
                "Y",
                null,
                null,
                closedModel,
                new RedirectAttributesModelMap());
        assertEquals(
                "계속사업자만 사업자 회원가입이 가능합니다.",
                closedModel.get("errorMessage"));

        NtsBusinessVerifyVO valid = new NtsBusinessVerifyVO(true, "계속사업자", "정상");
        when(ntsBusinessService.verifyBusiness("123", "대표", "20260101"))
                .thenReturn(valid);
        MockMultipartFile invalidLicense = new MockMultipartFile(
                "licenseFile",
                "license.exe",
                "application/octet-stream",
                new byte[] {1});
        ExtendedModelMap invalidLicenseModel = new ExtendedModelMap();
        controller.join(
                new MemberVO(),
                business,
                "BUSINESS",
                null,
                "Y",
                null,
                invalidLicense,
                invalidLicenseModel,
                new RedirectAttributesModelMap());
        assertEquals(
                "사업자등록증은 PDF, JPG, JPEG, PNG 파일만 등록할 수 있습니다.",
                invalidLicenseModel.get("errorMessage"));
    }

    @Test
    void joinShouldRejectInvalidProfileAndConvertUnexpectedFailureToGenericMessage() {
        MockMultipartFile invalidImage = new MockMultipartFile(
                "profileImageFile",
                "fake.png",
                "image/png",
                "not-image".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        ExtendedModelMap invalidImageModel = new ExtendedModelMap();

        controller.join(
                new MemberVO(),
                new BusinessVO(),
                "USER",
                null,
                "Y",
                invalidImage,
                null,
                invalidImageModel,
                new RedirectAttributesModelMap());
        assertEquals(
                "정상적인 이미지 파일이 아닙니다.",
                invalidImageModel.get("errorMessage"));
        verify(memberService, never()).joinMember(any(), any(), any());

        doThrow(new RuntimeException("DB 장애"))
                .when(memberService)
                .joinMember(any(MemberVO.class), any(), any());
        ExtendedModelMap genericFailureModel = new ExtendedModelMap();
        controller.join(
                new MemberVO(),
                new BusinessVO(),
                "USER",
                null,
                "Y",
                null,
                null,
                genericFailureModel,
                new RedirectAttributesModelMap());
        assertEquals(
                "회원가입 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                genericFailureModel.get("errorMessage"));
    }

    @Test
    void logoutShouldReturnOnlySafeSameOriginPreviousPaths() {
        MockHttpServletRequest validRequest = request();
        validRequest.addHeader(
                "Referer",
                "http://localhost/oditji/content/list?tab=all");
        assertEquals(
                "redirect:/content/list?tab=all",
                controller.logout(validRequest, new MockHttpSession()));

        MockHttpServletRequest loginRequest = request();
        loginRequest.addHeader("Referer", "http://localhost/oditji/member/login");
        assertEquals("redirect:/", controller.logout(loginRequest, new MockHttpSession()));

        MockHttpServletRequest externalRequest = request();
        externalRequest.addHeader("Referer", "https://example.com/oditji/content/list");
        assertEquals("redirect:/", controller.logout(externalRequest, new MockHttpSession()));

        MockHttpServletRequest malformedRequest = request();
        malformedRequest.addHeader("Referer", "http://[invalid");
        assertEquals("redirect:/", controller.logout(malformedRequest, new MockHttpSession()));

        assertEquals("redirect:/", controller.logout(request(), new MockHttpSession()));
    }

    @Test
    void updateMemberShouldRequireLoginAndHandlePasswordValidationErrors() {
        assertEquals(
                "redirect:/member/login",
                controller.updateMember(
                        new MemberVO(),
                        null,
                        null,
                        null,
                        null,
                        new MockHttpSession(),
                        new RedirectAttributesModelMap()));

        MockHttpSession session = session(10L);
        RedirectAttributesModelMap missingCurrent = new RedirectAttributesModelMap();
        assertEquals(
                "redirect:/member/mypage",
                controller.updateMember(
                        new MemberVO(),
                        null,
                        "NewPassword1!",
                        "NewPassword1!",
                        null,
                        session,
                        missingCurrent));
        assertEquals("현재 비밀번호를 입력해주세요.", missingCurrent.getFlashAttributes().get("errorMessage"));

        RedirectAttributesModelMap mismatch = new RedirectAttributesModelMap();
        controller.updateMember(
                new MemberVO(),
                "old",
                "NewPassword1!",
                "OtherPassword1!",
                null,
                session,
                mismatch);
        assertEquals("새 비밀번호가 일치하지 않습니다.", mismatch.getFlashAttributes().get("errorMessage"));

        when(memberService.checkPassword(10L, "wrong")).thenReturn(false);
        RedirectAttributesModelMap wrongCurrent = new RedirectAttributesModelMap();
        controller.updateMember(
                new MemberVO(),
                "wrong",
                "NewPassword1!",
                "NewPassword1!",
                null,
                session,
                wrongCurrent);
        assertEquals("현재 비밀번호가 일치하지 않습니다.", wrongCurrent.getFlashAttributes().get("errorMessage"));
    }

    @Test
    void updateMemberShouldUpdatePasswordImageAndSessionDisplayFallbacks() throws Exception {
        MockHttpSession session = session(20L);
        when(memberService.checkPassword(20L, "OldPassword1!")).thenReturn(true);
        MemberVO updated = new MemberVO();
        updated.setMemberNo(20L);
        updated.setMemberName(" ");
        updated.setNickname("새닉네임");
        when(memberService.getMemberByNo(20L)).thenReturn(updated);
        MockMultipartFile profile = new MockMultipartFile(
                "profileImageFile",
                "new.jpg",
                "image/jpeg",
                jpegBytes());
        MemberVO input = new MemberVO();
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertEquals(
                "redirect:/member/mypage",
                controller.updateMember(
                        input,
                        "OldPassword1!",
                        "NewPassword1!",
                        "NewPassword1!",
                        profile,
                        session,
                        redirect));

        ArgumentCaptor<MemberVO> captor = ArgumentCaptor.forClass(MemberVO.class);
        verify(memberService).updateMember(captor.capture());
        MemberVO saved = captor.getValue();
        assertEquals(20L, saved.getMemberNo());
        assertEquals("NewPassword1!", saved.getMemberPw());
        assertNotNull(saved.getProfileImage());
        assertTrue(saved.getProfileImage().endsWith(".jpg"));
        assertEquals(updated, session.getAttribute("loginMember"));
        assertEquals("새닉네임", session.getAttribute("nickname"));
        assertEquals("새닉네임", session.getAttribute("loginDisplayName"));
        assertEquals("회원정보가 수정되었습니다.", redirect.getFlashAttributes().get("message"));

        MemberVO noName = new MemberVO();
        noName.setMemberNo(21L);
        when(memberService.getMemberByNo(21L)).thenReturn(noName);
        MockHttpSession noNameSession = session(21L);
        controller.updateMember(
                new MemberVO(),
                null,
                null,
                null,
                null,
                noNameSession,
                new RedirectAttributesModelMap());
        assertEquals("회원", noNameSession.getAttribute("loginDisplayName"));
    }

    @Test
    void updateMemberShouldExposeServiceAndImageUploadFailures() throws Exception {
        MockHttpSession session = session(30L);
        doThrow(new IllegalStateException("회원정보 저장 실패"))
                .when(memberService)
                .updateMember(any(MemberVO.class));
        RedirectAttributesModelMap serviceFailure = new RedirectAttributesModelMap();
        controller.updateMember(
                new MemberVO(),
                null,
                null,
                null,
                null,
                session,
                serviceFailure);
        assertEquals("회원정보 저장 실패", serviceFailure.getFlashAttributes().get("errorMessage"));

        MultipartFile brokenImage = mock(MultipartFile.class);
        when(brokenImage.isEmpty()).thenReturn(false);
        when(brokenImage.getSize()).thenReturn(10L);
        when(brokenImage.getContentType()).thenReturn("image/png");
        when(brokenImage.getInputStream()).thenThrow(new java.io.IOException("읽기 실패"));
        RedirectAttributesModelMap imageFailure = new RedirectAttributesModelMap();
        controller.updateMember(
                new MemberVO(),
                null,
                null,
                null,
                brokenImage,
                session,
                imageFailure);
        assertEquals("이미지 업로드에 실패했습니다.", imageFailure.getFlashAttributes().get("errorMessage"));
    }

    private BusinessVO business(String number, String representative, String openDate) {
        BusinessVO business = new BusinessVO();
        business.setBusinessNumber(number);
        business.setRepresentativeName(representative);
        business.setOpenDate(openDate);
        return business;
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(80);
        request.setContextPath("/oditji");
        return request;
    }

    private MockHttpSession session(Long memberNo) {
        MockHttpSession session = new MockHttpSession();
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        session.setAttribute("loginMember", member);
        return session;
    }

    private byte[] pngBytes() throws Exception {
        return imageBytes("png");
    }

    private byte[] jpegBytes() throws Exception {
        return imageBytes("jpg");
    }

    private byte[] imageBytes(String format) throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, format, output));
        return output.toByteArray();
    }
}