package com.project.oditji.member.controller;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.support.WithdrawPolicy;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.PlatformVO;
import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.service.NtsBusinessService;
import com.project.oditji.business.vo.NtsBusinessVerifyVO;
import com.project.oditji.business.vo.BusinessVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member")
public class MemberController {

        private static final String LOGIN_REDIRECT_SESSION_KEY = "redirectAfterLogin";
        private static final Logger log = LoggerFactory.getLogger(MemberController.class);

        private final MemberService memberService;
        private final MemberPlatformService memberPlatformService;
        private final BusinessService businessService;
        private final NtsBusinessService ntsBusinessService;

        public MemberController(
                        MemberService memberService,
                        MemberPlatformService memberPlatformService,
                        BusinessService businessService,
                        NtsBusinessService ntsBusinessService) {

                this.memberService = memberService;
                this.memberPlatformService = memberPlatformService;
                this.businessService = businessService;
                this.ntsBusinessService = ntsBusinessService;
        }

        @GetMapping("/join")
        public String joinForm(Model model) {

                // OTT 선택 영역에 로고/목록을 표시하기 위한 플랫폼 목록 
                model.addAttribute("platformList", memberPlatformService.findPlatformList());

                return "member/join";
        }

        /*
         * =========================================================
         * 사업자등록번호 중복 확인
         *
         * Y : 등록 가능
         * N : 이미 등록된 번호
         * =========================================================
         */
        @GetMapping("/checkBusinessNumber")
        @ResponseBody
        public String checkBusinessNumber(@RequestParam("businessNumber") String businessNumber) {
                boolean available = businessService.isBusinessNumberAvailable(businessNumber);

                return available ? "Y" : "N";
        }

        // 국세청 사업자등록정보 진위확인
        @PostMapping("/verifyBusiness")
        @ResponseBody
        public NtsBusinessVerifyVO verifyBusiness(
                        @RequestParam("businessNumber") String businessNumber,
                        @RequestParam("representativeName") String representativeName,
                        @RequestParam("openDate") String openDate) {

                return ntsBusinessService.verifyBusiness(businessNumber, representativeName, openDate);
        }

