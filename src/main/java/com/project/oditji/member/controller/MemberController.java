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

import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.support.WithdrawPolicy;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.PlatformVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member")
public class MemberController {

        private static final String LOGIN_REDIRECT_SESSION_KEY = "redirectAfterLogin";

        private final MemberService memberService;
        private final MemberPlatformService memberPlatformService;

        public MemberController(
                        MemberService memberService,
                        MemberPlatformService memberPlatformService) {

                this.memberService = memberService;
                this.memberPlatformService = memberPlatformService;
        }

        @GetMapping("/join")
        public String joinForm() {
                return "member/join";
        }

        @PostMapping("/join")
        public String join(
                        MemberVO memberVO,
                        @RequestParam(value = "ottList", required = false) List<String> ottList,
                        @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile,
                        Model model,
                        RedirectAttributes redirectAttributes) {

                System.out.println(
                                "===== 회원가입 요청 들어옴 =====");

                System.out.println(
                                "memberId = "
                                                + memberVO.getMemberId());

                System.out.println(
                                "memberName = "
                                                + memberVO.getMemberName());

                System.out.println(
                                "nickname = "
                                                + memberVO.getNickname());

                System.out.println(
                                "email = "
                                                + memberVO.getEmail());

                System.out.println(
                                "ottList = "
                                                + ottList);

                try {

                        if (profileImageFile != null
                                        && !profileImageFile.isEmpty()) {

                                String uploadDir = "C:/oditji/uploads/profile/";

                                File dir = new File(uploadDir);

                                if (!dir.exists()) {
                                        dir.mkdirs();
                                }

                                String originalFileName = profileImageFile.getOriginalFilename();

                                String ext = "";

                                if (originalFileName != null
                                                && originalFileName.contains(".")) {

                                        ext = originalFileName.substring(
                                                        originalFileName.lastIndexOf("."));
                                }

                                String saveFileName = UUID.randomUUID().toString()
                                                + ext;

                                File saveFile = new File(
                                                uploadDir
                                                                + saveFileName);

                                profileImageFile.transferTo(
                                                saveFile);

                                /*
                                 * DB에는 경로가 아닌
                                 * 저장된 파일명만 저장한다.
                                 */
                                memberVO.setProfileImage(
                                                saveFileName);

                                System.out.println(
                                                "저장된 프로필 파일명 = "
                                                                + saveFileName);
                        }

                        memberService.joinMember(
                                        memberVO,
                                        ottList);

                        redirectAttributes.addFlashAttribute(
                                        "message",
                                        "회원가입이 완료되었습니다.");

                        return "redirect:/member/login";

                } catch (IllegalArgumentException e) {

                        e.printStackTrace();

                        model.addAttribute(
                                        "errorMessage",
                                        e.getMessage());

                        return "member/join";

                } catch (Exception e) {

                        e.printStackTrace();

                        model.addAttribute(
                                        "errorMessage",
                                        "회원가입 처리 중 오류가 발생했습니다: "
                                                        + e.getClass().getName()
                                                        + " / "
                                                        + e.getMessage());

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
                        @RequestParam(value = "redirect", required = false) String redirect,
                        HttpServletRequest request,
                        HttpSession session) {

                /*
                 * URL 파라미터로 돌아갈 주소가 전달되었다면
                 * 해당 주소를 우선 사용한다.
                 */
                String redirectUrl = normalizeRedirectUrl(
                                redirect,
                                request);

                /*
                 * redirect 파라미터가 없다면
                 * Referer 헤더에서 이전 페이지를 확인한다.
                 */
                if (redirectUrl == null) {

                        redirectUrl = extractPreviousUrl(
                                        request);
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
                                        redirectUrl);
                }

                return "member/login";
        }

