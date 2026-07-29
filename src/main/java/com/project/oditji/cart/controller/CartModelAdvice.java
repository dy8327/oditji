package com.project.oditji.cart.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.oditji.cart.service.CartService;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class CartModelAdvice {

    private final CartService cartService;
    private static final Logger log = LoggerFactory.getLogger(CartModelAdvice.class);

    public CartModelAdvice(CartService cartService) {

        this.cartService = cartService;
    }

    @ModelAttribute("cartCount")
    public int cartCount(HttpSession session) {

        Object sessionMember = session.getAttribute("loginMember");

        if (!(sessionMember instanceof MemberVO)) {
            return 0;
        }

        MemberVO loginMember = (MemberVO) sessionMember;

        if (loginMember.getMemberNo() == null || loginMember.getMemberNo() <= 0) {

            return 0;
        }

        try {

            return cartService.countCartItems(loginMember.getMemberNo());

        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("장바구니 수량 조회 실패", e);
            }
            return 0;
        }
    }
}