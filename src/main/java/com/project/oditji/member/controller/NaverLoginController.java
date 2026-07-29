package com.project.oditji.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.service.NaverLoginService;
import com.project.oditji.member.support.WithdrawPolicy;
import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.NaverLoginResultVO;

import jakarta.servlet.http.HttpSession;

/**
 * 기존 login.jsp의 네이버 로그인 버튼 요청을 처리하는 컨트롤러입니다.
 *
 * 화면 파일이나 버튼 구조를 변경하지 않고,
 * /member/naver/login 및 /member/naver/callback 경로만 추가합니다.
 */
@Controller
public class NaverLoginController {

    private static final String NAVER_OAUTH_STATE = "naverOAuthState";
    private static final String PROVIDER_NAVER = "NAVER";

    private final NaverLoginService naverLoginService;
    private final MemberPlatformService memberPlatformService;
    private final MemberService memberService;
    private static final Logger log = LoggerFactory.getLogger(NaverLoginController.class);

    public NaverLoginController(
            NaverLoginService naverLoginService,
            MemberPlatformService memberPlatformService,
            MemberService memberService) {

        this.naverLoginService = naverLoginService;
        this.memberPlatformService = memberPlatformService;
        this.memberService = memberService;
    }

    /**
     * 기존 네이버 로그인 버튼 클릭 시 호출됩니다.
     *
     * 요청별 state 값을 세션에 저장한 뒤 네이버 인증 화면으로 이동합니다.
     */
    @GetMapping("/member/naver/login")
    public String naverLogin(HttpSession session) {
        String state = naverLoginService.createState();
        session.setAttribute(NAVER_OAUTH_STATE, state);

        return "redirect:" + naverLoginService.getNaverLoginUrl(state);
    }

    /**
     * 네이버 인증 완료 후 등록된 Callback URL로 전달되는 요청을 처리합니다.
     */
    @GetMapping("/member/naver/callback")
    public String naverCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            HttpSession session, RedirectAttributes redirectAttributes) {

        String savedState = (String) session.getAttribute(NAVER_OAUTH_STATE);

        /*
         * state는 한 번만 사용할 수 있도록 콜백 진입 즉시 세션에서 제거합니다.
         */
        session.removeAttribute(NAVER_OAUTH_STATE);
        if (savedState == null || state == null || !savedState.equals(state)) {
            redirectAttributes.addFlashAttribute("errorMessage", "네이버 로그인 요청 검증에 실패했습니다. 다시 시도해주세요.");

            return "redirect:/member/login";
        }

        if (error != null && !error.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", buildCallbackErrorMessage(errorDescription));

            return "redirect:/member/login";
        }

        if (code == null || code.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "네이버 인증 코드를 받지 못했습니다. 다시 시도해주세요.");

            return "redirect:/member/login";
        }

        try {
            NaverLoginResultVO result = naverLoginService.naverLogin(code, state);

            if (result == null || result.getMember() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "네이버 로그인 회원 정보를 확인하지 못했습니다.");

                return "redirect:/member/login";
            }

            MemberSocialJoinVO member = result.getMember();
            if (member.getMemberNo() <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "네이버 로그인 회원 번호를 확인하지 못했습니다.");

                return "redirect:/member/login";
            }

            String displayName = getDisplayName(member);
            member.setMemberName(displayName);

            int platformCount = memberPlatformService.countMemberPlatform(member.getMemberNo());
            if (result.isNewMember() || platformCount == 0) {
                savePendingMemberSession(session, member, displayName);

                return "redirect:/member/platform/select";
            }

            MemberVO loginMember = memberService.getMemberByNo(member.getMemberNo());
            if (loginMember == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "네이버 로그인 회원 정보를 불러오지 못했습니다.");

                return "redirect:/member/login";
            }

            if (loginMember.getMemberName() == null || loginMember.getMemberName().isBlank()) {
                loginMember.setMemberName(displayName);
            }

            saveLoginSession(session, loginMember, member.getProvider(), displayName);

            return "redirect:/";

        } catch (MemberBlockedException e) {
            redirectAttributes.addFlashAttribute("blockedMessage", e.getMessage());

            return "redirect:/member/login";

        } catch (MemberWithdrawnException e) {
            /*
             * 네이버 OAuth 인증이 완료된 상태이므로 본인 확인이 끝난 것으로 보고
             * 기존 카카오 로그인과 동일한 계정 복구 절차를 사용합니다.
             */
            session.setAttribute("restoreMemberNo", e.getMemberNo());
            session.setAttribute("restoreProvider", PROVIDER_NAVER);

            redirectAttributes.addFlashAttribute("withdrawnMessage", WithdrawPolicy.buildWithdrawnMessage(e.getWithdrawnAt()));

            return "redirect:/member/login";

       } catch (IllegalArgumentException | IllegalStateException e) {
            if (log.isWarnEnabled()) {
                log.warn("네이버 로그인 처리 실패", e);
            }
            redirectAttributes.addFlashAttribute("errorMessage", "네이버 로그인 처리 중 오류가 발생했습니다.");
            return "redirect:/member/login";
        }
    }

    /**
     * 신규 네이버 회원이 OTT 선택을 완료하기 전까지 필요한 정보를 저장합니다.
     */
    private void savePendingMemberSession(HttpSession session, MemberSocialJoinVO member, String displayName) {

        session.setAttribute("pendingMemberNo", member.getMemberNo());
        session.setAttribute("pendingMemberId", member.getMemberId());
        session.setAttribute("pendingMemberName", member.getMemberName());
        session.setAttribute("pendingNickname", member.getNickname());
        session.setAttribute("pendingRole", member.getRole());
        session.setAttribute("pendingProvider", member.getProvider());
        session.setAttribute("pendingDisplayName", displayName);
        session.setAttribute("pendingProfileImage", member.getProfileImage());
    }

    /**
     * 기존 일반 로그인 및 카카오 로그인과 동일한 세션 키를 저장합니다.
     */
    private void saveLoginSession(HttpSession session, MemberVO loginMember, String provider, String displayName) {

        session.setAttribute("loginMember", loginMember);

        session.setAttribute("memberNo", loginMember.getMemberNo());
        session.setAttribute("memberId", loginMember.getMemberId());
        session.setAttribute("memberName", loginMember.getMemberName());
        session.setAttribute("nickname", loginMember.getNickname());
        session.setAttribute("role", loginMember.getRole());
        session.setAttribute("profileImage", loginMember.getProfileImage());

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

    /**
     * 회원 이름이 비어 있을 때 닉네임과 기본 문구 순서로 대체합니다.
     */
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

    // 네이버 OAuth 콜백 오류 상세는 로그에만 기록
    private String buildCallbackErrorMessage(String errorDescription) {
        if (errorDescription != null && !errorDescription.isBlank() && log.isWarnEnabled()) {
            log.warn("네이버 OAuth 콜백 오류: {}", errorDescription);
        }
        return "네이버 로그인이 취소되었거나 실패했습니다.";
    }
}
