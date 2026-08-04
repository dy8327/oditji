package com.project.oditji.member.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.service.GoogleLoginService;
import com.project.oditji.member.support.SocialLoginCallbackSupport;
import com.project.oditji.member.support.SocialLoginCallbackSupport.LoginOptions;
import com.project.oditji.member.support.SocialLoginSessionSupport;
import com.project.oditji.member.support.WithdrawPolicy;
import com.project.oditji.member.vo.SocialLoginResultVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Google 로그인 시작 및 콜백 처리를 담당합니다.
 */
@Controller
public class GoogleLoginController {

    private static final String GOOGLE_OAUTH_STATE = "googleOAuthState";
    private static final String ATTRIBUTE_ERROR_MESSAGE = "errorMessage";
    private static final String REDIRECT_MEMBER_LOGIN = "redirect:/member/login";
    private static final LoginOptions LOGIN_OPTIONS = new LoginOptions(
            true,
            "구글회원",
            false,
            "Google 회원정보를 확인할 수 없습니다.",
            "Google 회원번호를 확인할 수 없습니다.",
            "Google 로그인 회원정보를 불러오지 못했습니다."
    );
    private static final Logger log = LoggerFactory.getLogger(GoogleLoginController.class);

    private final GoogleLoginService googleLoginService;
    private final SocialLoginCallbackSupport socialLoginCallbackSupport;

    public GoogleLoginController(
            GoogleLoginService googleLoginService,
            SocialLoginCallbackSupport socialLoginCallbackSupport) {

        this.googleLoginService = googleLoginService;
        this.socialLoginCallbackSupport = socialLoginCallbackSupport;
    }

    @GetMapping("/member/google/login")
    public String googleLogin(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            String state = UUID.randomUUID().toString();
            session.setAttribute(GOOGLE_OAUTH_STATE, state);

            return "redirect:" + googleLoginService.getGoogleLoginUrl(state);

        } catch (IllegalStateException e) {
            if (log.isWarnEnabled()) {
                log.warn("Google 로그인 준비 실패", e);
            }
            redirectAttributes.addFlashAttribute(
                    ATTRIBUTE_ERROR_MESSAGE,
                    "Google 로그인 준비 중 오류가 발생했습니다."
            );
            return REDIRECT_MEMBER_LOGIN;
        }
    }

    @GetMapping("/member/google/callback")
    public String googleCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String expectedState = (String) session.getAttribute(GOOGLE_OAUTH_STATE);
        session.removeAttribute(GOOGLE_OAUTH_STATE);

        if (error != null && !error.isBlank()) {
            redirectAttributes.addFlashAttribute(
                    ATTRIBUTE_ERROR_MESSAGE,
                    "Google 로그인이 취소되었거나 승인되지 않았습니다."
            );
            return REDIRECT_MEMBER_LOGIN;
        }

        if (expectedState == null || state == null || !expectedState.equals(state)) {
            redirectAttributes.addFlashAttribute(
                    ATTRIBUTE_ERROR_MESSAGE,
                    "Google 로그인 요청 검증에 실패했습니다. 다시 시도해주세요."
            );
            return REDIRECT_MEMBER_LOGIN;
        }

        try {
            SocialLoginResultVO result = googleLoginService.googleLogin(code);

            return socialLoginCallbackSupport.completeLogin(
                    result,
                    request,
                    session,
                    redirectAttributes,
                    LOGIN_OPTIONS
            );

        } catch (MemberBlockedException e) {
            redirectAttributes.addFlashAttribute("blockedMessage", e.getMessage());
            return REDIRECT_MEMBER_LOGIN;

        } catch (MemberWithdrawnException e) {
            SocialLoginSessionSupport.saveRestoreSession(
                    request,
                    session,
                    e.getMemberNo(),
                    "GOOGLE"
            );
            redirectAttributes.addFlashAttribute(
                    "withdrawnMessage",
                    WithdrawPolicy.buildWithdrawnMessage(e.getWithdrawnAt())
            );
            return REDIRECT_MEMBER_LOGIN;

        } catch (IllegalStateException e) {
            if (log.isWarnEnabled()) {
                log.warn("Google 로그인 처리 실패", e);
            }
            redirectAttributes.addFlashAttribute(
                    ATTRIBUTE_ERROR_MESSAGE,
                    "Google 로그인 처리 중 오류가 발생했습니다."
            );
            return REDIRECT_MEMBER_LOGIN;
        }
    }
}
