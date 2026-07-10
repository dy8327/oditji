package com.project.oditji.member.controller;

import java.util.List;
import java.util.UUID;
import java.io.File;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/join")
    public String joinForm() {
        return "member/join";
    }

    @PostMapping("/join")
    public String join(MemberVO memberVO,
                    @RequestParam(value = "ottList", required = false) List<String> ottList,
                    @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile,
                    Model model,
                    RedirectAttributes redirectAttributes) {

        System.out.println("===== 회원가입 요청 들어옴 =====");
        System.out.println("memberId = " + memberVO.getMemberId());
        System.out.println("memberName = " + memberVO.getMemberName());
        System.out.println("nickname = " + memberVO.getNickname());
        System.out.println("email = " + memberVO.getEmail());
        System.out.println("ottList = " + ottList);

        try {
            if (profileImageFile != null && !profileImageFile.isEmpty()) {

                String uploadDir = "C:/oditji/upload/profile/";

                File dir = new File(uploadDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String originalFileName = profileImageFile.getOriginalFilename();
                String ext = "";

                if (originalFileName != null && originalFileName.contains(".")) {
                    ext = originalFileName.substring(originalFileName.lastIndexOf("."));
                }

                String saveFileName = UUID.randomUUID().toString() + ext;

                File saveFile = new File(uploadDir + saveFileName);
                profileImageFile.transferTo(saveFile);

                // DB에는 경로 말고 파일명만 저장
                memberVO.setProfileImage(saveFileName);

                System.out.println("저장된 프로필 파일명 = " + saveFileName);
            }

            memberService.joinMember(memberVO, ottList);

            redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다.");
            return "redirect:/member/login";

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", e.getMessage());
            return "member/join";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage",
                    "회원가입 처리 중 오류가 발생했습니다: " + e.getClass().getName() + " / " + e.getMessage());
            return "member/join";
        }
    }

    @GetMapping("/login")
    public String loginForm() {
        return "member/login";
    }

    @PostMapping("/login")
    public String login(MemberVO memberVO, HttpSession session, RedirectAttributes redirectAttributes) {

        MemberVO loginMember = memberService.loginMember(memberVO);

        System.out.println("===== loginMember 실행됨 =====");
        System.out.println("입력 아이디: " + memberVO.getMemberId());
        System.out.println("입력 비밀번호: " + memberVO.getMemberPw());

        // 1null 체크를 가장 먼저 수행
        if (loginMember == null) {
            redirectAttributes.addFlashAttribute("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "redirect:/member/login";
        }

        // null이 아님이 확인한 후에만 조회 결과를 출력합니다. (NPE 방지)
        System.out.println("조회 결과 memberNo: " + loginMember.getMemberNo());
        System.out.println("조회 결과 memberId: " + loginMember.getMemberId());
        System.out.println("조회 결과 memberName: " + loginMember.getMemberName());
        System.out.println("조회 결과 role: " + loginMember.getRole());

        // 세션 저장
        session.setAttribute("loginMember", loginMember);
        session.setAttribute("memberNo", loginMember.getMemberNo());
        session.setAttribute("memberId", loginMember.getMemberId());
        session.setAttribute("memberName", loginMember.getMemberName());
        session.setAttribute("nickname", loginMember.getNickname());
        session.setAttribute("role", loginMember.getRole());

        System.out.println("세션 저장 완료: " + session.getAttribute("memberId"));

        String displayName = loginMember.getMemberName();

        if (displayName == null || displayName.isBlank()) {
            displayName = loginMember.getNickname();
        }

        if (displayName == null || displayName.isBlank()) {
            displayName = "회원";
        }

        session.setAttribute("loginDisplayName", displayName);

        return "redirect:/";
    }

    @GetMapping("/logout") 
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/checkId")
    @ResponseBody
    public String checkId(@RequestParam("memberId") String memberId) {

        boolean duplicate = memberService.isDuplicateId(memberId);

        if (duplicate) {
            return "N"; // 이미 사용 중
        }

        return "Y"; // 사용 가능
    }

    @GetMapping("/checkNickname")
    @ResponseBody
    public String checkNickname(@RequestParam("nickname") String nickname) {

        System.out.println("===== 닉네임 중복확인 요청 =====");
        System.out.println("nickname = " + nickname);

        boolean duplicate = memberService.isDuplicateNickname(nickname);

        if (duplicate) {
            return "N"; // 이미 사용 중
        }

        return "Y"; // 사용 가능
    }

    // 마이페이지 모달 DB저장
    @GetMapping("/mypage")
    public String mypage(HttpSession session, Model model) {

        boolean socialMember =
                session.getAttribute("loginProvider") != null;

        model.addAttribute("socialMember", socialMember);

        return "member/mypage";
    }

    @PostMapping("/update")
    public String updateMember(
            MemberVO memberVO,
            @RequestParam(value = "currentPw", required = false) String currentPw,
            @RequestParam(value = "newPw", required = false) String newPw,
            @RequestParam(value = "newPwCheck", required = false) String newPwCheck,
            @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
        memberVO.setMemberNo(loginMember.getMemberNo());

        /* =========================
        비밀번호 변경
        ========================= */

        if (newPw != null && !newPw.isBlank()) {

            if (currentPw == null || currentPw.isBlank()) {
                return redirectWithError(redirectAttributes, "현재 비밀번호를 입력해주세요.");
            }

            if (!newPw.equals(newPwCheck)) {
                return redirectWithError(redirectAttributes, "새 비밀번호가 일치하지 않습니다.");
            }

            if (!memberService.checkPassword(loginMember.getMemberNo(), currentPw)) {
                return redirectWithError(redirectAttributes, "현재 비밀번호가 일치하지 않습니다.");
            }

            memberVO.setMemberPw(newPw);
        }

        /* =========================
        프로필 이미지
        ========================= */

        if (profileImageFile != null && !profileImageFile.isEmpty()) {

            try {

                String uploadDir = "C:/oditji/upload/profile/";

                File dir = new File(uploadDir);

                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String original = profileImageFile.getOriginalFilename();
                String ext = original.substring(original.lastIndexOf("."));
                String saveName = UUID.randomUUID() + ext;

                profileImageFile.transferTo(new File(uploadDir + saveName));

                memberVO.setProfileImage(saveName);

            } catch (Exception e) {

                return redirectWithError(redirectAttributes, "이미지 업로드에 실패했습니다.");

            }

        }

        /* =========================
        회원정보 수정
        ========================= */

        memberService.updateMember(memberVO);

        MemberVO updated = memberService.getMemberByNo(memberVO.getMemberNo());

        session.setAttribute("loginMember", updated);

        redirectAttributes.addFlashAttribute("message", "회원정보가 수정되었습니다.");

        return "redirect:/member/mypage";
    }

    @GetMapping("/checkUpdateNickname")
    @ResponseBody
    public String checkUpdateNickname(
            @RequestParam String nickname,
            @RequestParam Long memberNo) {

        boolean result =
                memberService.checkUpdateNickname(
                        memberNo,
                        nickname
                );

        return result ? "Y" : "N";
    }

    @GetMapping("/checkPassword")
    @ResponseBody
    public String checkPassword(
            @RequestParam String password,
            HttpSession session) {

        MemberVO loginMember =
                (MemberVO) session.getAttribute("loginMember");

        boolean result =
                memberService.checkPassword(
                        loginMember.getMemberNo(),
                        password
                );

        return result ? "Y" : "N";
    }

    @PostMapping("/updateOtt")
    public String updateOtt(
            @RequestParam(value = "ottList", required = false) List<String> ottList,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

        memberService.updateMemberOtt(loginMember.getMemberNo(), ottList);

        redirectAttributes.addFlashAttribute("message", "OTT 정보가 수정되었습니다.");

        return "redirect:/member/mypage";
    }

    @PostMapping("/delete")
    public String deleteMember(HttpSession session) {

        MemberVO loginMember =
                (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        memberService.deleteMember(loginMember.getMemberNo());

        session.invalidate();

        return "redirect:/";
    }

    private String redirectWithError(RedirectAttributes redirectAttributes, String message) {

        redirectAttributes.addFlashAttribute("errorMessage", message);
        redirectAttributes.addFlashAttribute("openMemberModal", true);

        return "redirect:/member/mypage";
    }

    @GetMapping("/findId")
    public String findId() {
        return "member/findId";
    }

    @PostMapping("/findId")
    public String findIdPost(
            @RequestParam String memberName,
            @RequestParam String email,
            Model model) {

        MemberVO memberVO = new MemberVO();
        memberVO.setMemberName(memberName);
        memberVO.setEmail(email);

        MemberVO result = memberService.findId(memberVO);

        if (result != null) {
            model.addAttribute(
                    "findIdResult",
                    result.getMemberId()
            );
        } else {
            model.addAttribute(
                    "errorMessage",
                    "일치하는 회원 정보가 없습니다."
            );
        }

        return "member/findId";
    }

    @GetMapping("/findPw")
    public String findPw() {
        return "member/findPw";
    }

    @PostMapping("/findPw")
    public String findPwPost(
            @RequestParam String memberId,
            @RequestParam String memberName,
            @RequestParam String email,
            Model model) {

        MemberVO memberVO = new MemberVO();

        memberVO.setMemberId(memberId);
        memberVO.setMemberName(memberName);
        memberVO.setEmail(email);

        MemberVO result = memberService.findPw(memberVO);

        if (result != null) {
            model.addAttribute(
                    "findPwResult",
                    result.getMemberPw()
            );
        } else {
            model.addAttribute(
                    "errorMessage",
                    "일치하는 회원 정보가 없습니다."
            );
        }

        return "member/findPw";
    }
}