        /**
         * 로그인 처리
         *
         * - 정지(BLOCKED) 회원: 안내 팝업만 노출한다.
         * - 탈퇴(WITHDRAWN) 회원: ID/PW가 이미 일치했다는 것 자체가 본인 인증이므로,
         * 세션에 복구 대상 회원번호를 저장해두고 로그인 화면에서 복구 모달을 띄운다.
         */
        @PostMapping("/login")
        public String login(
                        MemberVO memberVO,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

                try {

                        MemberVO loginMember = memberService.loginMember(
                                        memberVO);

                        if (loginMember == null) {

                                redirectAttributes.addFlashAttribute(
                                                "message",
                                                "아이디 또는 비밀번호가 일치하지 않습니다.");

                                return "redirect:/member/login";
                        }

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

                        String displayName = loginMember.getMemberName();

                        if (displayName == null
                                        || displayName.isBlank()) {

                                displayName = loginMember.getNickname();
                        }

                        if (displayName == null
                                        || displayName.isBlank()) {

                                displayName = "회원";
                        }

                        session.setAttribute(
                                        "loginDisplayName",
                                        displayName);

                        String redirectUrl = (String) session.getAttribute(
                                        LOGIN_REDIRECT_SESSION_KEY);

                        session.removeAttribute(
                                        LOGIN_REDIRECT_SESSION_KEY);

                        if (redirectUrl == null
                                        || redirectUrl.isBlank()) {

                                return "redirect:/";
                        }

                        return "redirect:"
                                        + redirectUrl;

                } catch (MemberBlockedException e) {

                        redirectAttributes.addFlashAttribute(
                                        "blockedMessage",
                                        e.getMessage());

                        return "redirect:/member/login";

                } catch (MemberWithdrawnException e) {

                        /*
                         * DAO의 loginMember 조회는 MEMBER_ID + MEMBER_PW가 일치해야만
                         * row를 반환하므로, 이 예외가 발생한 시점에는 이미 비밀번호 인증이
                         * 끝난 상태다. 따라서 복구 시 비밀번호를 다시 묻지 않고
                         * 세션에 회원번호만 저장해 /member/restore에서 신뢰한다.
                         */
                        session.setAttribute(
                                        "restoreMemberNo",
                                        e.getMemberNo());

                        session.setAttribute(
                                        "restoreProvider",
                                        "LOCAL");

                        redirectAttributes.addFlashAttribute(
                                        "withdrawnMessage",
                                        WithdrawPolicy.buildWithdrawnMessage(
                                                        e.getWithdrawnAt()));

                        return "redirect:/member/login";

                } catch (IllegalStateException e) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        e.getMessage());

