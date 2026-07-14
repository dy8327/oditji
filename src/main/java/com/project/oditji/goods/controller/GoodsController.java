package com.project.oditji.goods.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.oditji.goods.service.GoodsService;
import com.project.oditji.goods.vo.GoodsVO;

@Controller
public class GoodsController {

    private static final int GOODS_PAGE_SIZE = 10;
    private static final int RECOMMEND_GOODS_SIZE = 5;

    private final GoodsService goodsService;

    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    // =========================================================
    // 상품 목록
    // =========================================================
    @GetMapping("/goods/list")
    public String goodsList(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) List<String> productTypes,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false, defaultValue = "false")
            boolean discountOnly,
            @RequestParam(required = false, defaultValue = "false")
            boolean inStockOnly,
            @RequestParam(required = false, defaultValue = "")
            String type,
            @RequestParam(required = false, defaultValue = "1")
            int page,
            Model model) {

        int normalizedPage = page <= 0 ? 1 : page;

        int totalCount = goodsService.countSearchGoods(
                keyword,
                productTypes,
                minPrice,
                maxPrice,
                discountOnly,
                inStockOnly
        );

        int totalPage = Math.max(
                1,
                (int) Math.ceil((double) totalCount / GOODS_PAGE_SIZE)
        );

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
                normalizedPage,
                GOODS_PAGE_SIZE
        );

        List<GoodsVO> recommendedGoodsList =
                goodsService.getRecommendedGoods(RECOMMEND_GOODS_SIZE);

        List<String> availableProductTypes =
                goodsService.getSearchProductTypes();

        model.addAttribute("goodsList", goodsList);
        model.addAttribute(
                "recommendedGoodsList",
                recommendedGoodsList
        );
        model.addAttribute(
                "availableProductTypes",
                availableProductTypes
        );

        model.addAttribute("keyword", keyword);
        model.addAttribute("productTypes", productTypes);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("discountOnly", discountOnly);
        model.addAttribute("inStockOnly", inStockOnly);

        model.addAttribute("type", type);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("page", normalizedPage);
        model.addAttribute("totalPage", totalPage);

        return "goods/goodsList";
    }

    // =========================================================
    // 상품 상세
    // 현재 상세 페이지는 기존 하드코딩 구조 유지
    // 추후 상세 DB 조회 적용 시 별도 변경 가능
    // =========================================================
    @GetMapping("/goods/goodsDetail/{productNo}")
    public String detail(
            @PathVariable int productNo,
            Model model) {

        GoodsVO goods = new GoodsVO();

        goods.setProductNo(productNo);
        goods.setProductName("더 글로리 머그컵");
        goods.setProductType("ETC");
        goods.setBusinessName("테스트사업자");
        goods.setMainImage(
                "https://via.placeholder.com/500x500"
        );
        goods.setPrice(15000);
        goods.setDiscountRate(10);
        goods.setStock(5);
        goods.setStatus("ON_SALE");
        goods.setDescription(
                "드라마 <더 글로리> 공식 굿즈 머그컵입니다."
        );

        model.addAttribute("goods", goods);

        // =====================================================
        // 상세 이미지 갤러리
        // =====================================================
        List<Map<String, Object>> imageList =
                new ArrayList<Map<String, Object>>();

        Map<String, Object> img1 =
                new LinkedHashMap<String, Object>();

        img1.put(
                "imagePath",
                "https://via.placeholder.com/500x500"
        );
        img1.put("isMain", "Y");

        Map<String, Object> img2 =
                new LinkedHashMap<String, Object>();

        img2.put(
                "imagePath",
                "https://via.placeholder.com/500x500/222222"
        );
        img2.put("isMain", "N");

        imageList.add(img1);
        imageList.add(img2);

        model.addAttribute("imageList", imageList);

        // =====================================================
        // 원작 콘텐츠
        // =====================================================
        Map<String, Object> content =
                new LinkedHashMap<String, Object>();

        content.put("contentNo", 1);
        content.put("title", "더 글로리");
        content.put(
                "posterPath",
                "/pbpMk2JmcoNnQwx5JGpXngfoWtp.jpg"
        );
        content.put("contentType", "TV");
        content.put("tmdbScore", 8.5);

        model.addAttribute("content", content);

        // =====================================================
        // 담당 배우
        // =====================================================
        Map<String, Object> actor =
                new LinkedHashMap<String, Object>();

        actor.put("actorName", "송혜교");

        model.addAttribute("actor", actor);

        // =====================================================
        // 상품 리뷰
        // =====================================================
        List<Map<String, Object>> reviewList =
                new ArrayList<Map<String, Object>>();

        Map<String, Object> review1 =
                new LinkedHashMap<String, Object>();

        review1.put("writer", "user01");
        review1.put("rating", 5);
        review1.put(
                "content",
                "생각보다 퀄리티가 좋아요!"
        );
        review1.put("createdAt", "2026-07-01");

        reviewList.add(review1);

        model.addAttribute("reviewList", reviewList);
        model.addAttribute("avgRating", 5.0);
        model.addAttribute(
                "reviewCount",
                reviewList.size()
        );

        return "goods/goodsDetail";
    }
}