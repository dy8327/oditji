package com.project.oditji.verify.controller;

// 1. 작성하신 비즈니스 로직 관련 VO 및 Service 클래스 (프로젝트 내부 파일)
import com.project.oditji.verify.service.VerifyService;
import com.project.oditji.verify.vo.AdultVerifyReadyVO;
import com.project.oditji.verify.vo.AdultVerifyRequestVO;
import com.project.oditji.verify.vo.AdultVerifyCompleteVO;

// 2. 서블릿 및 세션 관리를 위한 API
import jakarta.servlet.http.HttpSession;

// 3. 스프링 프레임워크 코어 및 어노테이션 속성값 주입을 위한 API
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

// 4. Spring Web MVC 라우팅 및 HTTP 요청 처리를 위한 API
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/verify")
public class VerifyController {

    private final VerifyService verifyService;

    // 포트원 성인인증 연동을 위한 설정값들
    @Value("${portone.store-id:test-store-id}")
    private String storeId;

    @Value("${portone.identity1.channel-key:test-simple-identity-channel-key}")
    private String identity1ChannelKey;

    @Value("${portone.identity2.channel-key:test-sms-identity-channel-key}")
    private String identity2ChannelKey;

    // 이제 VerifyService만 주입받습니다.
    public VerifyController(VerifyService verifyService) {
        this.verifyService = verifyService;
    }

    /**
     * 1. 성인인증 페이지 진입
     * 로그인 여부 및 기존 인증 여부를 체크하고, 인증에 필요한 가맹점 키값들을 화면에 전달합니다.
     */
    @GetMapping("/adult")
    public String adultVerifyPage(
            @RequestParam(value = "returnUrl", required = false, defaultValue = "/") String returnUrl,
            HttpSession session,
            Model model
    ) {
        Long memberNo = (Long) session.getAttribute("memberNo");

        // 1) 로그인 검증 (아이디가 비어있다면 로그인 창으로 이동)
        if (memberNo == null || memberNo == 0) {
            session.setAttribute("afterLoginRedirectUrl", "/verify/adult?returnUrl=" + sanitizeReturnUrl(returnUrl));
            return "redirect:/member/login";
        }

        // 2) 이미 성인인증을 완료한 유저인지 검증
        if (memberNo != 0 && verifyService.isAdultVerified(memberNo)) {
            session.setAttribute("ADULT_VERIFIED", "Y");
            return "redirect:" + sanitizeReturnUrl(returnUrl);
        }

        // 3) 아직 안 했다면 포트원 연동 키값과 리턴 URL을 가지고 성인인증 화면으로 이동
        model.addAttribute("storeId", storeId);
        model.addAttribute("identity1ChannelKey", identity1ChannelKey);
        model.addAttribute("identity2ChannelKey", identity2ChannelKey);
        model.addAttribute("returnUrl", sanitizeReturnUrl(returnUrl));

        return "verify/adultVerify";
    }

    /**
     * 2. 성인인증 준비 (포트원 호출 전 사전 작업)
     */
    @PostMapping("/adult/ready")
    @ResponseBody
    public AdultVerifyReadyVO prepareVerification(HttpSession session) {
        Long memberNo = (Long) session.getAttribute("memberNo");

        if (memberNo == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        
        return verifyService.prepareVerification(memberNo);
    }

    /**
     * 3. 성인인증 완료 처리
     */
    @PostMapping("/adult/complete")
    @ResponseBody
    public AdultVerifyCompleteVO completeVerification(
            @RequestBody AdultVerifyRequestVO adultVerifyRequestVO,
            @RequestParam(value = "returnUrl", required = false, defaultValue = "/") String returnUrl,
            HttpSession session
    ) {
        try {
            System.out.println("===== 성인인증 complete 진입 =====");
            Long memberNo = (Long) session.getAttribute("memberNo");

            if (memberNo == null) {
                return AdultVerifyCompleteVO.fail("로그인이 필요합니다.");
            }

            String verifyId = adultVerifyRequestVO.getVerifyId();
            if (verifyId == null || verifyId.isBlank()) {
                return AdultVerifyCompleteVO.fail("본인인증 요청 ID가 없습니다.");
            }

            return verifyService.completeVerification(
                    memberNo,
                    verifyId,
                    returnUrl,
                    session
            );

        } catch (Exception e) {
            System.out.println("===== 성인인증 complete 서버 오류 =====");
            e.printStackTrace();
            return AdultVerifyCompleteVO.fail("성인인증 완료 처리 중 서버 오류가 발생했습니다.");
        }
    }

    /**
     * 4. 성인인증 성공 페이지
     */
    @GetMapping("/adult/success")
    public String success() {
        return "verify/adultVerifySuccess";
    }

    /**
     * 5. 성인인증 실패 페이지
     */
    @GetMapping("/adult/fail")
    public String fail(
            @RequestParam(value = "message", required = false, defaultValue = "성인인증에 실패했습니다.") String message,
            Model model
    ) {
        model.addAttribute("message", message);
        return "verify/adultVerifyFail";
    }

    /**
     * URL 오픈 리다이렉트 취약점 방지용 유틸 메서드
     */
    private String sanitizeReturnUrl(String returnUrl) {
        if (returnUrl == null || returnUrl.isBlank() || 
            returnUrl.startsWith("http://") || returnUrl.startsWith("https://") || 
            !returnUrl.startsWith("/")) {
            return "/";
        }
        return returnUrl;
    }
}