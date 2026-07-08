package com.project.oditji.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.oditji.member.service.KakaoLoginService;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.vo.KakaoLoginResultVO;
import com.project.oditji.member.vo.MemberSocialJoinVO;

import jakarta.servlet.http.HttpSession;

@Controller
public class KakaoLoginController {

    private final KakaoLoginService kakaoLoginService;
    private final MemberPlatformService memberPlatformService;

    public KakaoLoginController(KakaoLoginService kakaoLoginService, MemberPlatformService memberPlatformService) {
        this.kakaoLoginService = kakaoLoginService;
        this.memberPlatformService = memberPlatformService;
    }

    @GetMapping("/member/kakao/login")
    public String kakaoLogin() {
        return "redirect:" + kakaoLoginService.getKakaoLoginUrl();
    }

    @GetMapping("/member/kakao/callback")
    public String kakaoCallback(@RequestParam("code") String code,
                                HttpSession session) {

        KakaoLoginResultVO result = kakaoLoginService.kakaoLogin(code);
        MemberSocialJoinVO member = result.getMember();

        String displayName = getDisplayName(member);
        
        /*
         * header.jsp가 ${sessionScope.loginMember.memberName}을 사용하므로
         * DB의 MEMBER_NAME이 NULL이어도 세션 객체에는 화면 표시용 이름을 넣어준다.
         */
        member.setMemberName(displayName);

         int platformCount = memberPlatformService.countMemberPlatform(member.getMemberNo());

        // 신규 카카오 회원이면 바로 로그인 완료 X
        // OTT 플랫폼 선택 페이지로 이동
        if (result.isNewMember() || platformCount == 0) {
            session.setAttribute("pendingMemberNo", member.getMemberNo());
            session.setAttribute("pendingMemberId", member.getMemberId());
            session.setAttribute("pendingMemberName", member.getMemberName());
            session.setAttribute("pendingNickname", member.getNickname());
            session.setAttribute("pendingRole", member.getRole());
            session.setAttribute("pendingProvider", member.getProvider());
            session.setAttribute("pendingDisplayName", displayName);

            // OTT 선택 완료 후 loginMember로 옮기기 위해 임시 객체 저장
            session.setAttribute("pendingLoginMember", member);

            return "redirect:/member/platform/select";
        }

        // 기존 카카오 회원이고 OTT 플랫폼 선택 기록도 있으면 바로 로그인 완료 처리
        session.setAttribute("loginMember", member);
        session.setAttribute("loginMemberNo", member.getMemberNo());
        session.setAttribute("loginMemberId", member.getMemberId());
        session.setAttribute("loginMemberName", member.getMemberName());
        session.setAttribute("loginNickname", member.getNickname());
        session.setAttribute("loginRole", member.getRole());
        session.setAttribute("loginProvider", member.getProvider());
        session.setAttribute("loginDisplayName", displayName);

        System.out.println("카카오 로그인 세션 저장 확인");
        System.out.println("loginMemberNo = " + session.getAttribute("loginMemberNo"));
        System.out.println("loginMemberId = " + session.getAttribute("loginMemberId"));
        System.out.println("loginMemberName = " + session.getAttribute("loginMemberName"));
        System.out.println("loginNickname = " + session.getAttribute("loginNickname"));
        System.out.println("loginProvider = " + session.getAttribute("loginProvider"));
        System.out.println("loginDisplayName = " + session.getAttribute("loginDisplayName"));

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