package com.project.oditji.member.controller;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.service.GoogleLoginService;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.support.WithdrawPolicy;
import com.project.oditji.member.vo.GoogleLoginResultVO;
import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpSession;

/**
 * Google 로그인 시작 및 콜백 처리를 담당하는 컨트롤러입니다.
 *
 * 기존 카카오·네이버 로그인과 세션 구조를 동일하게 유지하므로
 * 헤더, 마이페이지, 권한 분기 로직을 별도로 수정하지 않아도 됩니다.
 */
@Controller
public class GoogleLoginController {

    private static final String GOOGLE_OAUTH_STATE =
            "googleOAuthState";

    private final GoogleLoginService googleLoginService;
    private final MemberPlatformService memberPlatformService;
    private final MemberService memberService;

    public GoogleLoginController(
            GoogleLoginService googleLoginService,
            MemberPlatformService memberPlatformService,
            MemberService memberService) {

        this.googleLoginService = googleLoginService;
        this.memberPlatformService = memberPlatformService;
        this.memberService = memberService;
    }

    /**
     * Google 로그인 동의 화면으로 이동합니다.
     *
     * state를 세션에 저장하고 콜백에서 비교해
     * 다른 사이트가 만든 위조 로그인 요청을 차단합니다.
     */
    @GetMapping("/member/google/login")
    public String googleLogin(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {

            String state = UUID.randomUUID().toString();

            session.setAttribute(GOOGLE_OAUTH_STATE, state);

            return "redirect:" + googleLoginService.getGoogleLoginUrl(state);

        } catch (IllegalStateException ignored) {
        redirectAttributes.addFlashAttribute("errorMessage", "Google 로그인 준비 중 오류가 발생했습니다.");
        return "redirect:/member/login";
        }
    }

    /**
     * Google 인증 후 돌아오는 콜백 주소입니다.
     *
     * Google Cloud Console의 승인된 리디렉션 URI에는
     * http://localhost:8080/oditji/member/google/callback
     * 을 정확히 등록해야 합니다.
     */
    @GetMapping("/member/google/callback")
    public String googleCallback(
            @RequestParam(value = "code", required = false)
            String code,
            @RequestParam(value = "state", required = false)
            String state,
            @RequestParam(value = "error", required = false)
            String error,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String expectedState =
                (String) session.getAttribute(
                        GOOGLE_OAUTH_STATE);

        session.removeAttribute(
                GOOGLE_OAUTH_STATE);

        if (error != null && !error.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Google 로그인이 취소되었거나 승인되지 않았습니다.");

            return "redirect:/member/login";
        }

        if (expectedState == null
                || state == null
                || !expectedState.equals(state)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Google 로그인 요청 검증에 실패했습니다. 다시 시도해주세요.");

            return "redirect:/member/login";
        }

        try {

            GoogleLoginResultVO result =
                    googleLoginService.googleLogin(code);

            if (result == null || result.getMember() == null) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Google 회원정보를 확인할 수 없습니다.");

                return "redirect:/member/login";
            }

            MemberSocialJoinVO member =
                    result.getMember();

            if (member.getMemberNo() <= 0) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Google 회원번호를 확인할 수 없습니다.");

                return "redirect:/member/login";
            }

            String displayName =
                    getDisplayName(member);

            /*
             * Google 로그인은 MEMBER_NAME을 저장하지 않고
             * Google 표시 이름을 NICKNAME으로 사용합니다.
             */
            int platformCount =
                    memberPlatformService.countMemberPlatform(
                            member.getMemberNo());

            /*
             * 신규 회원 또는 아직 OTT를 고르지 않은 회원은
             * 로그인 완료 전에 OTT 선택 화면으로 보냅니다.
             */
            if (result.isNewMember()
                    || platformCount == 0) {

                savePendingSession(
                        session,
                        member,
                        displayName);

                return "redirect:/member/platform/select";
            }

            MemberVO loginMember =
                    memberService.getMemberByNo(
                            member.getMemberNo());

