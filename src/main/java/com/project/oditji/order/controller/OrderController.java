package com.project.oditji.order.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.oditji.cart.vo.CartItemVO;
import com.project.oditji.order.vo.OrderVO;

@Controller
public class OrderController {

    @GetMapping("/order/list")
    public String orderList(Model model) {

        /*
         * 현재 주문 기능은 실제 DB 연동 전이므로
         * CartItemVO를 이용한 임시 주문 상품 데이터를 생성한다.
         */

        CartItemVO item1 = new CartItemVO();

        item1.setCartItemNo(1L);
        item1.setCartNo(1L);
        item1.setProductNo(101);
        item1.setProductName("아이언맨 피규어");
        item1.setProductType("FIGURE");
        item1.setBusinessName("ODITJI 굿즈샵");
        item1.setPrice(35000);
        item1.setDiscountRate(0);
        item1.setQuantity(1);
        item1.setStock(10);
        item1.setStatus("ON_SALE");
        item1.setMainImage(
                "/uploads/product/ironman.jpg"
        );

        CartItemVO item2 = new CartItemVO();

        item2.setCartItemNo(2L);
        item2.setCartNo(1L);
        item2.setProductNo(102);
        item2.setProductName("스파이더맨 키링");
        item2.setProductType("ACCESSORY");
        item2.setBusinessName("ODITJI 굿즈샵");
        item2.setPrice(12000);
        item2.setDiscountRate(0);
        item2.setQuantity(2);
        item2.setStock(20);
        item2.setStatus("ON_SALE");
        item2.setMainImage(
                "/uploads/product/spiderman.jpg"
        );

        CartItemVO item3 = new CartItemVO();

        item3.setCartItemNo(3L);
        item3.setCartNo(1L);
        item3.setProductNo(103);
        item3.setProductName("마블 포스터 세트");
        item3.setProductType("POSTER");
        item3.setBusinessName("ODITJI 굿즈샵");
        item3.setPrice(15000);
        item3.setDiscountRate(0);
        item3.setQuantity(1);
        item3.setStock(15);
        item3.setStatus("ON_SALE");
        item3.setMainImage(
                "/uploads/product/marvel.jpg"
        );

        List<CartItemVO> items =
                new ArrayList<CartItemVO>();

        items.add(item1);
        items.add(item2);
        items.add(item3);

        /*
         * 주문 정보 생성
         */
        OrderVO order = new OrderVO();

        order.setOrderId(1001L);
        order.setCreatedAt("2026-07-02");
        order.setStatus("결제완료");
        order.setItems(items);

        /*
         * 할인 적용 금액 × 수량으로 총 주문 금액 계산
         */
        long totalPrice = 0L;

        for (CartItemVO item : items) {
            totalPrice += item.getItemTotalPrice();
        }

        order.setTotalPrice(totalPrice);

        /*
         * 주문 목록 화면 전달
         */
        List<OrderVO> orderList =
                new ArrayList<OrderVO>();

        orderList.add(order);

        model.addAttribute(
                "orderList",
                orderList
        );

        return "order/orderList";
    }
}