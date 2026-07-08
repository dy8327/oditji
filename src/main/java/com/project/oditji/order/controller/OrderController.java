package com.project.oditji.order.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.oditji.cart.vo.CartVO;
import com.project.oditji.order.vo.OrderVO;

@Controller
public class OrderController {

    @GetMapping("/order/list")
    public String orderList(Model model) {

        // =========================
        // CartVO 기반 아이템 더미
        // =========================
        CartVO c1 = new CartVO();
        c1.setCartId(1);
        c1.setGoodsId(101);
        c1.setGoodsName("아이언맨 피규어");
        c1.setPrice(35000);
        c1.setQuantity(1);
        c1.setImage("/images/goods/ironman.jpg");

        CartVO c2 = new CartVO();
        c2.setCartId(2);
        c2.setGoodsId(102);
        c2.setGoodsName("스파이더맨 키링");
        c2.setPrice(12000);
        c2.setQuantity(2);
        c2.setImage("/images/goods/spiderman.jpg");

        CartVO c3 = new CartVO();
        c3.setCartId(3);
        c3.setGoodsId(103);
        c3.setGoodsName("마블 포스터 세트");
        c3.setPrice(15000);
        c3.setQuantity(1);
        c3.setImage("/images/goods/marvel.jpg");

        List<CartVO> items = new ArrayList<>();
        items.add(c1);
        items.add(c2);
        items.add(c3);

        // =========================
        // Order 생성
        // =========================
        OrderVO order = new OrderVO();
        order.setOrderId(1001);
        order.setCreatedAt("2026-07-02");
        order.setStatus("결제완료");
        order.setItems(items);

        // 총 금액 계산
        int totalPrice = 0;
        for (CartVO item : items) {
            totalPrice += item.getPrice() * item.getQuantity();
        }
        order.setTotalPrice(totalPrice);

        // =========================
        // 리스트로 감싸서 JSP 전달
        // =========================
        List<OrderVO> orderList = new ArrayList<>();
        orderList.add(order);

        model.addAttribute("orderList", orderList);

        return "order/orderList";
    }
}