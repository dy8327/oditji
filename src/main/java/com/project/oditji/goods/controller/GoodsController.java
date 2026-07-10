package com.project.oditji.goods.controller;

import com.project.oditji.goods.vo.GoodsVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class GoodsController {

    // =========================
    // 상품 리스트
    // =========================
    @GetMapping("/goods/list")
    public String goodsList(Model model) {

        List<GoodsVO> goodsList = new ArrayList<>();

        // =========================
        // 더미 1
        // =========================
        GoodsVO g1 = new GoodsVO();
        g1.setProductNo(1);
        g1.setProductName("오징어게임 키링");
        g1.setProductType("ACCESSORY");
        g1.setBusinessName("테스트사업자");
        g1.setMainImage("https://via.placeholder.com/300x450");
        g1.setPrice(12000);
        g1.setDiscountRate(0);
        g1.setStock(10);
        g1.setStatus("ON_SALE");

        // =========================
        // 더미 2
        // =========================
        GoodsVO g2 = new GoodsVO();
        g2.setProductNo(2);
        g2.setProductName("더 글로리 머그컵");
        g2.setProductType("ETC");
        g2.setBusinessName("테스트사업자");
        g2.setMainImage("https://via.placeholder.com/300x450");
        g2.setPrice(15000);
        g2.setDiscountRate(10);
        g2.setStock(5);
        g2.setStatus("ON_SALE");

        // =========================
        // 더미 3
        // =========================
        GoodsVO g3 = new GoodsVO();
        g3.setProductNo(3);
        g3.setProductName("넷플릭스 후드티");
        g3.setProductType("APPAREL");
        g3.setBusinessName("테스트사업자");
        g3.setMainImage("https://via.placeholder.com/300x450");
        g3.setPrice(39000);
        g3.setDiscountRate(0);
        g3.setStock(0);
        g3.setStatus("ON_SALE");

        goodsList.add(g1);
        goodsList.add(g2);
        goodsList.add(g3);

        model.addAttribute("goodsList", goodsList);

        // =========================
        // 목록 상단 / 페이지네이션
        // (goodsList.jsp: type, totalCount, page, totalPage)
        // =========================
        model.addAttribute("type", "");
        model.addAttribute("totalCount", goodsList.size());
        model.addAttribute("page", 1);
        model.addAttribute("totalPage", 1);

        // =========================
        // 오른쪽 사이드바 - 추천 상품
        // (goodsRightSidebar.jsp: recommendedGoodsList)
        // =========================
        List<GoodsVO> recommendedGoodsList = new ArrayList<>();

        GoodsVO r1 = new GoodsVO();
        r1.setProductNo(2);
        r1.setProductName("더 글로리 머그컵");
        r1.setBusinessName("테스트사업자");
        r1.setMainImage("https://via.placeholder.com/185x260");
        r1.setPrice(15000);
        r1.setDiscountRate(10);

        GoodsVO r2 = new GoodsVO();
        r2.setProductNo(1);
        r2.setProductName("오징어게임 키링");
        r2.setBusinessName("테스트사업자");
        r2.setMainImage("https://via.placeholder.com/185x260");
        r2.setPrice(12000);
        r2.setDiscountRate(0);

        recommendedGoodsList.add(r1);
        recommendedGoodsList.add(r2);

        model.addAttribute("recommendedGoodsList", recommendedGoodsList);

        return "goods/goodsList";
    }

    // =========================
    // 상품 상세
    // =========================
    @GetMapping("/goods/goodsDetail/{productNo}")
    public String detail(@PathVariable int productNo, Model model) {

        GoodsVO goods = new GoodsVO();
        goods.setProductNo(productNo);
        goods.setProductName("더 글로리 머그컵");
        goods.setProductType("ETC");
        goods.setBusinessName("테스트사업자");
        goods.setMainImage("https://via.placeholder.com/500x500");
        goods.setPrice(15000);
        goods.setDiscountRate(10);
        goods.setStock(5);
        goods.setStatus("ON_SALE");
        goods.setDescription("드라마 <더 글로리> 공식 굿즈 머그컵입니다.");

        model.addAttribute("goods", goods);

        // =========================
        // 상세 이미지 갤러리 (PRODUCT_IMAGE)
        // =========================
        List<Map<String, Object>> imageList = new ArrayList<>();

        Map<String, Object> img1 = new LinkedHashMap<>();
        img1.put("imagePath", "https://via.placeholder.com/500x500");
        img1.put("isMain", "Y");

        Map<String, Object> img2 = new LinkedHashMap<>();
        img2.put("imagePath", "https://via.placeholder.com/500x500/222222");
        img2.put("isMain", "N");

        imageList.add(img1);
        imageList.add(img2);

        model.addAttribute("imageList", imageList);

        // =========================
        // 원작 콘텐츠 (CONTENT, 단일 참조)
        // =========================
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("contentNo", 1);
        content.put("title", "더 글로리");
        content.put("posterPath", "/pbpMk2JmcoNnQwx5JGpXngfoWtp.jpg");
        content.put("contentType", "TV");
        content.put("tmdbScore", 8.5);

        model.addAttribute("content", content);

        // =========================
        // 담당 배우 (ACTOR, 단일 참조 - 없으면 생략 가능)
        // =========================
        Map<String, Object> actor = new LinkedHashMap<>();
        actor.put("actorName", "송혜교");

        model.addAttribute("actor", actor);

        // =========================
        // 상품 리뷰 (PRODUCT_REVIEW)
        // =========================
        List<Map<String, Object>> reviewList = new ArrayList<>();

        Map<String, Object> review1 = new LinkedHashMap<>();
        review1.put("writer", "user01");
        review1.put("rating", 5);
        review1.put("content", "생각보다 퀄리티가 좋아요!");
        review1.put("createdAt", "2026-07-01");

        reviewList.add(review1);

        model.addAttribute("reviewList", reviewList);
        model.addAttribute("avgRating", 5.0);
        model.addAttribute("reviewCount", reviewList.size());

        return "goods/goodsDetail";
    }
}
