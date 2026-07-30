package com.project.oditji.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.service.KakaoLoginService;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.support.WithdrawPolicy;
import com.project.oditji.member.vo.KakaoLoginResultVO;
import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.MemberVO;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
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
    public String kakaoCallback(@RequestParam("code") String code,HttpServletRequest request,
        HttpSession session,RedirectAttributes redirectAttributes) {

        try {

            KakaoLoginResultVO result = kakaoLoginService.kakaoLogin(code);
            if (result == null || result.getMember() == null) {
                
                return "redirect:/member/login";
            }

            MemberSocialJoinVO member = result.getMember();
            if (member.getMemberNo() <= 0) {
                
                return "redirect:/member/login";
            }

            request.changeSessionId();

            String displayName = getDisplayName(member);
            member.setMemberName(displayName);

            int platformCount = memberPlatformService.countMemberPlatform(member.getMemberNo());
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

            MemberVO loginMember = memberService.getMemberByNo(member.getMemberNo());
            if (loginMember == null) {
                
                return "redirect:/member/login";
            }

            if (loginMember.getMemberName() == null || loginMember.getMemberName().isBlank()) {
                loginMember.setMemberName(displayName);
            }

            saveLoginSession(session, loginMember, member.getProvider(), displayName);

            return "redirect:/";

        } catch (MemberBlockedException e) {

            /*
             * 정지 회원: 복구 절차 없이 단순 안내만 노출한다.
             */
            redirectAttributes.addFlashAttribute("blockedMessage", e.getMessage());
            return "redirect:/member/login";

        } catch (MemberWithdrawnException e) {

            /*
             * 탈퇴 회원: 카카오 OAuth 인증에 성공한 시점이 곧 본인 확인이므로
             * 별도 비밀번호 확인 없이 세션에 복구 대상 회원번호만 저장해둔다.
             * 로그인 화면(login.jsp)에서 이 값을 신뢰해 /member/restore를 호출한다.
             */

            request.changeSessionId();
            session.setAttribute("restoreMemberNo", e.getMemberNo());
            session.setAttribute("restoreProvider", "KAKAO");

            redirectAttributes.addFlashAttribute("withdrawnMessage", WithdrawPolicy.buildWithdrawnMessage(e.getWithdrawnAt()));

            return "redirect:/member/login";

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "카카오 로그인 처리 중 오류가 발생했습니다.");
            
            return "redirect:/member/login";
        }
    }

    private void saveLoginSession(HttpSession session, MemberVO loginMember, String provider, String displayName) {
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
