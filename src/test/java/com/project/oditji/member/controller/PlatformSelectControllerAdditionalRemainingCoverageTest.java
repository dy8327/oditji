package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import jakarta.servlet.http.HttpSession;

/**
 * OTT 선택 컨트롤러의 이메일 null/blank 및 pending 로그인 이름 유지 분기를 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class PlatformSelectControllerAdditionalRemainingCoverageTest {

    @Mock
    private MemberPlatformService memberPlatformService;

    @Mock
    private MemberService memberService;

    @Mock
    private HttpSession session;

    @Mock
    private RedirectAttributes redirectAttributes;

    private final Map<String, Object> sessionValues =
            new HashMap<String, Object>();

    private PlatformSelectController controller;

    @BeforeEach
    void setUp() {
        controller = new PlatformSelectController(
                memberPlatformService,
                memberService);

        lenient().when(session.getAttribute(anyString()))
                .thenAnswer(invocation ->
                        sessionValues.get(invocation.getArgument(0)));

        lenient().doAnswer(invocation -> {
            sessionValues.put(
                    invocation.getArgument(0),
                    invocation.getArgument(1));
            return null;
        }).when(session).setAttribute(
                anyString(),
                org.mockito.ArgumentMatchers.any());

        lenient().doAnswer(invocation -> {
            sessionValues.remove(invocation.getArgument(0));
            return null;
        }).when(session).removeAttribute(anyString());
    }

    @Test
    void formShouldRequestEmailForNullAndBlankEmailMembers() {
        sessionValues.put("loginMemberNo", 10L);

        MemberVO nullEmail = member(10L, null);
        when(memberService.getMemberByNo(10L))
                .thenReturn(nullEmail);

        ExtendedModelMap nullModel = new ExtendedModelMap();

        assertEquals(
                "member/selectOtt",
                controller.selectPlatformForm(
                        session,
                        nullModel));
        assertEquals(
                Boolean.TRUE,
                nullModel.get("needEmailInput"));

        MemberVO blankEmail = member(10L, "   ");
        when(memberService.getMemberByNo(10L))
                .thenReturn(blankEmail);

        ExtendedModelMap blankModel = new ExtendedModelMap();

        controller.selectPlatformForm(
                session,
                blankModel);

        assertEquals(
                Boolean.TRUE,
                blankModel.get("needEmailInput"));
    }

    @Test
    void saveShouldUpdateEmailWhenCurrentMemberIsNull() {
        sessionValues.put("loginMemberNo", 20L);

        when(memberService.getMemberByNo(20L))
                .thenReturn(null);

        assertEquals(
                "redirect:/",
                controller.savePlatform(
                        null,
                        "Y",
                        "social@test.com",
                        session,
                        redirectAttributes));

        verify(memberService)
                .updateSnsMemberEmail(
                        20L,
                        "social@test.com");
        verify(memberPlatformService)
                .saveMemberPlatforms(
                        20L,
                        null,
                        "Y");
    }

    @Test
    void pendingLoginShouldKeepExistingMemberName() {
        sessionValues.put("pendingMemberNo", 30L);
        sessionValues.put("pendingDisplayName", "표시명");

        MemberVO current = member(30L, "saved@test.com");
        MemberVO loginMember = member(30L, "saved@test.com");
        loginMember.setMemberId("social-30");
        loginMember.setMemberName("기존 이름");
        loginMember.setNickname("닉네임");
        loginMember.setRole("USER");

        when(memberService.getMemberByNo(30L))
                .thenReturn(current, loginMember);

        assertEquals(
                "redirect:/",
                controller.savePlatform(
                        List.of(1L),
                        null,
                        "ignored@test.com",
                        session,
                        redirectAttributes));

        assertEquals(
                "기존 이름",
                loginMember.getMemberName());
        assertEquals(
                "표시명",
                sessionValues.get("loginDisplayName"));
        assertFalse(
                sessionValues.containsKey("pendingMemberNo"));

        verify(memberService, never())
                .updateSnsMemberEmail(
                        30L,
                        "ignored@test.com");
    }

    private MemberVO member(
            Long memberNo,
            String email) {

        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        member.setEmail(email);
        return member;
    }
}
