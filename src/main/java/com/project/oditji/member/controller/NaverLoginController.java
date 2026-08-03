package com.project.oditji.member.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.service.NaverLoginService;
import com.project.oditji.member.support.SocialLoginCallbackSupport;
import com.project.oditji.member.support.SocialLoginSessionSupport;
import com.project.oditji.member.support.WithdrawPolicy;
import com.project.oditji.member.vo.SocialLoginResultVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 네이버 로그인 시작 및 콜백 처리를 담당합니다.
 */
@Controller
public class NaverLoginController {

    private static final String NAVER_OAUTH_STATE = "naverOAuthState";
    private static final String PROVIDER_NAVER = "NAVER";
    private static final String ATTRIBUTE_ERROR_MESSAGE = "errorMessage";
    private static final String REDIRECT_MEMBER_LOGIN = "redirect:/member/login";
    private static final Logger log = LoggerFactory.getLogger(NaverLoginController.class);

    private final NaverLoginService naverLoginService;
    private final SocialLoginCallbackSupport socialLoginCallbackSupport;

    public NaverLoginController(
            NaverLoginService naverLoginService,
            SocialLoginCallbackSupport socialLoginCallbackSupport) {

        this.naverLoginService = naverLoginService;
        this.socialLoginCallbackSupport = socialLoginCallbackSupport;
    }

    @GetMapping("/member/naver/login")
    public String naverLogin(HttpSession session) {
        String state = naverLoginService.createState();
        session.setAttribute(NAVER_OAUTH_STATE, state);

        return "redirect:" + naverLoginService.getNaverLoginUrl(state);
    }

    @GetMapping("/member/naver/callback")
    public String naverCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String savedState = (String) session.getAttribute(NAVER_OAUTH_STATE);
        session.removeAttribute(NAVER_OAUTH_STATE);

        String validationMessage = validateCallback(
                savedState,
                state,
                error,
                errorDescription,
                code
        );
        if (validationMessage != null) {
            redirectAttributes.addFlashAttribute(ATTRIBUTE_ERROR_MESSAGE, validationMessage);
            return REDIRECT_MEMBER_LOGIN;
        }

        try {
            SocialLoginResultVO result = naverLoginService.naverLogin(code, state);

            return socialLoginCallbackSupport.completeLogin(
                    result,
                    request,
                    session,
                    redirectAttributes,
                    false,
                    "회원",
                    true,
                    "네이버 로그인 회원 정보를 확인하지 못했습니다.",
                    "네이버 로그인 회원 번호를 확인하지 못했습니다.",
                    "네이버 로그인 회원 정보를 불러오지 못했습니다."
            );

        } catch (MemberBlockedException e) {
            redirectAttributes.addFlashAttribute("blockedMessage", e.getMessage());
            return REDIRECT_MEMBER_LOGIN;

        } catch (MemberWithdrawnException e) {
            SocialLoginSessionSupport.saveRestoreSession(
                    request,
                    session,
                    e.getMemberNo(),
                    PROVIDER_NAVER
            );
            redirectAttributes.addFlashAttribute(
                    "withdrawnMessage",
                    WithdrawPolicy.buildWithdrawnMessage(e.getWithdrawnAt())
            );
            return REDIRECT_MEMBER_LOGIN;

        } catch (IllegalArgumentException | IllegalStateException e) {
            if (log.isWarnEnabled()) {
                log.warn("네이버 로그인 처리 실패", e);
            }
            redirectAttributes.addFlashAttribute(
                    ATTRIBUTE_ERROR_MESSAGE,
                    "네이버 로그인 처리 중 오류가 발생했습니다."
            );
            return REDIRECT_MEMBER_LOGIN;
        }
    }

    private String validateCallback(
            String savedState,
            String state,
            String error,
            String errorDescription,
            String code) {

        if (savedState == null || state == null || !savedState.equals(state)) {
            return "네이버 로그인 요청 검증에 실패했습니다. 다시 시도해주세요.";
        }

        if (error != null && !error.isBlank()) {
            return buildCallbackErrorMessage(errorDescription);
        }

        if (code == null || code.isBlank()) {
            return "네이버 인증 코드를 받지 못했습니다. 다시 시도해주세요.";
        }

        return null;
    }

    private String buildCallbackErrorMessage(String errorDescription) {
        if (errorDescription != null && !errorDescription.isBlank() && log.isWarnEnabled()) {
            log.warn("네이버 OAuth 콜백 오류: {}", errorDescription);
        }
        return "네이버 로그인이 취소되었거나 실패했습니다.";
    }
}
