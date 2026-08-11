package com.project.oditji.subscription.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.project.oditji.event.dao.OttDiscountDAO;

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

    public SubscriptionController(OttDiscountDAO ottDiscountDAO) {
        this.ottDiscountDAO = ottDiscountDAO;
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
}
