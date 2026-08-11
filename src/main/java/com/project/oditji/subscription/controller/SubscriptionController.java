package com.project.oditji.subscription.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * OTT 구독 조합 계산기 화면을 담당합니다.
 * 계산에 필요한 데이터(콘텐츠 검색, 가격 계산)는 SubscriptionApiController가
 * JSON API로 제공하고, 이 컨트롤러는 뼈대 화면만 내려준다.
 */
@Controller
@RequestMapping("/subscription")
public class SubscriptionController {

    @GetMapping("/calculator")
    public String calculator() {

        return "subscription/calculator";
    }
}
