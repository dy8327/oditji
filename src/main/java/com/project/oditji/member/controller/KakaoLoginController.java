package com.project.oditji.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.service.KakaoLoginService;
import com.project.oditji.member.support.SocialLoginCallbackSupport;
import com.project.oditji.member.support.SocialLoginSessionSupport;
import com.project.oditji.member.support.WithdrawPolicy;
import com.project.oditji.member.vo.SocialLoginResultVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class KakaoLoginController {

    private static final String REDIRECT_MEMBER_LOGIN = "redirect:/member/login";

    private final KakaoLoginService kakaoLoginService;
    private final SocialLoginCallbackSupport socialLoginCallbackSupport;

    public KakaoLoginController(
            KakaoLoginService kakaoLoginService,
            SocialLoginCallbackSupport socialLoginCallbackSupport) {

        this.kakaoLoginService = kakaoLoginService;
        this.socialLoginCallbackSupport = socialLoginCallbackSupport;
    }

    @GetMapping("/member/kakao/login")
    public String kakaoLogin() {
        return "redirect:" + kakaoLoginService.getKakaoLoginUrl();
    }

    @GetMapping("/member/kakao/callback")
    public String kakaoCallback(
            @RequestParam("code") String code,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            SocialLoginResultVO result = kakaoLoginService.kakaoLogin(code);

            return socialLoginCallbackSupport.completeLogin(
                    result,
                    request,
                    session,
                    redirectAttributes,
                    false,
                    "회원",
                    true,
                    null,
                    null,
                    null
            );

        } catch (MemberBlockedException e) {
            redirectAttributes.addFlashAttribute("blockedMessage", e.getMessage());
            return REDIRECT_MEMBER_LOGIN;

        } catch (MemberWithdrawnException e) {
            SocialLoginSessionSupport.saveRestoreSession(
                    request,
                    session,
                    e.getMemberNo(),
                    "KAKAO"
            );
            redirectAttributes.addFlashAttribute(
                    "withdrawnMessage",
                    WithdrawPolicy.buildWithdrawnMessage(e.getWithdrawnAt())
            );
            return REDIRECT_MEMBER_LOGIN;

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "카카오 로그인 처리 중 오류가 발생했습니다."
            );
            return REDIRECT_MEMBER_LOGIN;
        }
    }
}
