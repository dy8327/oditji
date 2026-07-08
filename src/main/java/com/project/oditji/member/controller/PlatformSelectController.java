package com.project.oditji.member.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.vo.PlatformVO;

import jakarta.servlet.http.HttpSession;

@Controller
public class PlatformSelectController {

    private final MemberPlatformService memberPlatformService;

    public PlatformSelectController(MemberPlatformService memberPlatformService) {
        this.memberPlatformService = memberPlatformService;
    }

    @GetMapping("/member/platform/select")
    public String platformSelectForm(HttpSession session, Model model) {

        Long pendingMemberNo = (Long) session.getAttribute("pendingMemberNo");

        if (pendingMemberNo == null) {
            return "redirect:/member/login";
        }

        List<PlatformVO> platformList = memberPlatformService.findPlatformList();
        model.addAttribute("platformList", platformList);

        return "member/platformSelect";
    }

    @PostMapping("/member/platform/select")
    public String platformSelect(
            @RequestParam(value = "platformNoList", required = false) List<Long> platformNoList,
            HttpSession session,
            Model model) {

        Long pendingMemberNo = (Long) session.getAttribute("pendingMemberNo");

        if (pendingMemberNo == null) {
            return "redirect:/member/login";
        }

        if (platformNoList == null || platformNoList.isEmpty()) {
            List<PlatformVO> platformList = memberPlatformService.findPlatformList();
            model.addAttribute("platformList", platformList);
            model.addAttribute("errorMsg", "사용 중인 OTT 플랫폼을 1개 이상 선택해주세요.");
            return "member/platformSelect";
        }

        memberPlatformService.saveMemberPlatforms(pendingMemberNo, platformNoList);

        Object pendingLoginMember = session.getAttribute("pendingLoginMember");

        session.setAttribute("loginMember", pendingLoginMember);
        session.setAttribute("loginMemberNo", pendingMemberNo);
        session.setAttribute("loginMemberId", session.getAttribute("pendingMemberId"));
        session.setAttribute("loginMemberName", session.getAttribute("pendingMemberName"));
        session.setAttribute("loginNickname", session.getAttribute("pendingNickname"));
        session.setAttribute("loginRole", session.getAttribute("pendingRole"));
        session.setAttribute("loginProvider", session.getAttribute("pendingProvider"));
        session.setAttribute("loginDisplayName", session.getAttribute("pendingDisplayName"));

        session.removeAttribute("pendingMemberNo");
        session.removeAttribute("pendingMemberId");
        session.removeAttribute("pendingMemberName");
        session.removeAttribute("pendingNickname");
        session.removeAttribute("pendingRole");
        session.removeAttribute("pendingProvider");
        session.removeAttribute("pendingDisplayName");
        session.removeAttribute("pendingLoginMember");

        return "redirect:/";
    }
}