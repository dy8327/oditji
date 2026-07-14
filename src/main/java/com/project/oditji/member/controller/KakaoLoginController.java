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

        if (result == null || result.getMember() == null) {
            return "redirect:/member/login";
        }

        MemberSocialJoinVO member = result.getMember();

        if (member.getMemberNo() <= 0) {
            return "redirect:/member/login";
        }

        String displayName = getDisplayName(member);
        member.setMemberName(displayName);

        int platformCount = memberPlatformService.countMemberPlatform(member.getMemberNo());

        /*
         * 신규 회원 또는 OTT 미선택 회원
         * 이 단계에서는 아직 완전 로그인 처리하지 않고 플랫폼 선택 화면으로 보냄.
         */
        if (result.isNewMember() || platformCount == 0) {

            session.setAttribute("pendingMemberNo", member.getMemberNo());
            session.setAttribute("pendingMemberId", member.getMemberId());
            session.setAttribute("pendingMemberName", member.getMemberName());
            session.setAttribute("pendingNickname", member.getNickname());
            session.setAttribute("pendingRole", member.getRole());
            session.setAttribute("pendingProvider", member.getProvider());
            session.setAttribute("pendingDisplayName", displayName);
            session.setAttribute("pendingProfileImage", member.getProfileImage());

            return "redirect:/member/platform/select";
        }

        /*
         * 기존 카카오 회원이고 OTT 선택 기록도 있으면 로그인 완료 처리.
         * loginMember 세션에는 반드시 MemberVO만 저장해야 함.
         */
        MemberVO loginMember = memberService.getMemberByNo(member.getMemberNo());

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        if (loginMember.getMemberName() == null
                || loginMember.getMemberName().isBlank()) {
            loginMember.setMemberName(displayName);
        }

        saveLoginSession(session, loginMember, member.getProvider(), displayName);

        System.out.println("카카오 로그인 세션 저장 확인");
        System.out.println("loginMemberNo = " + session.getAttribute("loginMemberNo"));
        System.out.println("loginMemberId = " + session.getAttribute("loginMemberId"));
        System.out.println("loginMemberName = " + session.getAttribute("loginMemberName"));
        System.out.println("loginNickname = " + session.getAttribute("loginNickname"));
        System.out.println("loginProvider = " + session.getAttribute("loginProvider"));
        System.out.println("loginDisplayName = " + session.getAttribute("loginDisplayName"));
        System.out.println("profileImage = " + session.getAttribute("profileImage"));

        return "redirect:/";
    }

    private void saveLoginSession(
            HttpSession session,
            MemberVO loginMember,
            String provider,
            String displayName) {

        /*
         * 핵심:
         * loginMember에는 반드시 MemberVO 저장
         */
        session.setAttribute("loginMember", loginMember);

        /*
         * 일반 로그인 쪽에서 쓰는 세션명도 같이 저장
         */
        session.setAttribute("memberNo", loginMember.getMemberNo());
        session.setAttribute("memberId", loginMember.getMemberId());
        session.setAttribute("memberName", loginMember.getMemberName());
        session.setAttribute("nickname", loginMember.getNickname());
        session.setAttribute("role", loginMember.getRole());
        session.setAttribute("profileImage", loginMember.getProfileImage());

        /*
         * 카카오 로그인 쪽에서 기존에 쓰던 세션명도 유지
         */
        session.setAttribute("loginMemberNo", loginMember.getMemberNo());
        session.setAttribute("loginMemberId", loginMember.getMemberId());
        session.setAttribute("loginMemberName", loginMember.getMemberName());
        session.setAttribute("loginNickname", loginMember.getNickname());
        session.setAttribute("loginRole", loginMember.getRole());
        session.setAttribute("loginProvider", provider);
        session.setAttribute("loginDisplayName", displayName);

        String adultVerified = loginMember.getAdultVerified();

        if (adultVerified == null || adultVerified.isBlank()) {
            adultVerified = "N";
        }

        session.setAttribute("ADULT_VERIFIED", adultVerified);
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