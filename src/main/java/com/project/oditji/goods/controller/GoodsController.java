package com.project.oditji.goods.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.project.oditji.goods.service.GoodsService;
import com.project.oditji.goods.vo.GoodsVO;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.review.vo.ProductReviewVO;

@Controller
public class GoodsController {

    private static final int GOODS_PAGE_SIZE = 10;
    private static final int RECOMMEND_GOODS_SIZE = 5;

    private final GoodsService goodsService;
    private final ReviewService reviewService;

    public GoodsController(
            GoodsService goodsService,
            ReviewService reviewService) {

        this.goodsService = goodsService;
        this.reviewService = reviewService;
    }

    @GetMapping("/goods/list")
    public String goodsList(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) List<String> productTypes,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false, defaultValue = "false") boolean discountOnly,
            @RequestParam(required = false, defaultValue = "false") boolean inStockOnly,
            @RequestParam(required = false, defaultValue = "") String type,
            @RequestParam(required = false, defaultValue = "1") int page,
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

        model.addAttribute("goodsList", goodsList);
        model.addAttribute(
                "recommendedGoodsList",
                goodsService.getRecommendedGoods(RECOMMEND_GOODS_SIZE)
        );
        model.addAttribute(
                "availableProductTypes",
                goodsService.getSearchProductTypes()
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

    @GetMapping("/goods/goodsDetail/{productNo}")
    public String detail(
            @PathVariable int productNo,
            Model model) {

        GoodsVO goods =
                goodsService.getGoodsDetail(productNo);

        if (goods == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "존재하지 않거나 현재 공개되지 않은 상품입니다."
            );
        }

        List<Map<String, Object>> imageList =
                goodsService.getGoodsImageList(productNo);

        Map<String, Object> content =
                goodsService.getGoodsContent(productNo);

        Map<String, Object> actor =
                goodsService.getGoodsActor(productNo);

        List<ProductReviewVO> reviewList =
                reviewService.getProductReviewList(productNo);

        Double avgRating =
                reviewService.getProductAvgRating(productNo);

        int reviewCount =
                reviewService.getProductReviewCount(productNo);

        model.addAttribute("goods", goods);
        model.addAttribute("imageList", imageList);
        model.addAttribute("content", content);
        model.addAttribute("actor", actor);
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("reviewCount", reviewCount);

        return "goods/goodsDetail";
    }
}