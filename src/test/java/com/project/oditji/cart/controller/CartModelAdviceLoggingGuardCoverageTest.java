package com.project.oditji.cart.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.project.oditji.cart.service.CartService;
import com.project.oditji.member.vo.MemberVO;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.servlet.http.HttpSession;

/** 장바구니 공통 모델의 WARN 로그 비활성 분기를 보완합니다. */
class CartModelAdviceLoggingGuardCoverageTest {

    @Test
    void serviceFailureShouldReturnZeroWhenWarnLoggingIsDisabled() {
        CartService cartService = mock(CartService.class);
        HttpSession session = mock(HttpSession.class);
        CartModelAdvice advice = new CartModelAdvice(cartService);

        MemberVO member = new MemberVO();
        member.setMemberNo(77L);

        when(session.getAttribute("loginMember")).thenReturn(member);
        when(cartService.countCartItems(77L))
                .thenThrow(new IllegalStateException("cart failure"));

        Logger logger = (Logger) LoggerFactory.getLogger(CartModelAdvice.class);
        Level originalLevel = logger.getLevel();

        try {
            logger.setLevel(Level.OFF);
            assertEquals(0, advice.cartCount(session));
        } finally {
            logger.setLevel(originalLevel);
        }
    }
}
