package com.project.oditji.member.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.PlatformVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member/platform")
public class PlatformSelectController {

    private static final String SESSION_PENDING_MEMBER_NO = "pendingMemberNo";
    private static final String SESSION_LOGIN_MEMBER_NO = "loginMemberNo";

    private final MemberPlatformService memberPlatformService;
    private final MemberService memberService;
    private static final Logger log = LoggerFactory.getLogger(PlatformSelectController.class);

    public PlatformSelectController(MemberPlatformService memberPlatformService, MemberService memberService) {

        this.memberPlatformService = memberPlatformService;
        this.memberService = memberService;
    }

    @GetMapping("/select")
    public String selectPlatformForm(HttpSession session, Model model) {

        Long pendingMemberNo = getLongSessionValue(session, SESSION_PENDING_MEMBER_NO);
        Long loginMemberNo = getLongSessionValue(session, SESSION_LOGIN_MEMBER_NO);

        if (pendingMemberNo == null && loginMemberNo == null) {
            return "redirect:/member/login";
        }

        Long memberNo = pendingMemberNo != null ? pendingMemberNo : loginMemberNo;

        /*
         * 카카오 자동가입 회원은 EMAIL이 NULL로 저장되어 있으므로
         * 아직 이메일이 없는 경우 OTT 선택 화면에서 함께 입력받는다.
         */
        MemberVO member = memberService.getMemberByNo(memberNo);
        boolean needEmailInput = member == null || member.getEmail() == null || member.getEmail().isBlank();

        List<PlatformVO> platformList = memberPlatformService.findPlatformList();

        model.addAttribute("platformList", platformList);
        model.addAttribute("needEmailInput", needEmailInput);

        return "member/selectOtt";
    }

    @PostMapping("/select")
    public String savePlatform(
            @RequestParam(value = "platformNoList", required = false)
            List<Long> platformNoList,
            @RequestParam(value = "noOtt", required = false)
            String noOtt,
            @RequestParam(value = "email", required = false)
            String email,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long pendingMemberNo = getLongSessionValue(session, SESSION_PENDING_MEMBER_NO);
        Long loginMemberNo = getLongSessionValue(session, SESSION_LOGIN_MEMBER_NO);

        Long memberNo = pendingMemberNo != null ? pendingMemberNo : loginMemberNo;

        if (memberNo == null) {
            return "redirect:/member/login";
        }

        try {

            /*
             * 아직 이메일이 없는 SNS 자동가입 회원이면 이 화면에서 함께 등록한다.
             * 이미 이메일이 있는 회원은 여기서 값을 보내더라도 건드리지 않는다.
             */
            MemberVO currentMember = memberService.getMemberByNo(memberNo);
            boolean needEmailInput = currentMember == null || currentMember.getEmail() == null || currentMember.getEmail().isBlank();

            if (needEmailInput) {
                memberService.updateSnsMemberEmail(memberNo, email);
            }

            memberPlatformService.saveMemberPlatforms(memberNo, platformNoList, noOtt);

            // 신규 SNS 회원이면 로그인 세션 생성
            if (pendingMemberNo != null) {

                MemberVO loginMember = memberService.getMemberByNo(pendingMemberNo);
                String displayName = (String) session.getAttribute("pendingDisplayName");

                if (loginMember.getMemberName() == null || loginMember.getMemberName().isBlank()) {
                    loginMember.setMemberName(displayName);
                }

                session.setAttribute("loginMember", loginMember);
                session.setAttribute(SESSION_LOGIN_MEMBER_NO, loginMember.getMemberNo());

                // 성인인증 등 공통 기능에서 사용할 회원 번호
                session.setAttribute("memberNo", loginMember.getMemberNo());

                session.setAttribute("loginMemberId", loginMember.getMemberId());
                session.setAttribute("loginMemberName", loginMember.getMemberName());
                session.setAttribute("loginNickname", loginMember.getNickname());
                session.setAttribute("loginRole", loginMember.getRole());
                session.setAttribute("loginProvider", session.getAttribute("pendingProvider"));
                session.setAttribute("loginDisplayName", displayName);

                session.removeAttribute(SESSION_PENDING_MEMBER_NO);
                session.removeAttribute("pendingMemberId");
                session.removeAttribute("pendingMemberName");
                session.removeAttribute("pendingNickname");
                session.removeAttribute("pendingRole");
                session.removeAttribute("pendingProvider");
                session.removeAttribute("pendingDisplayName");
            }

            redirectAttributes.addFlashAttribute("successMessage", "OTT 정보가 저장되었습니다."
            );

            return "redirect:/";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/member/platform/select";

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("OTT 정보 저장 중 오류 - memberNo: {}", memberNo, e);
            }
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "OTT 정보 저장 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

            return "redirect:/member/platform/select";
        }
    }

    private Long getLongSessionValue(HttpSession session, String key) {

        Object value = session.getAttribute(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Long longValue) {
            return longValue;
        }

        if (value instanceof Integer integerValue) {
            return integerValue.longValue();
        }

        return Long.valueOf(String.valueOf(value));
    }
}