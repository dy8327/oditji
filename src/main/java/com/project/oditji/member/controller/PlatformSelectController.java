package com.project.oditji.member.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.PlatformVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member/platform")
public class PlatformSelectController {

    private final MemberPlatformService memberPlatformService;
    private final MemberService memberService;

    public PlatformSelectController(
            MemberPlatformService memberPlatformService,
            MemberService memberService) {

        this.memberPlatformService = memberPlatformService;
        this.memberService = memberService;
    }

    @GetMapping("/select")
    public String selectPlatformForm(HttpSession session, Model model) {

        Long pendingMemberNo = getLongSessionValue(session, "pendingMemberNo");
        Long loginMemberNo = getLongSessionValue(session, "loginMemberNo");

        System.out.println("===== OTT 선택 화면 진입 =====");
        System.out.println("pendingMemberNo = " + pendingMemberNo);
        System.out.println("loginMemberNo = " + loginMemberNo);

        if (pendingMemberNo == null && loginMemberNo == null) {
            return "redirect:/member/login";
        }

        List<PlatformVO> platformList =
                memberPlatformService.findPlatformList();

        model.addAttribute("platformList", platformList);

        return "member/selectOtt";
    }

    @PostMapping("/select")
    public String savePlatform(
            @RequestParam(value = "platformNoList", required = false)
            List<Long> platformNoList,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long pendingMemberNo = getLongSessionValue(session, "pendingMemberNo");
        Long loginMemberNo = getLongSessionValue(session, "loginMemberNo");

        Long memberNo =
                pendingMemberNo != null ? pendingMemberNo : loginMemberNo;

        System.out.println("===== OTT 선택 저장 =====");
        System.out.println("memberNo = " + memberNo);
        System.out.println("platformNoList = " + platformNoList);

        if (memberNo == null) {
            return "redirect:/member/login";
        }

        try {

            memberPlatformService.saveMemberPlatforms(memberNo, platformNoList);

            // 신규 SNS 회원이면 로그인 세션 생성
            if (pendingMemberNo != null) {

                MemberVO loginMember =
                        memberService.getMemberByNo(pendingMemberNo);

                String displayName =
                        (String) session.getAttribute("pendingDisplayName");

                if (loginMember.getMemberName() == null
                        || loginMember.getMemberName().isBlank()) {

                    loginMember.setMemberName(displayName);
                }

                session.setAttribute("loginMember", loginMember);
                session.setAttribute("loginMemberNo", loginMember.getMemberNo());

                // 성인인증 등 공통 기능에서 사용할 회원 번호
                session.setAttribute("memberNo", loginMember.getMemberNo());

                session.setAttribute("loginMemberId", loginMember.getMemberId());
                session.setAttribute("loginMemberName", loginMember.getMemberName());
                session.setAttribute("loginNickname", loginMember.getNickname());
                session.setAttribute("loginRole", loginMember.getRole());
                session.setAttribute("loginProvider",
                        session.getAttribute("pendingProvider"));
                session.setAttribute("loginDisplayName", displayName);

                session.removeAttribute("pendingMemberNo");
                session.removeAttribute("pendingMemberId");
                session.removeAttribute("pendingMemberName");
                session.removeAttribute("pendingNickname");
                session.removeAttribute("pendingRole");
                session.removeAttribute("pendingProvider");
                session.removeAttribute("pendingDisplayName");
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "OTT 정보가 저장되었습니다."
            );

            return "redirect:/";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "redirect:/member/platform/select";

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "OTT 정보 저장 중 오류가 발생했습니다."
            );

            return "redirect:/member/platform/select";
        }
    }

    private Long getLongSessionValue(HttpSession session, String key) {

        Object value = session.getAttribute(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }

        return Long.valueOf(String.valueOf(value));
    }
}