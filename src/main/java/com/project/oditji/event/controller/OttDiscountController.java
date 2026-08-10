package com.project.oditji.event.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.oditji.common.util.PaginationUtil;
import com.project.oditji.common.vo.PageVO;
import com.project.oditji.event.service.OttDiscountService;
import com.project.oditji.event.vo.OttDiscountVO;

@Controller
public class OttDiscountController {

    private static final String DEFAULT_FILTER = "ALL";

    /** [수정] OTT 할인 정보 페이지 9개 단위 페이징 추가 */
    private static final int DISCOUNT_PAGE_SIZE = 9;

    private final OttDiscountService ottDiscountService;

    public OttDiscountController(OttDiscountService ottDiscountService) {
        this.ottDiscountService = ottDiscountService;
    }

    /**
     * OTT 할인 정보 메인 화면 이동
     * URL: /discount/ott
     * 최초 진입 시 전체(ALL) 조건으로 9개 단위 페이징된 할인 목록을 함께 내려주고,
     * 이후 필터/페이지 변경은 화면에서 /api/discount 를 비동기로 호출해 처리한다.
     */
    @GetMapping("/discount/ott")
    public String ottDiscountPage(
            @RequestParam(value = "platform", required = false, defaultValue = DEFAULT_FILTER) String platform,
            @RequestParam(value = "category", required = false, defaultValue = DEFAULT_FILTER) String category,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model) {

        int totalCount = ottDiscountService.getDiscountListCount(platform, category);
        PageVO pagination = PaginationUtil.createPage(page, DISCOUNT_PAGE_SIZE, totalCount);

        List<OttDiscountVO> discountList =
                ottDiscountService.getDiscountList(platform, category, pagination.getCurrentPage(), DISCOUNT_PAGE_SIZE);

        model.addAttribute("discountList", discountList);
        /* [수정] 히어로 배너는 목록 페이징과 무관하게 항상 정확한 대표 항목을 보여줘야
           하므로, 페이징된 discountList가 아닌 별도 조회 결과를 쓴다. */
        model.addAttribute("heroItem", ottDiscountService.getHeroDiscount(platform, category));
        model.addAttribute("selectedPlatform", platform);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("pageVO", pagination);

        return "event/ottDiscount";
    }
}
