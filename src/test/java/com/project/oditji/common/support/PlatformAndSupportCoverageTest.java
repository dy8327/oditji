package com.project.oditji.common.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.chat.common.ChatResult;
import com.project.oditji.common.service.MainContentPlatformService;
import com.project.oditji.common.util.PlatformNameNormalizer;
import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.support.SocialLoginCallbackSupport;
import com.project.oditji.member.support.SocialLoginCallbackSupport.LoginOptions;
import com.project.oditji.member.support.SocialLoginSessionSupport;
import com.project.oditji.member.support.WithdrawPolicy;
import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.SocialLoginResultVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 플랫폼명 정규화, 소셜 로그인 공통 처리와 탈퇴 정책을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class PlatformAndSupportCoverageTest {

    @Mock
    private MemberPlatformService memberPlatformService;

    @Mock
    private MemberService memberService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private RedirectAttributes redirectAttributes;

    private SocialLoginCallbackSupport callbackSupport;

    @BeforeEach
    void setUp() {
        callbackSupport = new SocialLoginCallbackSupport(
                memberPlatformService,
                memberService);
    }

    @Test
    void platformNameNormalizerShouldHandleSupportedAndUnknownNames() {
        assertEquals("netflix", PlatformNameNormalizer.toKey(" Netflix "));
        assertEquals("netflix", PlatformNameNormalizer.toKey("넷플릭스"));
        assertEquals("tving", PlatformNameNormalizer.toKey("TVING"));
        assertEquals("wavve", PlatformNameNormalizer.toKey("웨이브"));
        assertEquals("disney", PlatformNameNormalizer.toKey("Disney+"));
        assertEquals("watcha", PlatformNameNormalizer.toKey("왓챠"));
        assertEquals("coupang", PlatformNameNormalizer.toKey("쿠팡 플레이"));
        assertEquals("unknownott", PlatformNameNormalizer.toKey("Unknown OTT"));
        assertEquals("", PlatformNameNormalizer.toKey(null));

        assertEquals("", PlatformNameNormalizer.toSupportedKey("other"));
        assertEquals("Netflix", PlatformNameNormalizer.toDisplayName("netflix"));
        assertEquals("TVING", PlatformNameNormalizer.toDisplayName("티빙"));
        assertEquals("wavve", PlatformNameNormalizer.toDisplayName("wavve"));
        assertEquals("Disney Plus", PlatformNameNormalizer.toDisplayName("디즈니 플러스"));
        assertEquals("Watcha", PlatformNameNormalizer.toDisplayName("watcha"));
        assertEquals("Coupangplay", PlatformNameNormalizer.toDisplayName("coupang play"));
        assertNull(PlatformNameNormalizer.toDisplayName("other"));
    }

    @Test
    void withdrawPolicyShouldCalculateRemainingDaysAndMessages() {
        assertEquals(0, WithdrawPolicy.calcDaysLeft(null));

        Date now = new Date();
        assertEquals(WithdrawPolicy.RESTORE_PERIOD_DAYS,
                WithdrawPolicy.calcDaysLeft(now));
        assertTrue(WithdrawPolicy.buildWithdrawnMessage(now).contains("일 남음"));

        long eightDays = 8L * 24 * 60 * 60 * 1000;
        Date expired = new Date(System.currentTimeMillis() - eightDays);
        assertEquals(0, WithdrawPolicy.calcDaysLeft(expired));
        assertEquals(
                "탈퇴한 계정입니다. 복구 가능 기한이 지났습니다.",
                WithdrawPolicy.buildWithdrawnMessage(expired));
    }

    @Test
    void socialSessionSupportShouldResolveDisplayNameInPriorityOrder() {
        MemberSocialJoinVO member = new MemberSocialJoinVO();
        member.setNickname("닉네임");
        member.setMemberName("이름");

        assertEquals("닉네임",
                SocialLoginSessionSupport.resolveDisplayName(
                        member,
                        true,
                        "기본값"));
        assertEquals("이름",
                SocialLoginSessionSupport.resolveDisplayName(
                        member,
                        false,
                        "기본값"));

        member.setNickname(" ");
        assertEquals("이름",
                SocialLoginSessionSupport.resolveDisplayName(
                        member,
                        true,
                        "기본값"));

        member.setMemberName(null);
        assertEquals("기본값",
                SocialLoginSessionSupport.resolveDisplayName(
                        member,
                        true,
                        "기본값"));
    }

    @Test
    void socialSessionSupportShouldSavePendingAndLoginAttributes() {
        MemberSocialJoinVO pending = createSocialMember();

        SocialLoginSessionSupport.savePendingSession(
                session,
                pending,
                "표시명");

        verify(session).setAttribute("pendingMemberNo", 10L);
        verify(session).setAttribute("pendingMemberId", "social-id");
        verify(session).setAttribute("pendingProvider", "google");
        verify(session).setAttribute("pendingDisplayName", "표시명");

        MemberVO loginMember = createLoginMember();
        loginMember.setAdultVerified(null);

        SocialLoginSessionSupport.saveLoginSession(
                session,
                loginMember,
                "google",
                "로그인표시명");

        verify(session).setAttribute("loginMember", loginMember);
        verify(session).setAttribute("memberNo", 10L);
        verify(session).setAttribute("loginProvider", "google");
        verify(session).setAttribute("loginDisplayName", "로그인표시명");
        verify(session).setAttribute("ADULT_VERIFIED", "N");
    }

    @Test
    void socialSessionSupportShouldSaveRestoreSession() {
        SocialLoginSessionSupport.saveRestoreSession(
                request,
                session,
                10L,
                "naver");

        verify(request).changeSessionId();
        verify(session).setAttribute("restoreMemberNo", 10L);
        verify(session).setAttribute("restoreProvider", "naver");
    }

    @Test
    void callbackSupportShouldRejectMissingOrInvalidMembers() {
        LoginOptions options = createOptions();

        SocialLoginResultVO missingResult = new SocialLoginResultVO();
        assertEquals(
                "redirect:/member/login",
                callbackSupport.completeLogin(
                        missingResult,
                        request,
                        session,
                        redirectAttributes,
                        options));
        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "회원 정보가 없습니다.");

        MemberSocialJoinVO invalidMember = createSocialMember();
        invalidMember.setMemberNo(0L);
        SocialLoginResultVO invalidResult = new SocialLoginResultVO();
        invalidResult.setMember(invalidMember);

        assertEquals(
                "redirect:/member/login",
                callbackSupport.completeLogin(
                        invalidResult,
                        request,
                        session,
                        redirectAttributes,
                        options));
        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "회원 번호가 올바르지 않습니다.");
    }

    @Test
    void callbackSupportShouldSendNewMemberToPlatformSelection() {
        MemberSocialJoinVO member = createSocialMember();
        SocialLoginResultVO result = new SocialLoginResultVO();
        result.setMember(member);
        result.setNewMember(true);

        assertEquals(
                "redirect:/member/platform/select",
                callbackSupport.completeLogin(
                        result,
                        request,
                        session,
                        redirectAttributes,
                        createOptions()));

        verify(request).changeSessionId();
        verify(session).setAttribute("pendingMemberNo", 10L);
        verify(memberService, never()).getMemberByNo(10L);
    }

    @Test
    void callbackSupportShouldRequireStoredMemberForExistingAccount() {
        MemberSocialJoinVO member = createSocialMember();
        SocialLoginResultVO result = new SocialLoginResultVO();
        result.setMember(member);
        result.setNewMember(false);

        when(memberPlatformService.countMemberPlatform(10L)).thenReturn(1);
        when(memberService.getMemberByNo(10L)).thenReturn(null);

        assertEquals(
                "redirect:/member/login",
                callbackSupport.completeLogin(
                        result,
                        request,
                        session,
                        redirectAttributes,
                        createOptions()));

        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "저장된 회원 정보가 없습니다.");
    }

    @Test
    void callbackSupportShouldCompleteExistingMemberLogin() {
        MemberSocialJoinVO member = createSocialMember();
        member.setMemberName(" ");
        member.setNickname("소셜닉네임");

        SocialLoginResultVO result = new SocialLoginResultVO();
        result.setMember(member);
        result.setNewMember(false);

        MemberVO storedMember = createLoginMember();
        storedMember.setMemberName(" ");

        when(memberPlatformService.countMemberPlatform(10L)).thenReturn(2);
        when(memberService.getMemberByNo(10L)).thenReturn(storedMember);

        LoginOptions options = new LoginOptions(
                true,
                "기본이름",
                true,
                "회원 정보가 없습니다.",
                "회원 번호가 올바르지 않습니다.",
                "저장된 회원 정보가 없습니다.");

        assertEquals(
                "redirect:/",
                callbackSupport.completeLogin(
                        result,
                        request,
                        session,
                        redirectAttributes,
                        options));

        assertEquals("소셜닉네임", member.getMemberName());
        assertEquals("소셜닉네임", storedMember.getMemberName());
        verify(session).setAttribute("loginMember", storedMember);
        verify(session).setAttribute("loginProvider", "google");
    }

    @Test
    void mainContentPlatformServiceShouldFilterAndPreservePlatforms() {
        MainContentPlatformService service = new MainContentPlatformService();
        SearchResultVO content = new SearchResultVO();
        OttPlatformVO netflix = createPlatform("Netflix");
        OttPlatformVO tving = createPlatform("TVING");
        OttPlatformVO unsupported = createPlatform("Unknown");
        content.setPlatformList(List.of(netflix, tving, unsupported));

        service.attachPlatformLogos(
                List.of(content),
                Arrays.asList("넷플릭스", null, " "));

        assertEquals(1, content.getPlatformList().size());
        assertSame(netflix, content.getPlatformList().get(0));

        content.setPlatformList(List.of(netflix, tving));
        service.attachPlatformLogos(List.of(content), List.of());
        assertEquals(2, content.getPlatformList().size());

        content.setPlatformList(null);
        service.attachPlatformLogos(List.of(content), List.of("Netflix"));
        assertTrue(content.getPlatformList().isEmpty());

        service.attachPlatformLogos(null, List.of("Netflix"));
        service.attachPlatformLogos(List.of(), List.of("Netflix"));
        service.attachPlatformLogos(Arrays.asList((SearchResultVO) null), null);
    }

    @Test
    void resultConstantsAndMemberExceptionsShouldExposeValues() {
        assertEquals(1, ChatResult.SUCCESS);
        assertEquals(0, ChatResult.FAIL);
        assertEquals(2, ChatResult.ALREADY_JOINED);
        assertEquals(3, ChatResult.ROOM_FULL);
        assertEquals(4, ChatResult.ROOM_NOT_FOUND);

        MemberBlockedException blocked = new MemberBlockedException("차단");
        assertEquals("차단", blocked.getMessage());

        Date withdrawnAt = new Date();
        MemberWithdrawnException withdrawn = new MemberWithdrawnException(
                "탈퇴",
                10L,
                withdrawnAt);
        assertEquals("탈퇴", withdrawn.getMessage());
        assertEquals(10L, withdrawn.getMemberNo());
        assertSame(withdrawnAt, withdrawn.getWithdrawnAt());
        assertFalse(withdrawn.getWithdrawnAt().after(new Date()));
    }

    private LoginOptions createOptions() {
        return new LoginOptions(
                true,
                "기본이름",
                false,
                "회원 정보가 없습니다.",
                "회원 번호가 올바르지 않습니다.",
                "저장된 회원 정보가 없습니다.");
    }

    private MemberSocialJoinVO createSocialMember() {
        MemberSocialJoinVO member = new MemberSocialJoinVO();
        member.setMemberNo(10L);
        member.setMemberId("social-id");
        member.setMemberName("소셜이름");
        member.setNickname("소셜닉네임");
        member.setRole("USER");
        member.setProvider("google");
        member.setProfileImage("profile.png");
        return member;
    }

    private MemberVO createLoginMember() {
        MemberVO member = new MemberVO();
        member.setMemberNo(10L);
        member.setMemberId("member-id");
        member.setMemberName("회원이름");
        member.setNickname("회원닉네임");
        member.setRole("USER");
        member.setProfileImage("profile.png");
        member.setAdultVerified("Y");
        return member;
    }

    private OttPlatformVO createPlatform(String platformName) {
        OttPlatformVO platform = new OttPlatformVO();
        platform.setPlatformName(platformName);
        return platform;
    }
}
