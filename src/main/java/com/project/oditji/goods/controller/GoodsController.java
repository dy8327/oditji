package com.project.oditji.goods.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.project.oditji.common.util.GoodsFilterModelUtil;
import com.project.oditji.common.util.LoginMemberUtil;
import com.project.oditji.goods.service.GoodsService;
import com.project.oditji.goods.vo.GoodsVO;
import com.project.oditji.report.service.ReportService;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.review.vo.ProductReviewVO;
import com.project.oditji.wish.service.WishService;
import com.project.oditji.wish.vo.WishVO;

import jakarta.servlet.http.HttpSession;

@Controller
public class GoodsController {

    private static final int GOODS_PAGE_SIZE = 10;
    private static final int RECOMMEND_GOODS_SIZE = 5;

    private final GoodsService goodsService;
    private final ReviewService reviewService;
    private final ReportService reportService;
    private final WishService wishService;

    public GoodsController(
            GoodsService goodsService,
            ReviewService reviewService,
            ReportService reportService,
            WishService wishService) {

        this.goodsService = goodsService;
        this.reviewService = reviewService;
        this.reportService = reportService;
        this.wishService = wishService;
    }

    /**
     * 전체, 인기, 카테고리별 상품 목록을 같은 화면에서 제공합니다.
     *
     * type=all : 최신 등록순
     * type=popular : 재고 상품 우선, 할인율 높은 순, 최신순
     * type=category : 전체 목록을 보여주되 왼쪽 카테고리 필터로 이동
     */
    @GetMapping("/goods/list")
    public String goodsList(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) List<String> productTypes,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false, defaultValue = "false") boolean discountOnly,
            @RequestParam(required = false, defaultValue = "false") boolean inStockOnly,
            @RequestParam(required = false) List<String> priceRanges,
            @RequestParam(required = false) List<String> stockStatus,
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false, defaultValue = "popular") String sort,
            @RequestParam(required = false, defaultValue = "1") int page,
            HttpSession session,
            Model model) {

        String normalizedType = normalizeListType(type);

        /*
         * [수정] 카테고리 상품 화면은 반드시 도서/의상/OST/소품 중
         * 하나를 선택한 상태에서만 열리도록 처리합니다.
         * 세부 카테고리 없이 type=category만 전달된 기존 주소는
         * 전체 상품 화면으로 돌려 "카테고리별 상품 + 전체 상품" 화면이
         * 별도로 노출되지 않게 합니다.
         */
        String selectedCategoryName = getSelectedCategoryName(productTypes);

        if ("category".equals(normalizedType) && selectedCategoryName == null) {
            return "redirect:/goods/list?type=all";
        }

        int normalizedPage = page <= 0 ? 1 : page;

        int totalCount = goodsService.countSearchGoods(
                keyword,
                productTypes,
                minPrice,
                maxPrice,
                discountOnly,
                inStockOnly,
                priceRanges,
                stockStatus);

        int totalPage = Math.max(
                1,
                (int) Math.ceil((double) totalCount / GOODS_PAGE_SIZE));

        if (normalizedPage > totalPage) {
            normalizedPage = totalPage;
        }

        List<GoodsVO> goodsList = goodsService.searchGoods(
                keyword,
                productTypes,
                minPrice,
                maxPrice,
                discountOnly,
                inStockOnly,
                priceRanges,
                stockStatus,
                normalizedType,
                sort,
                normalizedPage,
                GOODS_PAGE_SIZE);

        Long loginMemberNo = LoginMemberUtil.getLoginMemberNo(session);
        Set<Integer> wishedProductNoSet = wishService.getWishedProductNoSet(loginMemberNo);

        model.addAttribute("goodsList", goodsList);
        model.addAttribute("recommendedGoodsList", goodsService.getRecommendedGoods(RECOMMEND_GOODS_SIZE));
        model.addAllAttributes(GoodsFilterModelUtil.create(
                keyword,
                productTypes,
                goodsService.getSearchProductTypes(),
                minPrice,
                maxPrice,
                discountOnly,
                inStockOnly));
        model.addAttribute("priceRanges", priceRanges);
        model.addAttribute("stockStatus", stockStatus);
        model.addAttribute("type", normalizedType);
        // [추가] 정렬 select(정렬 드롭다운)에서 선택한 정렬 조건을 화면에 그대로 유지합니다.
        model.addAttribute("sort", sort);
        // [추가] 선택한 세부 카테고리명을 목록 제목에 표시합니다.
        model.addAttribute("selectedCategoryName", selectedCategoryName);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("page", normalizedPage);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("wishedProductNoSet", wishedProductNoSet);

        return "goods/goodsList";
    }

    @GetMapping("/goods/goodsDetail/{productNo}")
    public String detail(
            @PathVariable int productNo,
            HttpSession session,
            Model model) {

        GoodsVO goods = goodsService.getGoodsDetail(productNo);

        if (goods == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "존재하지 않거나 현재 공개되지 않은 상품입니다.");
        }

        List<Map<String, Object>> imageList = goodsService.getGoodsImageList(productNo);
        // [상품 옵션 기능 추가] 의상/신발의 색상-사이즈별 재고를 상세 화면에 전달합니다.
        model.addAttribute("productOptionList", goodsService.getProductOptionList(productNo));
        Map<String, Object> content = goodsService.getGoodsContent(productNo);
        Map<String, Object> actor = goodsService.getGoodsActor(productNo);
        List<ProductReviewVO> reviewList = reviewService.getProductReviewList(productNo);
        Double avgRating = reviewService.getProductAvgRating(productNo);
        int reviewCount = reviewService.getProductReviewCount(productNo);

        /*
         * 오른쪽 사이드바(추천 상품)에 사용할 목록입니다.
         * 지금 보고 있는 상품 자신이 추천 목록에 함께 뜨지 않도록
         * 여유분(+1)을 조회한 뒤 현재 productNo를 제외하고
         * 원래 노출 개수(RECOMMEND_GOODS_SIZE)만큼만 잘라서 사용합니다.
         */
        List<GoodsVO> recommendedGoodsCandidates = goodsService.getRecommendedGoods(RECOMMEND_GOODS_SIZE + 1);

        List<GoodsVO> recommendedGoodsList = new ArrayList<>();

        for (GoodsVO recommend : recommendedGoodsCandidates) {

            if (recommend.getProductNo() != productNo) {
                if (recommendedGoodsList.size() >= RECOMMEND_GOODS_SIZE) {
                    break;
                }

                recommendedGoodsList.add(recommend);
            }
        }

        Long loginMemberNo = LoginMemberUtil.getLoginMemberNo(session);
        goodsService.addProductClickLog(productNo, loginMemberNo);
        Set<Integer> reportedReviewSet = reportService.getReportedProductReviewSet(loginMemberNo);

        boolean wishActive = false;

        if (loginMemberNo != null) {

            WishVO wishVO = new WishVO();
            wishVO.setMemberNo(loginMemberNo);
            wishVO.setProductNo(productNo);

            wishActive = wishService.isWished(wishVO);
        }

        model.addAttribute("goods", goods);
        model.addAttribute("imageList", imageList);
        model.addAttribute("content", content);
        model.addAttribute("actor", actor);
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("reportedReviewSet", reportedReviewSet);
        model.addAttribute("wishActive", wishActive);
        model.addAttribute("recommendedGoodsList", recommendedGoodsList);

        return "goods/goodsDetail";
    }

    /**
     * [추가] 헤더의 세부 카테고리 메뉴에서 허용하는 상품 종류를
     * 화면 표시명으로 변환합니다. 카테고리 화면에서는 한 종류만
     * 선택할 수 있으므로 값이 없거나 여러 개이면 null을 반환합니다.
     */
    private String getSelectedCategoryName(List<String> productTypes) {

        if (productTypes == null || productTypes.size() != 1) {
            return null;
        }

        String productType = productTypes.get(0);

        /*
         * [중복 코드 개선] 동일한 if-return 구조를 switch 표현식으로 통합합니다.
         * 허용된 상품 종류와 화면 표시명은 기존과 동일합니다.
         */
        return switch (productType) {
            case "BOOK" -> "도서";
            case "CLOTHES" -> "의상";
            case "SHOES" -> "신발";
            case "OST" -> "OST";
            case "PROP" -> "소품";
            case "GOODS" -> "굿즈";
            case "FIGURE" -> "피규어";
            case "POSTER" -> "포스터";
            case "ETC" -> "기타";
            default -> null;
        };
    }

    /** 잘못된 상품 목록 유형은 전체 상품으로 처리합니다. */
    private String normalizeListType(String type) {
        String normalized = type == null
                ? "all"
                : type.trim().toLowerCase(Locale.ROOT);

        if ("popular".equals(normalized) || "category".equals(normalized)) {
            return normalized;
        }

        return "all";
    }
}