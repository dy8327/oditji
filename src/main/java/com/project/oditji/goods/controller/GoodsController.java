package com.project.oditji.goods.controller;

import com.project.oditji.goods.vo.GoodsVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class GoodsController {

    @GetMapping("/goods/list")
    public String goodsList(Model model) {

        List<GoodsVO> goodsList = new ArrayList<>();

        // =========================
        // 더미 1
        // =========================
        GoodsVO g1 = new GoodsVO();
        g1.setGoodsId(1);
        g1.setGoodsName("오징어게임 키링");
        g1.setImageUrl("https://via.placeholder.com/300x450");
        g1.setPrice(12000);
        g1.setStock(10);

        // =========================
        // 더미 2
        // =========================
        GoodsVO g2 = new GoodsVO();
        g2.setGoodsId(2);
        g2.setGoodsName("더 글로리 머그컵");
        g2.setImageUrl("https://via.placeholder.com/300x450");
        g2.setPrice(15000);
        g2.setStock(5);

        // =========================
        // 더미 3
        // =========================
        GoodsVO g3 = new GoodsVO();
        g3.setGoodsId(3);
        g3.setGoodsName("넷플릭스 후드티");
        g3.setImageUrl("https://via.placeholder.com/300x450");
        g3.setPrice(39000);
        g3.setStock(2);

        goodsList.add(g1);
        goodsList.add(g2);
        goodsList.add(g3);

        model.addAttribute("goodsList", goodsList);

        return "goods/goodsList";
    }

    // 상세
    @GetMapping("/goods/detail")
    public String detail() {
        return "goods/goodsDetail";
    }
}