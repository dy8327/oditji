package com.project.oditji.event.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.oditji.common.util.OttPlatformUtil;
import com.project.oditji.common.util.PaginationUtil;
import com.project.oditji.common.vo.PageVO;
import com.project.oditji.event.service.OttDiscountService;
import com.project.oditji.event.vo.OttDiscountVO;
import com.project.oditji.tmdb.dao.TmdbDAO;

@Controller
public class OttDiscountController {

    private static final String DEFAULT_FILTER = "ALL";

    /** [수정] OTT 할인 정보 페이지 8개 단위 페이징 */
    private static final int DISCOUNT_PAGE_SIZE = 8;

    private final OttDiscountService ottDiscountService;
    private final TmdbDAO tmdbDAO;

    public OttDiscountController(OttDiscountService ottDiscountService, TmdbDAO tmdbDAO) {
        this.ottDiscountService = ottDiscountService;
        this.tmdbDAO = tmdbDAO;
    }

    /**
     * OTT 할인 정보 메인 화면 이동
     * URL: /discount/ott
     * 최초 진입 시 전체(ALL) 조건으로 8개 단위 페이징된 할인 목록을 함께 내려주고,
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

        /* [수정] 카드/히어로 배너의 OTT 로고는 다른 화면(콘텐츠 목록, 검색 등)과 동일하게
           OTT_PLATFORM 테이블에 저장된 실제 로고 이미지를 사용한다. platformName -> logoImage URL
           맵으로 변환해두면, JSTL에서는 ottLogoMap['netflix'] 형태로, JS 쪽은 아래 JSON 스크립트로
           내려준 값을 그대로 재사용한다(플랫폼 목록 자체는 필터와 무관하게 항상 동일하므로
           AJAX로 필터를 바꿔도 다시 조회할 필요가 없다). */
        Map<String, String> ottLogoMap = OttPlatformUtil.createLogoMap(tmdbDAO.selectActivePlatformList());

        model.addAttribute("discountList", discountList);
        /* [수정] 히어로 배너는 목록 페이징과 무관하게 항상 정확한 대표 항목을 보여줘야
           하므로, 페이징된 discountList가 아닌 별도 조회 결과를 쓴다. */
        model.addAttribute("heroItem", ottDiscountService.getHeroDiscount(platform, category));
        model.addAttribute("selectedPlatform", platform);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("pageVO", pagination);
        model.addAttribute("ottLogoMap", ottLogoMap);

        return "event/ottDiscount";
    }
}
