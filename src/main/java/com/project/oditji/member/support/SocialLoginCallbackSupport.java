package com.project.oditji.member.support;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.SocialLoginResultVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Google, Kakao, Naver OAuth 콜백 이후의 공통 로그인 완료 절차입니다.
 *
 * 회원 유효성 확인, 세션 ID 교체, OTT 선택 분기, 로그인 세션 저장을
 * 한 곳에서 처리하고 공급자별 컨트롤러는 OAuth 검증과 예외 안내만 담당합니다.
 */
@Component
public class SocialLoginCallbackSupport {

    private static final String REDIRECT_MEMBER_LOGIN = "redirect:/member/login";
    private static final String REDIRECT_PLATFORM_SELECT = "redirect:/member/platform/select";
    private static final String REDIRECT_HOME = "redirect:/";

    private final MemberPlatformService memberPlatformService;
    private final MemberService memberService;

    public SocialLoginCallbackSupport(
            MemberPlatformService memberPlatformService,
            MemberService memberService) {

        this.memberPlatformService = memberPlatformService;
        this.memberService = memberService;
    }

    /**
     * 소셜 로그인 공급자별 표시명 정책과 오류 메시지를 한 객체로 전달합니다.
     *
     * 기존 completeLogin 메서드의 과도한 매개변수를 줄이기 위한 설정 객체입니다.
     */
    public record LoginOptions(
            boolean nicknameFirst,
            String fallbackName,
            boolean synchronizeMemberName,
            String missingMemberMessage,
            String invalidMemberNoMessage,
            String missingStoredMemberMessage) {
    }

    /**
     * 소셜 로그인 완료 후 회원 검증, OTT 선택 분기와 세션 저장을 처리합니다.
     */
    public String completeLogin(
            SocialLoginResultVO result,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            LoginOptions options) {

        if (result == null || result.getMember() == null) {
            addErrorMessage(
                    redirectAttributes,
                    options.missingMemberMessage()
            );
            return REDIRECT_MEMBER_LOGIN;
        }

        MemberSocialJoinVO member = result.getMember();
        if (member.getMemberNo() <= 0) {
            addErrorMessage(
                    redirectAttributes,
                    options.invalidMemberNoMessage()
            );
            return REDIRECT_MEMBER_LOGIN;
        }

        request.changeSessionId();

        String displayName = SocialLoginSessionSupport.resolveDisplayName(
                member,
                options.nicknameFirst(),
                options.fallbackName()
        );

        if (options.synchronizeMemberName()) {
            member.setMemberName(displayName);
        }

        int platformCount = memberPlatformService.countMemberPlatform(member.getMemberNo());
        if (result.isNewMember() || platformCount == 0) {
            SocialLoginSessionSupport.savePendingSession(session, member, displayName);
            return REDIRECT_PLATFORM_SELECT;
        }

        MemberVO loginMember = memberService.getMemberByNo(member.getMemberNo());
        if (loginMember == null) {
            addErrorMessage(
                    redirectAttributes,
                    options.missingStoredMemberMessage()
            );
            return REDIRECT_MEMBER_LOGIN;
        }

        if (options.synchronizeMemberName()
                && (loginMember.getMemberName() == null || loginMember.getMemberName().isBlank())) {
            loginMember.setMemberName(displayName);
        }

        SocialLoginSessionSupport.saveLoginSession(
                session,
                loginMember,
                member.getProvider(),
                displayName
        );

        return REDIRECT_HOME;
    }

    private void addErrorMessage(
            RedirectAttributes redirectAttributes,
            String message) {

        if (message != null && !message.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", message);
        }
    }
}
