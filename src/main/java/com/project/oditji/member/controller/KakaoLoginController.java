package com.project.oditji.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.oditji.member.service.KakaoLoginService;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.vo.KakaoLoginResultVO;
import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpSession;

@Controller
public class KakaoLoginController {

    private final KakaoLoginService kakaoLoginService;
    private final MemberPlatformService memberPlatformService;
    private final MemberService memberService;

    public KakaoLoginController(
            KakaoLoginService kakaoLoginService,
            MemberPlatformService memberPlatformService,
            MemberService memberService) {

        this.kakaoLoginService = kakaoLoginService;
        this.memberPlatformService = memberPlatformService;
        this.memberService = memberService;
    }

    @GetMapping("/member/kakao/login")
    public String kakaoLogin() {
        return "redirect:" + kakaoLoginService.getKakaoLoginUrl();
    }

    @GetMapping("/member/kakao/callback")
    public String kakaoCallback(
            @RequestParam("code") String code,
            HttpSession session) {

        KakaoLoginResultVO result = kakaoLoginService.kakaoLogin(code);
        MemberSocialJoinVO member = result.getMember();

        String displayName = getDisplayName(member);
        member.setMemberName(displayName);

        int platformCount =
                memberPlatformService.countMemberPlatform(member.getMemberNo());

        // 신규 회원 또는 OTT 미선택
        if (result.isNewMember() || platformCount == 0) {

            session.setAttribute("pendingMemberNo", member.getMemberNo());
            session.setAttribute("pendingMemberId", member.getMemberId());
            session.setAttribute("pendingMemberName", member.getMemberName());
            session.setAttribute("pendingNickname", member.getNickname());
            session.setAttribute("pendingRole", member.getRole());
            session.setAttribute("pendingProvider", member.getProvider());
            session.setAttribute("pendingDisplayName", displayName);

            return "redirect:/member/platform/select";
        }

        // 로그인 세션은 항상 MemberVO로 통일
        MemberVO loginMember =
                memberService.getMemberByNo(member.getMemberNo());

        if (loginMember.getMemberName() == null
                || loginMember.getMemberName().isBlank()) {
            loginMember.setMemberName(displayName);
        }

        session.setAttribute("loginMember", loginMember);
        session.setAttribute("loginMemberNo", loginMember.getMemberNo());
        session.setAttribute("loginMemberId", loginMember.getMemberId());
        session.setAttribute("loginMemberName", loginMember.getMemberName());
        session.setAttribute("loginNickname", loginMember.getNickname());
        session.setAttribute("loginRole", loginMember.getRole());
        session.setAttribute("loginProvider", member.getProvider());
        session.setAttribute("loginDisplayName", displayName);

        return "redirect:/";
    }

    private String getDisplayName(MemberSocialJoinVO member) {

        String displayName = member.getMemberName();

        if (displayName == null || displayName.isBlank()) {
            displayName = member.getNickname();
        }

        if (displayName == null || displayName.isBlank()) {
            displayName = "회원";
        }

        return displayName;
    }
}