            if (loginMember == null) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Google 로그인 회원정보를 불러오지 못했습니다.");

                return "redirect:/member/login";
            }

            saveLoginSession(
                    session,
                    loginMember,
                    member.getProvider(),
                    displayName);

            return "redirect:/";

        } catch (MemberBlockedException e) {

            redirectAttributes.addFlashAttribute(
                    "blockedMessage",
                    e.getMessage());

            return "redirect:/member/login";

        } catch (MemberWithdrawnException e) {

            /*
             * Google OAuth 인증에 성공했으므로 본인 확인이 끝난 상태입니다.
             * 기존 탈퇴 복구 흐름과 동일하게 복구 대상 번호를 세션에 보관합니다.
             */
            session.setAttribute(
                    "restoreMemberNo",
                    e.getMemberNo());

            session.setAttribute(
                    "restoreProvider",
                    "GOOGLE");

            redirectAttributes.addFlashAttribute(
                    "withdrawnMessage",
                    WithdrawPolicy.buildWithdrawnMessage(
                            e.getWithdrawnAt()));

            return "redirect:/member/login";

        } catch (IllegalStateException ignored) {
        redirectAttributes.addFlashAttribute("errorMessage", "Google 로그인 처리 중 오류가 발생했습니다.");
        return "redirect:/member/login";
        }
    }

    private void savePendingSession(
            HttpSession session,
            MemberSocialJoinVO member,
            String displayName) {

        session.setAttribute(
                "pendingMemberNo",
                member.getMemberNo());

        session.setAttribute(
                "pendingMemberId",
                member.getMemberId());

        session.setAttribute(
                "pendingMemberName",
                member.getMemberName());

        session.setAttribute(
                "pendingNickname",
                member.getNickname());

        session.setAttribute(
                "pendingRole",
                member.getRole());

        session.setAttribute(
                "pendingProvider",
                member.getProvider());

        session.setAttribute(
                "pendingDisplayName",
                displayName);

        session.setAttribute(
                "pendingProfileImage",
                member.getProfileImage());
    }

    /**
     * 기존 일반·카카오·네이버 로그인에서 사용하는 세션명을 모두 맞춥니다.
     */
    private void saveLoginSession(
            HttpSession session,
            MemberVO loginMember,
            String provider,
            String displayName) {

        session.setAttribute(
                "loginMember",
                loginMember);

        session.setAttribute(
                "memberNo",
                loginMember.getMemberNo());

        session.setAttribute(
                "memberId",
                loginMember.getMemberId());

        session.setAttribute(
                "memberName",
                loginMember.getMemberName());

        session.setAttribute(
                "nickname",
                loginMember.getNickname());

        session.setAttribute(
                "role",
                loginMember.getRole());

        session.setAttribute(
                "profileImage",
                loginMember.getProfileImage());

        session.setAttribute(
                "loginMemberNo",
                loginMember.getMemberNo());

        session.setAttribute(
                "loginMemberId",
                loginMember.getMemberId());

        session.setAttribute(
                "loginMemberName",
                loginMember.getMemberName());

        session.setAttribute(
                "loginNickname",
                loginMember.getNickname());

        session.setAttribute(
                "loginRole",
                loginMember.getRole());

        session.setAttribute(
                "loginProvider",
                provider);

        session.setAttribute(
                "loginDisplayName",
                displayName);

        String adultVerified =
                loginMember.getAdultVerified();

        if (adultVerified == null
                || adultVerified.isBlank()) {

            adultVerified = "N";
        }

        session.setAttribute(
                "ADULT_VERIFIED",
                adultVerified);
    }

    private String getDisplayName(
            MemberSocialJoinVO member) {

        String displayName =
                member.getNickname();

        if (displayName == null
                || displayName.isBlank()) {

            displayName = member.getMemberName();
        }

        if (displayName == null
                || displayName.isBlank()) {

            displayName = "구글회원";
        }

        return displayName;
    }
}
