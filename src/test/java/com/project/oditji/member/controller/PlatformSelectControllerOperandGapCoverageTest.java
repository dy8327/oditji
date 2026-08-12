package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpSession;

/** OTT 선택의 이메일/회원명 OR 조건에서 남은 개별 피연산자를 보완합니다. */
class PlatformSelectControllerOperandGapCoverageTest {

    @Test
    void saveShouldTreatNullEmailAsMissingForExistingSessionMember() {
        MemberPlatformService platformService = mock(MemberPlatformService.class);
        MemberService memberService = mock(MemberService.class);
        HttpSession session = mock(HttpSession.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        PlatformSelectController controller = new PlatformSelectController(
                platformService,
                memberService);

        when(session.getAttribute("pendingMemberNo")).thenReturn(null);
        when(session.getAttribute("loginMemberNo")).thenReturn(80L);

        MemberVO currentMember = new MemberVO();
        currentMember.setMemberNo(80L);
        currentMember.setEmail(null);
        when(memberService.getMemberByNo(80L)).thenReturn(currentMember);

        assertEquals(
                "redirect:/",
                controller.savePlatform(
                        null,
                        "Y",
                        "social80@test.com",
                        session,
                        redirectAttributes));

        verify(memberService).updateSnsMemberEmail(80L, "social80@test.com");
        verify(platformService).saveMemberPlatforms(80L, null, "Y");
    }

    @Test
    void pendingLoginShouldUseDisplayNameWhenMemberNameIsNull() {
        MemberPlatformService platformService = mock(MemberPlatformService.class);
        MemberService memberService = mock(MemberService.class);
        HttpSession session = mock(HttpSession.class);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
        PlatformSelectController controller = new PlatformSelectController(
                platformService,
                memberService);

        when(session.getAttribute("pendingMemberNo")).thenReturn(81L);
        when(session.getAttribute("loginMemberNo")).thenReturn(null);
        when(session.getAttribute("pendingDisplayName")).thenReturn("네이버 표시명");
        when(session.getAttribute("pendingProvider")).thenReturn("NAVER");

        MemberVO currentMember = member(81L, "saved@test.com", "기존");
        MemberVO loginMember = member(81L, "saved@test.com", null);
        loginMember.setMemberId("social-81");
        loginMember.setNickname("닉네임");
        loginMember.setRole("USER");

        when(memberService.getMemberByNo(81L))
                .thenReturn(currentMember, loginMember);

        assertEquals(
                "redirect:/",
                controller.savePlatform(
                        List.of(1L),
                        null,
                        null,
                        session,
                        redirectAttributes));

        assertEquals("네이버 표시명", loginMember.getMemberName());
        verify(memberService, never()).updateSnsMemberEmail(81L, null);
        verify(platformService).saveMemberPlatforms(81L, List.of(1L), null);
    }

    private MemberVO member(Long memberNo, String email, String memberName) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        member.setEmail(email);
        member.setMemberName(memberName);
        return member;
    }
}
