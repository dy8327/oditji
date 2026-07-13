package com.project.oditji.member.controller;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member")
public class MemberController {

    private static final String LOGIN_REDIRECT_SESSION_KEY =
            "redirectAfterLogin";

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/join")
    public String joinForm() {
        return "member/join";
    }

    @PostMapping("/join")
    public String join(
            MemberVO memberVO,
            @RequestParam(
                    value = "ottList",
                    required = false
            ) List<String> ottList,
            @RequestParam(
                    value = "profileImageFile",
                    required = false
            ) MultipartFile profileImageFile,
            Model model,
            RedirectAttributes redirectAttributes) {

        System.out.println("===== 회원가입 요청 들어옴 =====");
        System.out.println(
                "memberId = "
                        + memberVO.getMemberId()
        );
        System.out.println(
                "memberName = "
                        + memberVO.getMemberName()
        );
        System.out.println(
                "nickname = "
                        + memberVO.getNickname()
        );
        System.out.println(
                "email = "
                        + memberVO.getEmail()
        );
        System.out.println(
                "ottList = "
                        + ottList
        );

        try {

            if (profileImageFile != null
                    && !profileImageFile.isEmpty()) {

                String uploadDir =
                        "C:/oditji/upload/profile/";

                File dir = new File(uploadDir);

                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String originalFileName =
                        profileImageFile.getOriginalFilename();

                String ext = "";

                if (originalFileName != null
                        && originalFileName.contains(".")) {

                    ext = originalFileName.substring(
                            originalFileName.lastIndexOf(".")
                    );
                }

                String saveFileName =
                        UUID.randomUUID().toString()
                                + ext;

                File saveFile =
                        new File(
                                uploadDir
                                        + saveFileName
                        );

                profileImageFile.transferTo(
                        saveFile
                );

                /*
                 * DB에는 경로가 아닌
                 * 저장된 파일명만 저장한다.
                 */
                memberVO.setProfileImage(
                        saveFileName
                );

                System.out.println(
                        "저장된 프로필 파일명 = "
                                + saveFileName
                );
            }

            memberService.joinMember(
                    memberVO,
                    ottList
            );

            redirectAttributes.addFlashAttribute(
                    "message",
                    "회원가입이 완료되었습니다."
            );

            return "redirect:/member/login";

        } catch (IllegalArgumentException e) {

            e.printStackTrace();

            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "member/join";

        } catch (Exception e) {

            e.printStackTrace();

            model.addAttribute(
                    "errorMessage",
                    "회원가입 처리 중 오류가 발생했습니다: "
                            + e.getClass().getName()
                            + " / "
                            + e.getMessage()
            );

            return "member/join";
        }
    }

    /**
     * 로그인 화면
     *
     * 로그인 화면으로 이동하기 전 페이지를 세션에 저장한다.
     * 로그인 성공 후 해당 주소로 돌아간다.
     */
    @GetMapping("/login")
    public String loginForm(
            @RequestParam(
                    value = "redirect",
                    required = false
            ) String redirect,
            HttpServletRequest request,
            HttpSession session) {

        /*
         * URL 파라미터로 돌아갈 주소가 전달되었다면
         * 해당 주소를 우선 사용한다.
         */
        String redirectUrl =
                normalizeRedirectUrl(
                        redirect,
                        request
                );

        /*
         * redirect 파라미터가 없다면
         * Referer 헤더에서 이전 페이지를 확인한다.
         */
        if (redirectUrl == null) {

            redirectUrl =
                    extractPreviousUrl(
                            request
                    );
        }

        /*
         * 로그인, 회원가입 등 회원 관련 화면 자체는
         * 복귀 주소로 저장하지 않는다.
         */
        if (isUsableRedirectUrl(
                redirectUrl,
                request)) {

            session.setAttribute(
                    LOGIN_REDIRECT_SESSION_KEY,
                    redirectUrl
            );
        }

        return "member/login";
    }