        @PostMapping("/join")
        public String join(MemberVO memberVO, BusinessVO businessVO,
                        @RequestParam(value = "joinType", defaultValue = "USER") String joinType,
                        @RequestParam(value = "ottList", required = false) List<String> ottList,
                        @RequestParam(value = "noOtt", defaultValue = "N") String noOtt,
                        @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile,
                        @RequestParam(value = "licenseFile", required = false) MultipartFile licenseFile,

                        Model model,
                        RedirectAttributes redirectAttributes) {

                System.out.println("===== 회원가입 요청 들어옴 =====");
                System.out.println("memberId = " + memberVO.getMemberId());
                System.out.println("memberName = " + memberVO.getMemberName());
                System.out.println("nickname = " + memberVO.getNickname());
                System.out.println("email = " + memberVO.getEmail());
                System.out.println("ottList = " + ottList);

                /*
                 * 가입 실패로 join.jsp가 다시 렌더링되는 경우에도
                 * OTT 선택 영역(로고/목록)이 그대로 보이도록 미리 담아둔다.
                 */
                model.addAttribute(
                                "platformList",
                                memberPlatformService.findPlatformList());
                try {
                        if (profileImageFile != null && !profileImageFile.isEmpty()) {
                                String uploadDir = "C:/oditji/uploads/profile/";
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

                                //DB에는 경로가 아닌 저장된 파일명만 저장한다.
                                
                                memberVO.setProfileImage(saveFileName);

                                System.out.println("저장된 프로필 파일명 = " + saveFileName);
                        }

                        // 회원 유형별 가입 처리
                        if ("BUSINESS".equalsIgnoreCase(joinType)) {

                                /*
                                 * 1. 국세청 사업자 정보 서버 재검증
                                 * 브라우저의 businessVerified 값은 조작 가능하므로
                                 * 실제 가입 시 서버에서 다시 검증한다.
                                 */
                                NtsBusinessVerifyVO verifyResult = ntsBusinessService.verifyBusiness(
                                                businessVO.getBusinessNumber(), businessVO.getRepresentativeName(),
                                                businessVO.getOpenDate());

                                if (verifyResult == null || !verifyResult.isValid()) {
                                        String message = verifyResult != null && verifyResult.getMessage() != null
                                                        ? verifyResult.getMessage()
                                                        : "사업자 정보를 확인할 수 없습니다.";
                                        throw new IllegalArgumentException(message);
                                }

                                // 2. 계속사업자인지 최종 확인 
                                if (!"계속사업자".equals(verifyResult.getBusinessStatus())) {
                                        throw new IllegalArgumentException("계속사업자만 사업자 회원가입이 가능합니다.");
                                }

                                /*
                                 * 국세청에서 실제 확인한 상태값을 저장한다.
                                 * 클라이언트에서 전달된 값은 사용하지 않는다.
                                 */
                                businessVO.setNtsBusinessStatus(verifyResult.getBusinessStatus());

                                // 3. 사업자등록증 필수 확인
                                if (licenseFile == null || licenseFile.isEmpty()) {
                                        throw new IllegalArgumentException("사업자등록증을 첨부해주세요.");
                                }

                                // 4. 사업자등록증 확장자 확인
                                String originalLicenseName = licenseFile.getOriginalFilename();

                                if (originalLicenseName == null || !originalLicenseName.contains(".")) {
                                        throw new IllegalArgumentException("사업자등록증 파일 형식이 올바르지 않습니다.");
                                }

                               String licenseExt = originalLicenseName.substring(originalLicenseName.lastIndexOf("."))
                                                        .toLowerCase(Locale.ROOT);

                                if (!".pdf".equals(licenseExt)
                                                        && !".jpg".equals(licenseExt)
                                                        && !".jpeg".equals(licenseExt)
                                                        && !".png".equals(licenseExt)) {
                                        throw new IllegalArgumentException(
                                                        "사업자등록증은 PDF, JPG, JPEG, PNG 파일만 등록할 수 있습니다.");
                                }

                                // 5. 사업자등록증 저장
                                String licenseUploadDir = "C:/oditji/uploads/business-license/";
                                File licenseDir = new File(licenseUploadDir);

                                if (!licenseDir.exists()) {
                                        boolean created = licenseDir.mkdirs();

                                        if (!created && !licenseDir.exists()) {
                                                throw new IllegalStateException("사업자등록증 저장 폴더를 생성할 수 없습니다.");
                                        }
                                }

                                String savedLicenseName = UUID.randomUUID().toString() + licenseExt;
                                File savedLicenseFile = new File(licenseUploadDir + savedLicenseName);
                                licenseFile.transferTo(savedLicenseFile);

                                // DB에는 UUID로 저장한 파일명만 저장
                                businessVO.setLicenseFilePath(savedLicenseName);

                                // 6. MEMBER + BUSINESS 저장
                                memberService.joinBusinessMember(memberVO, businessVO);

                                redirectAttributes.addFlashAttribute("message",
                                                "사업자 회원가입 신청이 완료되었습니다. 관리자 승인 후 이용할 수 있습니다.");

                        } else {
                                // 일반회원은 기존 가입 로직 그대로 사용
                                memberService.joinMember(memberVO, ottList, noOtt);
                                redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다.");
                        }

                        return "redirect:/member/login";

                } catch (IllegalArgumentException e) {
                        if (log.isWarnEnabled()) {
                        log.warn("회원가입 입력값 검증 실패: {}", e.getMessage());
                        }
                        model.addAttribute("errorMessage", e.getMessage());

                        return "member/join";

                } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                        log.error("회원가입 처리 중 오류 발생", e);
                        }
                        model.addAttribute("errorMessage", "회원가입 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

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
                String redirectUrl = normalizeRedirectUrl(redirect, request);

                /*
                 * redirect 파라미터가 없다면
                 * Referer 헤더에서 이전 페이지를 확인한다.
                 */
                if (redirectUrl == null) {
                        redirectUrl = extractPreviousUrl(request);
                }

                /*
                 * 로그인, 회원가입 등 회원 관련 화면 자체는
                 * 복귀 주소로 저장하지 않는다.
                 */
               if (isUsableRedirectUrl(redirectUrl)) {
                        session.setAttribute(LOGIN_REDIRECT_SESSION_KEY, redirectUrl);
                        } else {
                        session.removeAttribute(LOGIN_REDIRECT_SESSION_KEY);
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
        public String login(MemberVO memberVO, HttpSession session, RedirectAttributes redirectAttributes) {

                try {
                        MemberVO loginMember = memberService.loginMember(memberVO);
                        if (loginMember == null) {

                                redirectAttributes.addFlashAttribute("message", "아이디 또는 비밀번호가 틀렸습니다.");

                                /*
                                 * 로그인에 실패해도 사용자가 입력한 아이디는 유지한다.
                                 * 비밀번호는 보안상 다시 전달하지 않는다.
                                 */
                                redirectAttributes.addFlashAttribute("loginMemberId", memberVO.getMemberId());

                                return "redirect:/member/login";
                        }

                        /*
                         * =========================================================
                         * 사업자 회원 승인 상태 확인
                         *
                         * BUSINESS 행이 존재하면 사업자 회원으로 판단한다.
                         * 관리자 승인(APPROVED) 전에는 로그인시키지 않는다.
                         * =========================================================
                         */
                        BusinessVO business = businessService.getBusinessByMemberNo(loginMember.getMemberNo());

                        if (business != null) {
                                String businessStatus = business.getStatus();

                                // 승인 대기 
                                if ("WAITING".equals(businessStatus)) {
                                        redirectAttributes.addFlashAttribute("message", "관리자 승인 대기 중인 사업자 계정입니다.");

                                        return "redirect:/member/login";
                                }

                                // 승인 거절 
                                if ("REJECTED".equals(businessStatus)) {
                                        String message = "사업자 승인이 거절되었습니다.";

                                        if (business.getRejectReason() != null && !business.getRejectReason().isBlank()) {
                                                message += "\n사유: " + business.getRejectReason();
                                        }
                                        redirectAttributes.addFlashAttribute("message", message);

                                        return "redirect:/member/login";
                                }

                                // 승인된 사업자만 로그인 허용
                                if (!"APPROVED".equals(businessStatus)) {
                                        redirectAttributes.addFlashAttribute("message", "현재 사업자 계정 상태로는 로그인할 수 없습니다.");

                                        return "redirect:/member/login";
                                }
                        }

                        session.setAttribute("loginMember", loginMember);
                        session.setAttribute("memberNo", loginMember.getMemberNo());
                        session.setAttribute("memberId", loginMember.getMemberId());
                        session.setAttribute("memberName", loginMember.getMemberName());
                        session.setAttribute("nickname", loginMember.getNickname());
                        session.setAttribute("role", loginMember.getRole());

                        if (business != null) {
                                session.setAttribute("businessNo", business.getBusinessNo());
                                session.setAttribute("businessName", business.getBusinessName());
                                session.setAttribute("businessStatus", business.getStatus());
                        }
                        String displayName = loginMember.getMemberName();
                        if (business != null) {
                                displayName = business.getBusinessName();
                        } else {
                                if (displayName == null || displayName.isBlank()) {
                                        displayName = loginMember.getNickname();
                                }
                        }
                        if (displayName == null || displayName.isBlank()) {
                                displayName = "회원";
                        }

                        session.setAttribute("loginDisplayName", displayName);
                        String redirectUrl = (String) session.getAttribute(LOGIN_REDIRECT_SESSION_KEY);
                        session.removeAttribute(LOGIN_REDIRECT_SESSION_KEY);

                        if (redirectUrl == null || redirectUrl.isBlank()) {
                                return "redirect:/";
                        }

                        return "redirect:" + redirectUrl;

                } catch (MemberBlockedException e) {
                        redirectAttributes.addFlashAttribute("blockedMessage", e.getMessage());

                        return "redirect:/member/login";

                } catch (MemberWithdrawnException e) {

                        /*
                         * DAO의 loginMember 조회는 MEMBER_ID + MEMBER_PW가 일치해야만
                         * row를 반환하므로, 이 예외가 발생한 시점에는 이미 비밀번호 인증이
                         * 끝난 상태다. 따라서 복구 시 비밀번호를 다시 묻지 않고
                         * 세션에 회원번호만 저장해 /member/restore에서 신뢰한다.
                         */
                        session.setAttribute("restoreMemberNo", e.getMemberNo());
                        session.setAttribute("restoreProvider", "LOCAL");
                        redirectAttributes.addFlashAttribute("withdrawnMessage", WithdrawPolicy.buildWithdrawnMessage(e.getWithdrawnAt()));

                        return "redirect:/member/login";

                } catch (IllegalStateException e) {
                        log.error("로그인 처리 중 오류 발생", e);
                        redirectAttributes.addFlashAttribute("errorMessage", "로그인 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

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
        public String restoreMember(HttpSession session, RedirectAttributes redirectAttributes) {

                Object restoreNoObj = session.getAttribute("restoreMemberNo");
                if (restoreNoObj == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "복구 요청 정보가 없습니다. 다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                Long memberNo = ((Number) restoreNoObj).longValue();

                try {

                        memberService.restoreMember(memberNo);
                        session.removeAttribute("restoreMemberNo");
                        session.removeAttribute("restoreProvider");

                        redirectAttributes.addFlashAttribute("restoredMessage", "계정이 복구되었습니다. 다시 로그인해주세요.");

                } catch (IllegalStateException | IllegalArgumentException e) {

                        session.removeAttribute("restoreMemberNo");
                        session.removeAttribute("restoreProvider");

                        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                }

                return "redirect:/member/login";
        }

        /**
         * 로그아웃 처리
         * 로그아웃 링크를 누른 현재 페이지의 주소를 저장한 후
         * 세션을 무효화하고 해당 페이지로 돌아간다.
         */
        @GetMapping("/logout")
        public String logout(HttpServletRequest request, HttpSession session) {

                String redirectUrl = extractPreviousUrl(request);

                //세션 무효화 전에 이전 주소를 지역변수에 저장한다.
                session.invalidate();

                if (!isUsableRedirectUrl(redirectUrl)) {

                        return "redirect:/";
                }

                return "redirect:" + redirectUrl;
        }

        @GetMapping("/checkId")
        @ResponseBody
        public String checkId(@RequestParam("memberId") String memberId) {

                boolean duplicate = memberService.isDuplicateId(memberId);

                if (duplicate) {
                        return "N";
                }

                return "Y";
        }

        /*
         * =========================================================
         * 이메일 중복확인
         *
         * Y : 사용 가능한 이메일
         * N : 이미 사용 중인 이메일
         * =========================================================
         */
        @GetMapping("/checkEmail")
        @ResponseBody
        public String checkEmail(
                        @RequestParam("email") String email) {
                boolean duplicate = memberService.isDuplicateEmail(email);

                if (duplicate) {
                        return "N";
                }

                return "Y";
        }

        @GetMapping("/checkNickname")
        @ResponseBody
        public String checkNickname(@RequestParam("nickname") String nickname) {

                System.out.println("===== 닉네임 중복확인 요청 =====");
                System.out.println("nickname = "+ nickname);
                boolean duplicate = memberService.isDuplicateNickname( nickname);

                if (duplicate) {
                        return "N";
                }

                return "Y";
        }

        // 마이페이지
        @GetMapping("/mypage")
        public String mypage(HttpSession session, Model model) {

                // 로그인 회원 조회 
                MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

                /*
                 * 로그인하지 않은 사용자가 마이페이지에 접근하면
                 * 로그인 화면으로 이동한다.
                 */
                if (loginMember == null || loginMember.getMemberNo() == null) {

                        return "redirect:/member/login";
                }

                // 관리자는 관리자 페이지로 이동
                if ("ADMIN".equals(loginMember.getRole())) {
                        return "redirect:/admin/main";
                }

                // 사업자 전용 페이지로 이동
                BusinessVO business = businessService.getBusinessByMemberNo(loginMember.getMemberNo());

                if (business != null) {
                        return "redirect:/business/main";
                }

                // SNS 로그인 회원 여부
                boolean socialMember = session.getAttribute("loginProvider") != null;
                model.addAttribute("socialMember", socialMember);

                // 로그인 회원이 선택한 활성 OTT 목록 조회
                List<PlatformVO> ottList = memberPlatformService.findMemberPlatformList(loginMember.getMemberNo());
                // JSP에서 ${ottList}로 사용할 수 있도록 전달 
                model.addAttribute("ottList", ottList);

                // OTT 정보 수정 모달에서 선택 가능한 전체 플랫폼 목록(로고 포함)
                model.addAttribute("platformList", memberPlatformService.findPlatformList());

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

                // 로그인하지 않은 상태에서 수정 요청이 들어오는 것을 방지 
                if (loginMember == null || loginMember.getMemberNo() == null) {

                        return "redirect:/member/login";
                }

                /*
                 * 클라이언트가 전달한 memberNo를 신뢰하지 않고
                 * 로그인 세션의 회원번호를 사용한다.
                 */
                memberVO.setMemberNo(loginMember.getMemberNo());

                // 비밀번호 변경
                if (newPw != null && !newPw.isBlank()) {
                        if (currentPw == null || currentPw.isBlank()) {

                                return redirectWithError(redirectAttributes, "현재 비밀번호를 입력해주세요.");
                        }

                        if (!newPw.equals(newPwCheck)) {

                                return redirectWithError(redirectAttributes, "새 비밀번호가 일치하지 않습니다.");
                        }

                        if (!memberService.checkPassword(loginMember.getMemberNo(),currentPw)) {

                                return redirectWithError(redirectAttributes, "현재 비밀번호가 일치하지 않습니다.");
                        }

                        memberVO.setMemberPw(newPw);
                }

                // 프로필 이미지
                if (profileImageFile != null && !profileImageFile.isEmpty()) {
                        try {
                                String uploadDir = "C:/oditji/uploads/profile/";
                                File dir = new File(uploadDir);

                                if (!dir.exists()) {
                                        dir.mkdirs();
                                }

                                String original = profileImageFile.getOriginalFilename();
                                String ext = "";

                                if (original != null && original.contains(".")) {
                                        ext = original.substring(original.lastIndexOf("."));
                                }

                                String saveName = UUID.randomUUID() + ext;
                                profileImageFile.transferTo(new File(uploadDir + saveName));
                                memberVO.setProfileImage(saveName);

                        } catch (Exception e) {

                                return redirectWithError(redirectAttributes, "이미지 업로드에 실패했습니다.");
                        }
                }

                /*
                 * 회원정보 수정
                 *
                 * 중복확인 버튼의 결과는 브라우저에서 조작할 수 있으므로
                 * Service에서 현재 회원을 제외한 닉네임/이메일 중복을 다시 검사한다.
                 */
                try {
                        memberService.updateMember(memberVO);

                } catch (IllegalArgumentException | IllegalStateException e) {

                        return redirectWithError(redirectAttributes, e.getMessage());
                }

                MemberVO updated = memberService.getMemberByNo(memberVO.getMemberNo());
                session.setAttribute("loginMember", updated);

                /*
                 * 헤더 등에서 개별 세션값을 사용하고 있으므로
                 * 수정된 닉네임도 함께 갱신한다.
                 */
                session.setAttribute("nickname", updated.getNickname());

                String displayName = updated.getMemberName();

                if (displayName == null || displayName.isBlank()) {
                        displayName = updated.getNickname();
                }

                if (displayName == null || displayName.isBlank()) {
                        displayName = "회원";
                }

                session.setAttribute("loginDisplayName", displayName);
                redirectAttributes.addFlashAttribute("message", "회원정보가 수정되었습니다.");

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
        public String checkUpdateNickname(@RequestParam("nickname") String nickname, HttpSession session) {

                System.out.println("===== 회원정보 수정 닉네임 중복확인 요청 =====");
                System.out.println("nickname = " + nickname);

                // 현재 로그인한 회원 정보를 세션에서 조회한다.
                MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

                /*
                 * 로그인 세션이 없거나 회원번호가 없다면
                 * 정상적인 중복확인을 진행할 수 없다.
                 */
                if (loginMember == null || loginMember.getMemberNo() == null) {
                        System.out.println("닉네임 중복확인 실패: 로그인 회원 정보 없음");

                        return "N";
                }

                // 공백만 입력된 닉네임은 검사하지 않는다.
                if (nickname == null || nickname.isBlank()) {
                        System.out.println("닉네임 중복확인 실패: 닉네임 값 없음");

                        return "N";
                }

                String trimmedNickname = nickname.trim();
                Long memberNo = loginMember.getMemberNo();
                System.out.println("로그인 회원번호 = " + memberNo);

                /*
                 * 현재 로그인한 회원을 제외하고
                 * 같은 닉네임을 사용하는 회원이 있는지 검사한다.
                 */
                boolean available = memberService.checkUpdateNickname(memberNo, trimmedNickname);

                System.out.println("닉네임 사용 가능 여부 = " + available);

                return available ? "Y" : "N";
        }

        /**
         * 회원정보 수정용 이메일 중복확인
         *
         * 로그인 세션의 회원번호를 사용하여 자기 자신을 제외한
         * 이메일 중복 여부를 확인한다.
         */
        @GetMapping("/checkUpdateEmail")
        @ResponseBody
        public String checkUpdateEmail(@RequestParam("email") String email, HttpSession session) {
                MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
                if (loginMember == null|| loginMember.getMemberNo() == null) {

                        return "N";
                }

                if (email == null || email.isBlank()) {

                        return "N";
                }

                String trimmedEmail = email.trim();
                boolean available = memberService.checkUpdateEmail(loginMember.getMemberNo(), trimmedEmail);

                return available ? "Y" : "N";
        }

        @PostMapping("/updateOtt")
        public String updateOtt(
                        @RequestParam(value = "ottList", required = false) List<String> ottList,
                        HttpSession session, RedirectAttributes redirectAttributes) {

                MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
                if (loginMember == null || loginMember.getMemberNo() == null) {

                        return "redirect:/member/login";
                }

                memberService.updateMemberOtt(loginMember.getMemberNo(), ottList);
                redirectAttributes.addFlashAttribute("message", "OTT 정보가 수정되었습니다.");

                return "redirect:/member/mypage";
        }

        @PostMapping("/withdraw")
        public String withdrawMember(HttpSession session) {

                MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
                if (loginMember == null || loginMember.getMemberNo() == null) {

                        return "redirect:/member/login";
                }

                memberService.withdrawMember(loginMember.getMemberNo());
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
                        @RequestParam("memberName") String memberName,
                        @RequestParam("email") String email,Model model) {

                MemberVO memberVO = new MemberVO();
                memberVO.setMemberName(memberName);
                memberVO.setEmail(email);

                MemberVO result = memberService.findId(memberVO);

                if (result != null) {
                        model.addAttribute("findIdResult", result.getMemberId());

                } else {
                        model.addAttribute("errorMessage", "일치하는 회원 정보가 없습니다.");
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
                        @RequestParam("email") String email, Model model) {

                MemberVO memberVO = new MemberVO();
                memberVO.setMemberId(memberId);
                memberVO.setMemberName(memberName);
                memberVO.setEmail(email);

                MemberVO result = memberService.findPw(memberVO);

                if (result != null) {
                        model.addAttribute("findPwResult", "회원 정보가 확인되었습니다. 비밀번호 재설정이 필요합니다.");

                } else {
                        model.addAttribute("errorMessage", "일치하는 회원 정보가 없습니다.");
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
        private String extractPreviousUrl(HttpServletRequest request) {
                String referer = request.getHeader("Referer");

                return normalizeRedirectUrl(referer, request);
        }

        /**
         * 전체 URL 또는 애플리케이션 내부 경로를
         * redirect에서 사용할 수 있는 내부 경로로 변환한다.
         */
        private String normalizeRedirectUrl(String redirectUrl, HttpServletRequest request) {
                if (redirectUrl == null || redirectUrl.isBlank()) {

                        return null;
                }
                try {
                        URI uri = URI.create(redirectUrl.trim());
                        String path;

                        /*
                         * http://localhost:8080/oditji/recommend 같은
                         * 전체 URL인 경우 동일 호스트인지 확인한다.
                         */
                        if (uri.isAbsolute()) {
                                if (!isSameOrigin(uri, request)) {

                                        return null;
                                }
                                path = uri.getRawPath();
                        } else {
                                path = uri.getRawPath();
                        }
                        if (path == null || path.isBlank()) {

                                return null;
                        }

                        String contextPath = request.getContextPath();

                        /*
                         * /oditji/recommend
                         * → /recommend
                         */
                        if (contextPath != null && !contextPath.isBlank()  && path.startsWith(contextPath)) {

                                path = path.substring(contextPath.length());
                        }

                        if (path.isBlank()) {
                                path = "/";
                        }

                        if (!path.startsWith("/")) {
                                path = "/" + path;
                        }

                        String query = uri.getRawQuery();
                        if (query != null && !query.isBlank()) {

                                path = path + "?" + query;
                        }

                        return path;

                } catch (Exception e) {

                        return null;
                }
        }

        /**
         * 외부 사이트로 리다이렉트되는 것을 방지한다.
         */
        private boolean isSameOrigin(URI uri, HttpServletRequest request) {

                if (uri.getHost() == null) {
                        return false;
                }

                if (!uri.getHost().equalsIgnoreCase(request.getServerName())) {

                        return false;
                }

                int requestPort = request.getServerPort();
                int uriPort = uri.getPort();

                /*
                 * URL에서 포트가 생략된 경우
                 * 스킴의 기본 포트를 적용한다.
                 */
                if (uriPort == -1) {
                        if ("https".equalsIgnoreCase(uri.getScheme())) {
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
        private boolean isUsableRedirectUrl(String redirectUrl) {

                if (redirectUrl == null || redirectUrl.isBlank()) {
                        return false;
                }

                String path = redirectUrl;
                int queryIndex = path.indexOf("?");

                if (queryIndex >= 0) {
                        path = path.substring(0, queryIndex);
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