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
import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.PlatformVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member/platform")
public class PlatformSelectController {

    private final MemberPlatformService memberPlatformService;

    public PlatformSelectController(MemberPlatformService memberPlatformService) {
        this.memberPlatformService = memberPlatformService;
    }

    @GetMapping("/select")
    public String selectPlatformForm(HttpSession session, Model model) {

        Long pendingMemberNo = getLongSessionValue(session, "pendingMemberNo");
        Long loginMemberNo = getLongSessionValue(session, "loginMemberNo");

        System.out.println("===== OTT 선택 화면 진입 =====");
        System.out.println("pendingMemberNo = " + pendingMemberNo);
        System.out.println("loginMemberNo = " + loginMemberNo);

        /*
         * 신규 SNS 회원은 아직 loginMemberNo가 없고 pendingMemberNo만 있다.
         * 기존 로그인 회원이 플랫폼 수정하러 들어오는 경우는 loginMemberNo를 사용한다.
         */
        if (pendingMemberNo == null && loginMemberNo == null) {
            return "redirect:/member/login";
        }

        List<PlatformVO> platformList = memberPlatformService.findPlatformList();

        model.addAttribute("platformList", platformList);

        return "member/selectOtt";
    }

    @PostMapping("/select")
    public String savePlatform(@RequestParam(value = "platformNoList", required = false) List<Long> platformNoList,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        Long pendingMemberNo = getLongSessionValue(session, "pendingMemberNo");
        Long loginMemberNo = getLongSessionValue(session, "loginMemberNo");

        Long memberNo = pendingMemberNo != null ? pendingMemberNo : loginMemberNo;

        System.out.println("===== OTT 선택 저장 =====");
        System.out.println("pendingMemberNo = " + pendingMemberNo);
        System.out.println("loginMemberNo = " + loginMemberNo);
        System.out.println("저장 대상 memberNo = " + memberNo);
        System.out.println("platformNoList = " + platformNoList);

        if (memberNo == null) {
            return "redirect:/member/login";
        }

        try {
            memberPlatformService.saveMemberPlatforms(memberNo, platformNoList);

            /*
             * pendingLoginMember가 있으면 신규 SNS 회원의 OTT 선택 완료 상황이다.
             * 이때 pending 세션을 정식 로그인 세션으로 옮긴다.
             */
            MemberSocialJoinVO pendingLoginMember =
                    (MemberSocialJoinVO) session.getAttribute("pendingLoginMember");

            if (pendingLoginMember != null) {

                session.setAttribute("loginMember", pendingLoginMember);
                session.setAttribute("loginMemberNo", pendingLoginMember.getMemberNo());
                session.setAttribute("loginMemberId", pendingLoginMember.getMemberId());
                session.setAttribute("loginMemberName", pendingLoginMember.getMemberName());
                session.setAttribute("loginNickname", pendingLoginMember.getNickname());
                session.setAttribute("loginRole", pendingLoginMember.getRole());
                session.setAttribute("loginProvider", pendingLoginMember.getProvider());
                session.setAttribute("loginDisplayName", session.getAttribute("pendingDisplayName"));

                session.removeAttribute("pendingMemberNo");
                session.removeAttribute("pendingMemberId");
                session.removeAttribute("pendingMemberName");
                session.removeAttribute("pendingNickname");
                session.removeAttribute("pendingRole");
                session.removeAttribute("pendingProvider");
                session.removeAttribute("pendingDisplayName");
                session.removeAttribute("pendingLoginMember");

                System.out.println("===== OTT 선택 완료 후 정식 로그인 처리 =====");
                System.out.println("loginMemberNo = " + session.getAttribute("loginMemberNo"));
                System.out.println("loginMemberId = " + session.getAttribute("loginMemberId"));
                System.out.println("loginMemberName = " + session.getAttribute("loginMemberName"));
                System.out.println("loginNickname = " + session.getAttribute("loginNickname"));
                System.out.println("loginProvider = " + session.getAttribute("loginProvider"));
            }

            redirectAttributes.addFlashAttribute("successMessage", "OTT 정보가 저장되었습니다.");
            return "redirect:/";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/member/platform/select";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "OTT 정보 저장 중 오류가 발생했습니다.");
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