    /**
     * 로그인 처리
     */
    @PostMapping("/login")
    public String login(
            MemberVO memberVO,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        MemberVO loginMember =
                memberService.loginMember(
                        memberVO
                );

        System.out.println(
                "===== loginMember 실행됨 ====="
        );

        System.out.println(
                "입력 아이디: "
                        + memberVO.getMemberId()
        );

        System.out.println(
                "입력 비밀번호: "
                        + memberVO.getMemberPw()
        );

        /*
         * 로그인 실패
         *
         * redirectAfterLogin 값은 삭제하지 않는다.
         * 사용자가 다시 로그인하면 원래 페이지로 이동한다.
         */
        if (loginMember == null) {

            redirectAttributes.addFlashAttribute(
                    "message",
                    "아이디 또는 비밀번호가 일치하지 않습니다."
            );

            return "redirect:/member/login";
        }

        System.out.println(
                "조회 결과 memberNo: "
                        + loginMember.getMemberNo()
        );

        System.out.println(
                "조회 결과 memberId: "
                        + loginMember.getMemberId()
        );

        System.out.println(
                "조회 결과 memberName: "
                        + loginMember.getMemberName()
        );

        System.out.println(
                "조회 결과 role: "
                        + loginMember.getRole()
        );

        /*
         * 로그인 회원 세션 저장
         */
        session.setAttribute(
                "loginMember",
                loginMember
        );

        session.setAttribute(
                "memberNo",
                loginMember.getMemberNo()
        );

        session.setAttribute(
                "memberId",
                loginMember.getMemberId()
        );

        session.setAttribute(
                "memberName",
                loginMember.getMemberName()
        );

        session.setAttribute(
                "nickname",
                loginMember.getNickname()
        );

        session.setAttribute(
                "role",
                loginMember.getRole()
        );

        System.out.println(
                "세션 저장 완료: "
                        + session.getAttribute("memberId")
        );

        String displayName =
                loginMember.getMemberName();

        if (displayName == null
                || displayName.isBlank()) {

            displayName =
                    loginMember.getNickname();
        }

        if (displayName == null
                || displayName.isBlank()) {

            displayName = "회원";
        }

        session.setAttribute(
                "loginDisplayName",
                displayName
        );

        /*
         * 로그인 화면으로 이동하기 전 주소를 꺼낸다.
         */
        String redirectUrl =
                (String) session.getAttribute(
                        LOGIN_REDIRECT_SESSION_KEY
                );

        /*
         * 한 번 사용한 복귀 주소는 제거한다.
         */
        session.removeAttribute(
                LOGIN_REDIRECT_SESSION_KEY
        );

        if (redirectUrl == null
                || redirectUrl.isBlank()) {

            return "redirect:/";
        }

        return "redirect:"
                + redirectUrl;
    }

    /**
     * 로그아웃 처리
     *
     * 로그아웃 링크를 누른 현재 페이지의 주소를 저장한 후
     * 세션을 무효화하고 해당 페이지로 돌아간다.
     */
    @GetMapping("/logout")
    public String logout(
            HttpServletRequest request,
            HttpSession session) {

        String redirectUrl =
                extractPreviousUrl(
                        request
                );

        /*
         * 세션 무효화 전에 이전 주소를 지역변수에 저장한다.
         */
        session.invalidate();

        if (!isUsableRedirectUrl(
                redirectUrl,
                request)) {

            return "redirect:/";
        }

        return "redirect:"
                + redirectUrl;
    }

    @GetMapping("/checkId")
    @ResponseBody
    public String checkId(
            @RequestParam("memberId")
            String memberId) {

        boolean duplicate =
                memberService.isDuplicateId(
                        memberId
                );

        if (duplicate) {
            return "N";
        }

        return "Y";
    }

    @GetMapping("/checkNickname")
    @ResponseBody
    public String checkNickname(
            @RequestParam("nickname")
            String nickname) {

        System.out.println(
                "===== 닉네임 중복확인 요청 ====="
        );

        System.out.println(
                "nickname = "
                        + nickname
        );

        boolean duplicate =
                memberService.isDuplicateNickname(
                        nickname
                );

        if (duplicate) {
            return "N";
        }

        return "Y";
    }

    /**
     * 마이페이지
     */
    @GetMapping("/mypage")
    public String mypage(
            HttpSession session,
            Model model) {

        boolean socialMember =
                session.getAttribute(
                        "loginProvider"
                ) != null;

        model.addAttribute(
                "socialMember",
                socialMember
        );

        return "member/mypage";
    }

