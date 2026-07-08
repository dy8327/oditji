package com.project.oditji.cart.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.oditji.cart.vo.CartVO;

@Controller
public class CartController {

    // =========================
    // 장바구니 페이지
    // =========================
    @GetMapping("/cart")
    public String cart(Model model) {

        List<CartVO> cartList = new ArrayList<>();

        CartVO item1 = new CartVO();
        item1.setGoodsId(1);
        item1.setGoodsName("아이언맨 피규어");
        item1.setPrice(35000);
        item1.setQuantity(2);

        CartVO item2 = new CartVO();
        item2.setGoodsId(2);
        item2.setGoodsName("스파이더맨 굿즈");
        item2.setPrice(28000);
        item2.setQuantity(1);

        CartVO item3 = new CartVO();
        item3.setGoodsId(3);
        item3.setGoodsName("마블 포스터 세트");
        item3.setPrice(15000);
        item3.setQuantity(3);

        cartList.add(item1);
        cartList.add(item2);
        cartList.add(item3);

        // 총 금액 계산 (더미)
        int totalPrice = 0;
        for (CartVO item : cartList) {
            totalPrice += item.getPrice() * item.getQuantity();
        }

        model.addAttribute("cartList", cartList);
        model.addAttribute("totalPrice", totalPrice);

        return "cart/cart";
    }
}