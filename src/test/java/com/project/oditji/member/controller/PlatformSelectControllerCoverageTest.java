package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.PlatformVO;

import jakarta.servlet.http.HttpSession;

/** OTT 선택 화면의 세션 분기, 이메일 저장과 신규 소셜 회원 세션 생성을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class PlatformSelectControllerCoverageTest {

    @Mock
    private MemberPlatformService memberPlatformService;

    @Mock
    private MemberService memberService;

    @Mock
    private HttpSession session;

    @Mock
    private RedirectAttributes redirectAttributes;

    private final Map<String, Object> sessionValues = new HashMap<String, Object>();

    private PlatformSelectController controller;

    @BeforeEach
    void setUp() {
        controller = new PlatformSelectController(memberPlatformService, memberService);

        lenient().when(session.getAttribute(anyString()))
                .thenAnswer(invocation -> sessionValues.get(invocation.getArgument(0)));
        lenient().doAnswer(invocation -> {
            sessionValues.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(session).setAttribute(anyString(), org.mockito.ArgumentMatchers.any());
        lenient().doAnswer(invocation -> {
            sessionValues.remove(invocation.getArgument(0));
            return null;
        }).when(session).removeAttribute(anyString());
    }

    @Test
    void formShouldRedirectWhenNoMemberSessionExists() {
        assertEquals(
                "redirect:/member/login",
                controller.selectPlatformForm(session, new ExtendedModelMap()));
    }

    @Test
    void formShouldUseIntegerLoginMemberAndExposePlatforms() {
        sessionValues.put("loginMemberNo", Integer.valueOf(12));
        MemberVO member = member(12L, "member@test.com");
        List<PlatformVO> platforms = List.of(new PlatformVO());
        when(memberService.getMemberByNo(12L)).thenReturn(member);
        when(memberPlatformService.findPlatformList()).thenReturn(platforms);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.selectPlatformForm(session, model);

        assertEquals("member/selectOtt", view);
        assertEquals(platforms, model.get("platformList"));
        assertEquals(Boolean.FALSE, model.get("needEmailInput"));
    }

    @Test
    void formShouldPreferPendingMemberAndRequestMissingEmail() {
        sessionValues.put("pendingMemberNo", "21");
        sessionValues.put("loginMemberNo", 99L);
        when(memberService.getMemberByNo(21L)).thenReturn(null);
        when(memberPlatformService.findPlatformList()).thenReturn(new ArrayList<PlatformVO>());
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("member/selectOtt", controller.selectPlatformForm(session, model));
        assertEquals(Boolean.TRUE, model.get("needEmailInput"));
    }

    @Test
    void saveShouldRedirectWhenMemberSessionIsMissing() {
        assertEquals(
                "redirect:/member/login",
                controller.savePlatform(List.of(1L), null, null, session, redirectAttributes));
    }

    @Test
    void existingMemberShouldSavePlatformsWithoutChangingEmail() {
        sessionValues.put("loginMemberNo", 30L);
        MemberVO current = member(30L, "saved@test.com");
        when(memberService.getMemberByNo(30L)).thenReturn(current);

        String result = controller.savePlatform(
                List.of(1L, 2L),
                null,
                "ignored@test.com",
                session,
                redirectAttributes);

        assertEquals("redirect:/", result);
        verify(memberService, never()).updateSnsMemberEmail(30L, "ignored@test.com");
        verify(memberPlatformService).saveMemberPlatforms(30L, List.of(1L, 2L), null);
        verify(redirectAttributes).addFlashAttribute("successMessage", "OTT 정보가 저장되었습니다.");
    }

    @Test
    void pendingMemberShouldSaveEmailAndCreateCompleteLoginSession() {
        sessionValues.put("pendingMemberNo", 40L);
        sessionValues.put("pendingDisplayName", "표시 이름");
        sessionValues.put("pendingProvider", "KAKAO");

        MemberVO current = member(40L, " ");
        MemberVO loginMember = member(40L, "new@test.com");
        loginMember.setMemberId("social-40");
        loginMember.setMemberName(" ");
        loginMember.setNickname("닉네임");
        loginMember.setRole("USER");

        when(memberService.getMemberByNo(40L)).thenReturn(current, loginMember);

        String result = controller.savePlatform(
                null,
                "Y",
                "new@test.com",
                session,
                redirectAttributes);

        assertEquals("redirect:/", result);
        verify(memberService).updateSnsMemberEmail(40L, "new@test.com");
        verify(memberPlatformService).saveMemberPlatforms(40L, null, "Y");
        assertEquals("표시 이름", loginMember.getMemberName());
        assertEquals(loginMember, sessionValues.get("loginMember"));
        assertEquals(40L, sessionValues.get("loginMemberNo"));
        assertEquals(40L, sessionValues.get("memberNo"));
        assertEquals("KAKAO", sessionValues.get("loginProvider"));
        assertFalse(sessionValues.containsKey("pendingMemberNo"));
        assertFalse(sessionValues.containsKey("pendingDisplayName"));
    }

    @Test
    void illegalArgumentShouldReturnSelectionPageWithOriginalMessage() {
        sessionValues.put("loginMemberNo", 50L);
        when(memberService.getMemberByNo(50L)).thenReturn(member(50L, "member@test.com"));
        doThrow(new IllegalArgumentException("OTT 선택이 올바르지 않습니다."))
                .when(memberPlatformService)
                .saveMemberPlatforms(50L, null, null);

        String result = controller.savePlatform(null, null, null, session, redirectAttributes);

        assertEquals("redirect:/member/platform/select", result);
        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "OTT 선택이 올바르지 않습니다.");
    }

    @Test
    void unexpectedFailureShouldReturnGenericMessage() {
        sessionValues.put("loginMemberNo", 60L);
        when(memberService.getMemberByNo(60L)).thenThrow(new IllegalStateException("db"));

        String result = controller.savePlatform(null, "Y", null, session, redirectAttributes);

        assertEquals("redirect:/member/platform/select", result);
        verify(redirectAttributes).addFlashAttribute(
                "errorMessage",
                "OTT 정보 저장 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        assertTrue(sessionValues.containsKey("loginMemberNo"));
    }

    private MemberVO member(Long memberNo, String email) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        member.setEmail(email);
        return member;
    }
}