    @PostMapping("/update")
    public String updateMember(
            MemberVO memberVO,
            @RequestParam(
                    value = "currentPw",
                    required = false
            ) String currentPw,
            @RequestParam(
                    value = "newPw",
                    required = false
            ) String newPw,
            @RequestParam(
                    value = "newPwCheck",
                    required = false
            ) String newPwCheck,
            @RequestParam(
                    value = "profileImageFile",
                    required = false
            ) MultipartFile profileImageFile,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        MemberVO loginMember =
                (MemberVO) session.getAttribute(
                        "loginMember"
                );

        memberVO.setMemberNo(
                loginMember.getMemberNo()
        );

        /*
         * 비밀번호 변경
         */
        if (newPw != null
                && !newPw.isBlank()) {

            if (currentPw == null
                    || currentPw.isBlank()) {

                return redirectWithError(
                        redirectAttributes,
                        "현재 비밀번호를 입력해주세요."
                );
            }

            if (!newPw.equals(newPwCheck)) {

                return redirectWithError(
                        redirectAttributes,
                        "새 비밀번호가 일치하지 않습니다."
                );
            }

            if (!memberService.checkPassword(
                    loginMember.getMemberNo(),
                    currentPw)) {

                return redirectWithError(
                        redirectAttributes,
                        "현재 비밀번호가 일치하지 않습니다."
                );
            }

            memberVO.setMemberPw(
                    newPw
            );
        }

        /*
         * 프로필 이미지
         */
        if (profileImageFile != null
                && !profileImageFile.isEmpty()) {

            try {

                String uploadDir =
                        "C:/oditji/upload/profile/";

                File dir =
                        new File(uploadDir);

                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String original =
                        profileImageFile.getOriginalFilename();

                String ext = "";

                if (original != null
                        && original.contains(".")) {

                    ext = original.substring(
                            original.lastIndexOf(".")
                    );
                }

                String saveName =
                        UUID.randomUUID()
                                + ext;

                profileImageFile.transferTo(
                        new File(
                                uploadDir
                                        + saveName
                        )
                );

                memberVO.setProfileImage(
                        saveName
                );

            } catch (Exception e) {

                return redirectWithError(
                        redirectAttributes,
                        "이미지 업로드에 실패했습니다."
                );
            }
        }

        /*
         * 회원정보 수정
         */
        memberService.updateMember(
                memberVO
        );

        MemberVO updated =
                memberService.getMemberByNo(
                        memberVO.getMemberNo()
                );

        session.setAttribute(
                "loginMember",
                updated
        );

        redirectAttributes.addFlashAttribute(
                "message",
                "회원정보가 수정되었습니다."
        );

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

        return result
                ? "Y"
                : "N";
    }

    @GetMapping("/checkPassword")
    @ResponseBody
    public String checkPassword(
            @RequestParam String password,
            HttpSession session) {

        MemberVO loginMember =
                (MemberVO) session.getAttribute(
                        "loginMember"
                );

        boolean result =
                memberService.checkPassword(
                        loginMember.getMemberNo(),
                        password
                );

        return result
                ? "Y"
                : "N";
    }

    @PostMapping("/updateOtt")
    public String updateOtt(
            @RequestParam(
                    value = "ottList",
                    required = false
            ) List<String> ottList,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        MemberVO loginMember =
                (MemberVO) session.getAttribute(
                        "loginMember"
                );

        memberService.updateMemberOtt(
                loginMember.getMemberNo(),
                ottList
        );

        redirectAttributes.addFlashAttribute(
                "message",
                "OTT 정보가 수정되었습니다."
        );

        return "redirect:/member/mypage";
    }

    @PostMapping("/delete")
    public String deleteMember(
            HttpSession session) {

        MemberVO loginMember =
                (MemberVO) session.getAttribute(
                        "loginMember"
                );

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        memberService.deleteMember(
                loginMember.getMemberNo()
        );

        session.invalidate();

        return "redirect:/";
    }