                        return "redirect:/member/login";
                }
        }

        /**
         * 탈퇴 회원 복구 처리
         *
         * 로그인(/member/login) 또는 카카오 콜백(/member/kakao/callback)에서
         * 이미 본인 인증이 끝난 뒤에 세션에 심어둔 restoreMemberNo만 신뢰해서 처리한다.
         * 클라이언트가 임의의 회원번호를 파라미터로 넘겨도 무시되므로,
         * 세션을 탈취하지 않는 한 다른 사람의 계정을 복구할 수 없다.
         */
        @PostMapping("/restore")
        public String restoreMember(
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

                Object restoreNoObj = session.getAttribute(
                                "restoreMemberNo");

                if (restoreNoObj == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "복구 요청 정보가 없습니다. 다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                Long memberNo = ((Number) restoreNoObj).longValue();

                try {

                        memberService.restoreMember(
                                        memberNo);

                        session.removeAttribute(
                                        "restoreMemberNo");

                        session.removeAttribute(
                                        "restoreProvider");

                        redirectAttributes.addFlashAttribute(
                                        "restoredMessage",
                                        "계정이 복구되었습니다. 다시 로그인해주세요.");

                } catch (IllegalStateException
                                | IllegalArgumentException e) {

                        session.removeAttribute(
                                        "restoreMemberNo");

                        session.removeAttribute(
                                        "restoreProvider");

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        e.getMessage());
                }

                return "redirect:/member/login";
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

                String redirectUrl = extractPreviousUrl(
                                request);

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
                        @RequestParam("memberId") String memberId) {

                boolean duplicate = memberService.isDuplicateId(
                                memberId);

                if (duplicate) {
                        return "N";
                }

                return "Y";
        }

        @GetMapping("/checkNickname")
        @ResponseBody
        public String checkNickname(
                        @RequestParam("nickname") String nickname) {

                System.out.println(
                                "===== 닉네임 중복확인 요청 =====");

                System.out.println(
                                "nickname = "
                                                + nickname);

                boolean duplicate = memberService.isDuplicateNickname(
                                nickname);

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

                /*
                 * 로그인 회원 조회
                 */
                MemberVO loginMember = (MemberVO) session.getAttribute(
                                "loginMember");

                /*
                 * 로그인하지 않은 사용자가 마이페이지에 접근하면
                 * 로그인 화면으로 이동한다.
                 */
                if (loginMember == null
                                || loginMember.getMemberNo() == null) {

                        return "redirect:/member/login";
                }

                /*
                 * SNS 로그인 회원 여부
                 */
                boolean socialMember = session.getAttribute(
                                "loginProvider") != null;

                model.addAttribute(
                                "socialMember",
                                socialMember);

                /*
                 * 로그인 회원이 선택한 활성 OTT 목록 조회
                 */
                List<PlatformVO> ottList = memberPlatformService.findMemberPlatformList(
                                loginMember.getMemberNo());

                /*
                 * JSP에서 ${ottList}로 사용할 수 있도록 전달
                 */
                model.addAttribute(
                                "ottList",
                                ottList);

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

                MemberVO loginMember = (MemberVO) session.getAttribute(
                                "loginMember");

                /*
                 * 로그인하지 않은 상태에서 수정 요청이 들어오는 것을 방지한다.
                 */
                if (loginMember == null
                                || loginMember.getMemberNo() == null) {

                        return "redirect:/member/login";
                }

                /*
                 * 클라이언트가 전달한 memberNo를 신뢰하지 않고
                 * 로그인 세션의 회원번호를 사용한다.
                 */
                memberVO.setMemberNo(
                                loginMember.getMemberNo());

                /*
                 * 비밀번호 변경
                 */
                if (newPw != null
                                && !newPw.isBlank()) {

                        if (currentPw == null
                                        || currentPw.isBlank()) {

                                return redirectWithError(
                                                redirectAttributes,
                                                "현재 비밀번호를 입력해주세요.");
                        }

                        if (!newPw.equals(
                                        newPwCheck)) {

                                return redirectWithError(
                                                redirectAttributes,
                                                "새 비밀번호가 일치하지 않습니다.");
                        }

                        if (!memberService.checkPassword(
                                        loginMember.getMemberNo(),
                                        currentPw)) {

                                return redirectWithError(
                                                redirectAttributes,
                                                "현재 비밀번호가 일치하지 않습니다.");
                        }

                        memberVO.setMemberPw(
                                        newPw);
                }

                /*
                 * 프로필 이미지
                 */
                if (profileImageFile != null
                                && !profileImageFile.isEmpty()) {

                        try {

                                String uploadDir = "C:/oditji/uploads/profile/";

                                File dir = new File(uploadDir);

                                if (!dir.exists()) {
                                        dir.mkdirs();
                                }

                                String original = profileImageFile.getOriginalFilename();

                                String ext = "";

                                if (original != null
                                                && original.contains(".")) {

                                        ext = original.substring(
                                                        original.lastIndexOf("."));
                                }

                                String saveName = UUID.randomUUID()
                                                + ext;

                                profileImageFile.transferTo(
                                                new File(
                                                                uploadDir
                                                                                + saveName));

                                memberVO.setProfileImage(
                                                saveName);

                        } catch (Exception e) {

                                return redirectWithError(
                                                redirectAttributes,
                                                "이미지 업로드에 실패했습니다.");
                        }
                }

                /*
                 * 회원정보 수정
                 */
                memberService.updateMember(
                                memberVO);

                MemberVO updated = memberService.getMemberByNo(
                                memberVO.getMemberNo());

                session.setAttribute(
                                "loginMember",
                                updated);

                /*
                 * 헤더 등에서 개별 세션값을 사용하고 있으므로
                 * 수정된 닉네임도 함께 갱신한다.
                 */
                session.setAttribute(
                                "nickname",
                                updated.getNickname());

                String displayName = updated.getMemberName();

                if (displayName == null
                                || displayName.isBlank()) {

                        displayName = updated.getNickname();
                }

                if (displayName == null
                                || displayName.isBlank()) {

                        displayName = "회원";
                }

                session.setAttribute(
                                "loginDisplayName",
                                displayName);

                redirectAttributes.addFlashAttribute(
                                "message",
                                "회원정보가 수정되었습니다.");

                return "redirect:/member/mypage";
        }

        /**
         * 회원정보 수정용 닉네임 중복확인
         *
         * 브라우저에서 전달하는 memberNo는 값이 비어 있거나
         * 다른 회원번호로 조작될 수 있으므로 사용하지 않는다.
         *
         * 로그인 세션의 loginMember에서 현재 회원번호를 꺼내
         * 자기 자신을 제외한 닉네임 중복 여부를 검사한다.
         */
        @GetMapping("/checkUpdateNickname")
        @ResponseBody
        public String checkUpdateNickname(
                        @RequestParam("nickname") String nickname,
                        HttpSession session) {

                System.out.println(
                                "===== 회원정보 수정 닉네임 중복확인 요청 =====");

                System.out.println(
                                "nickname = "
                                                + nickname);

                /*
                 * 현재 로그인한 회원 정보를 세션에서 조회한다.
                 */
                MemberVO loginMember = (MemberVO) session.getAttribute(
                                "loginMember");

                /*
                 * 로그인 세션이 없거나 회원번호가 없다면
                 * 정상적인 중복확인을 진행할 수 없다.
                 */
                if (loginMember == null
                                || loginMember.getMemberNo() == null) {

                        System.out.println(
                                        "닉네임 중복확인 실패: 로그인 회원 정보 없음");

                        return "N";
                }

                /*
                 * 공백만 입력된 닉네임은 검사하지 않는다.
                 */
                if (nickname == null
                                || nickname.isBlank()) {

                        System.out.println(
                                        "닉네임 중복확인 실패: 닉네임 값 없음");

                        return "N";
                }

                String trimmedNickname = nickname.trim();

                Long memberNo = loginMember.getMemberNo();

                System.out.println(
                                "로그인 회원번호 = "
                                                + memberNo);

                /*
                 * 현재 로그인한 회원을 제외하고
                 * 같은 닉네임을 사용하는 회원이 있는지 검사한다.
                 */
                boolean available = memberService.checkUpdateNickname(
                                memberNo,
                                trimmedNickname);

                System.out.println(
                                "닉네임 사용 가능 여부 = "
                                                + available);

                return available
                                ? "Y"
                                : "N";
        }

        @GetMapping("/checkPassword")
        @ResponseBody
        public String checkPassword(
                        @RequestParam("password") String password,
                        HttpSession session) {

                MemberVO loginMember = (MemberVO) session.getAttribute(
                                "loginMember");

                if (loginMember == null
                                || loginMember.getMemberNo() == null) {

                        return "N";
                }

                boolean result = memberService.checkPassword(
                                loginMember.getMemberNo(),
                                password);

                return result
                                ? "Y"
                                : "N";
        }

        @PostMapping("/updateOtt")
        public String updateOtt(
                        @RequestParam(value = "ottList", required = false) List<String> ottList,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

                MemberVO loginMember = (MemberVO) session.getAttribute(
                                "loginMember");

                if (loginMember == null
                                || loginMember.getMemberNo() == null) {

                        return "redirect:/member/login";
                }

                memberService.updateMemberOtt(
                                loginMember.getMemberNo(),
                                ottList);

                redirectAttributes.addFlashAttribute(
                                "message",
                                "OTT 정보가 수정되었습니다.");

                return "redirect:/member/mypage";
        }

        @PostMapping("/withdraw")
        public String withdrawMember(
                        HttpSession session) {

                MemberVO loginMember = (MemberVO) session.getAttribute(
                                "loginMember");

                if (loginMember == null
                                || loginMember.getMemberNo() == null) {

                        return "redirect:/member/login";
                }

                memberService.withdrawMember(
                                loginMember.getMemberNo());

                session.invalidate();

                return "redirect:/";
        }

        private String redirectWithError(
                        RedirectAttributes redirectAttributes,
                        String message) {

                redirectAttributes.addFlashAttribute(
                                "errorMessage",
                                message);

                redirectAttributes.addFlashAttribute(
                                "openMemberModal",
                                true);

                return "redirect:/member/mypage";
        }

        @GetMapping("/findId")
        public String findId() {
                return "member/findId";
        }

        @PostMapping("/findId")
        public String findIdPost(
                        @RequestParam("memberName") String memberName,
                        @RequestParam("email") String email,
                        Model model) {

                MemberVO memberVO = new MemberVO();

                memberVO.setMemberName(
                                memberName);

                memberVO.setEmail(
                                email);

                MemberVO result = memberService.findId(
                                memberVO);

                if (result != null) {

                        model.addAttribute(
                                        "findIdResult",
                                        result.getMemberId());

                } else {

                        model.addAttribute(
                                        "errorMessage",
                                        "일치하는 회원 정보가 없습니다.");
                }

                return "member/findId";
        }

        @GetMapping("/findPw")
        public String findPw() {
                return "member/findPw";
        }

        @PostMapping("/findPw")
        public String findPwPost(
                        @RequestParam("memberId") String memberId,
                        @RequestParam("memberName") String memberName,
                        @RequestParam("email") String email,
                        Model model) {

                MemberVO memberVO = new MemberVO();

                memberVO.setMemberId(
                                memberId);

                memberVO.setMemberName(
                                memberName);

                memberVO.setEmail(
                                email);

                MemberVO result = memberService.findPw(
                                memberVO);

                if (result != null) {

                        model.addAttribute(
                                        "findPwResult",
                                        result.getMemberPw());

                } else {

                        model.addAttribute(
                                        "errorMessage",
                                        "일치하는 회원 정보가 없습니다.");
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

                String referer = request.getHeader(
                                "Referer");

                return normalizeRedirectUrl(
                                referer,
                                request);
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

                        URI uri = URI.create(
                                        redirectUrl.trim());

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

                        String contextPath = request.getContextPath();

                        /*
                         * /oditji/recommend
                         * → /recommend
                         */
                        if (contextPath != null
                                        && !contextPath.isBlank()
                                        && path.startsWith(
                                                        contextPath)) {

                                path = path.substring(
                                                contextPath.length());
                        }

                        if (path.isBlank()) {
                                path = "/";
                        }

                        if (!path.startsWith("/")) {
                                path = "/"
                                                + path;
                        }

                        String query = uri.getRawQuery();

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

                int requestPort = request.getServerPort();

                int uriPort = uri.getPort();

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

                String path = redirectUrl;

                int queryIndex = path.indexOf("?");

                if (queryIndex >= 0) {

                        path = path.substring(
                                        0,
                                        queryIndex);
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