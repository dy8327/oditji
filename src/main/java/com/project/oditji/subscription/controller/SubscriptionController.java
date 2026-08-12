package com.project.oditji.subscription.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.project.oditji.event.dao.OttDiscountDAO;
import com.project.oditji.subscription.service.SubscriptionCalculatorService;
import com.project.oditji.subscription.vo.SubscriptionCalculationResultVO;

/**
 * OTT 구독 조합 계산기 화면을 담당합니다.
 * 계산 결과(가격 산출)는 SubscriptionApiController가 JSON API로 제공하고,
 * 이 컨트롤러는 화면 뼈대와 함께 "내 할인 조건" 필터(통신사/카드사/멤버십)
 * 선택지를 DB에서 조회해 내려준다. 선택지가 DB(OTT_DISCOUNT_INFO) 실데이터를
 * 그대로 따라가도록 하드코딩하지 않는다.
 */
@Controller
@RequestMapping("/subscription")
public class SubscriptionController {

    private static final String CATEGORY_TELECOM = "TELECOM";
    private static final String CATEGORY_CARD = "CARD";
    private static final String CATEGORY_MEMBERSHIP = "MEMBERSHIP";

    private final OttDiscountDAO ottDiscountDAO;

    private final SubscriptionCalculatorService subscriptionCalculatorService;

    public SubscriptionController(
            OttDiscountDAO ottDiscountDAO,
            SubscriptionCalculatorService subscriptionCalculatorService) {
        this.ottDiscountDAO = ottDiscountDAO;
        this.subscriptionCalculatorService = subscriptionCalculatorService;
    }

    @GetMapping("/calculator")
    public String calculator(Model model) {

        List<String> telecomList =
                ottDiscountDAO.selectDiscountProviderNames(CATEGORY_TELECOM);
        List<String> cardList =
                ottDiscountDAO.selectDiscountProviderNames(CATEGORY_CARD);
        List<String> membershipList =
                ottDiscountDAO.selectDiscountProviderNames(CATEGORY_MEMBERSHIP);

        model.addAttribute("telecomList", telecomList);
        model.addAttribute("cardList", cardList);
        model.addAttribute("membershipList", membershipList);

        return "subscription/calculator";
    }

    /**
     * 공유 링크로 저장된 계산 결과를 복원해서 보여준다. 로그인 여부와 무관하게 조회 가능하다.
     * 존재하지 않는 resultId면 resultNotFound 플래그만 세팅해서 안내 화면을 보여준다.
     */
    @GetMapping("/result/{resultId}")
    public String result(
            @PathVariable("resultId") String resultId,
            Model model) {

        SubscriptionCalculationResultVO result =
                subscriptionCalculatorService.restoreResult(resultId);

        if (result == null) {

            model.addAttribute("resultNotFound", true);

            return "subscription/result";
        }

        model.addAttribute("result", result);
        model.addAttribute("resultId", resultId);

        return "subscription/result";
    }
}