    private String redirectWithError(
            RedirectAttributes redirectAttributes,
            String message) {

        redirectAttributes.addFlashAttribute(
                "errorMessage",
                message
        );

        redirectAttributes.addFlashAttribute(
                "openMemberModal",
                true
        );

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

        MemberVO memberVO =
                new MemberVO();

        memberVO.setMemberName(
                memberName
        );

        memberVO.setEmail(
                email
        );

        MemberVO result =
                memberService.findId(
                        memberVO
                );

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

        MemberVO memberVO =
                new MemberVO();

        memberVO.setMemberId(
                memberId
        );

        memberVO.setMemberName(
                memberName
        );

        memberVO.setEmail(
                email
        );

        MemberVO result =
                memberService.findPw(
                        memberVO
                );

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

    /**
     * 요청의 Referer 주소에서
     * 현재 애플리케이션 내부 경로를 추출한다.
     *
     * 예:
     * http://localhost:8080/oditji/recommend
     * → /recommend
     */
    private String extractPreviousUrl(
            HttpServletRequest request) {

        String referer =
                request.getHeader("Referer");

        return normalizeRedirectUrl(
                referer,
                request
        );
    }

    /**
     * 전체 URL 또는 애플리케이션 내부 경로를
     * redirect에서 사용할 수 있는 내부 경로로 변환한다.
     */
    private String normalizeRedirectUrl(
            String redirectUrl,
            HttpServletRequest request) {

        if (redirectUrl == null
                || redirectUrl.isBlank()) {

            return null;
        }

        try {

            URI uri =
                    URI.create(
                            redirectUrl.trim()
                    );

            String path;

            /*
             * http://localhost:8080/oditji/recommend 같은
             * 전체 URL인 경우 동일 호스트인지 확인한다.
             */
            if (uri.isAbsolute()) {

                if (!isSameOrigin(
                        uri,
                        request)) {

                    return null;
                }

                path = uri.getRawPath();

            } else {

                path = uri.getRawPath();
            }

            if (path == null
                    || path.isBlank()) {

                return null;
            }

            String contextPath =
                    request.getContextPath();

            /*
             * /oditji/recommend
             * → /recommend
             */
            if (contextPath != null
                    && !contextPath.isBlank()
                    && path.startsWith(contextPath)) {

                path = path.substring(
                        contextPath.length()
                );
            }

            if (path.isBlank()) {
                path = "/";
            }

            if (!path.startsWith("/")) {
                path = "/" + path;
            }

            String query =
                    uri.getRawQuery();

            if (query != null
                    && !query.isBlank()) {

                path = path
                        + "?"
                        + query;
            }

            return path;

        } catch (Exception e) {

            return null;
        }
    }

    /**
     * 외부 사이트로 리다이렉트되는 것을 방지한다.
     */
    private boolean isSameOrigin(
            URI uri,
            HttpServletRequest request) {

        if (uri.getHost() == null) {
            return false;
        }

        if (!uri.getHost().equalsIgnoreCase(
                request.getServerName())) {

            return false;
        }

        int requestPort =
                request.getServerPort();

        int uriPort =
                uri.getPort();

        /*
         * URL에서 포트가 생략된 경우
         * 스킴의 기본 포트를 적용한다.
         */
        if (uriPort == -1) {

            if ("https".equalsIgnoreCase(
                    uri.getScheme())) {

                uriPort = 443;

            } else {

                uriPort = 80;
            }
        }

        return requestPort == uriPort;
    }

    /**
     * 로그인, 로그아웃, 회원가입 등으로 다시 이동하는
     * 반복 리다이렉트를 방지한다.
     */
    private boolean isUsableRedirectUrl(
            String redirectUrl,
            HttpServletRequest request) {

        if (redirectUrl == null
                || redirectUrl.isBlank()) {

            return false;
        }

        String path =
                redirectUrl;

        int queryIndex =
                path.indexOf("?");

        if (queryIndex >= 0) {
            path = path.substring(
                    0,
                    queryIndex
            );
        }

        if ("/member/login".equals(path)
                || "/member/logout".equals(path)
                || "/member/join".equals(path)
                || "/member/findId".equals(path)
                || "/member/findPw".equals(path)) {

            return false;
        }

        return path.startsWith("/");
    }
}