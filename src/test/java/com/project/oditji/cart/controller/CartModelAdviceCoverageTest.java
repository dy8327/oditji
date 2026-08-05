package com.project.oditji.cart.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.cart.service.CartService;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpSession;

/** 공통 장바구니 배지의 세션 검증, 정상 조회와 장애 대체값을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class CartModelAdviceCoverageTest {

    @Mock
    private CartService cartService;

    @Mock
    private HttpSession session;

    private CartModelAdvice advice;

    @BeforeEach
    void setUp() {
        advice = new CartModelAdvice(cartService);
    }

    @Test
    void anonymousOrInvalidSessionShouldReturnZero() {
        assertEquals(0, advice.cartCount(session));

        when(session.getAttribute("loginMember"))
                .thenReturn("invalid")
                .thenReturn(member(null))
                .thenReturn(member(0L))
                .thenReturn(member(-1L));

        assertEquals(0, advice.cartCount(session));
        assertEquals(0, advice.cartCount(session));
        assertEquals(0, advice.cartCount(session));
        assertEquals(0, advice.cartCount(session));
        verify(cartService, never()).countCartItems(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void validMemberShouldReturnServiceCount() {
        when(session.getAttribute("loginMember")).thenReturn(member(11L));
        when(cartService.countCartItems(11L)).thenReturn(6);

        assertEquals(6, advice.cartCount(session));
    }

    @Test
    void serviceFailureShouldReturnZero() {
        when(session.getAttribute("loginMember")).thenReturn(member(12L));
        when(cartService.countCartItems(12L))
                .thenThrow(new IllegalStateException("db"));

        assertEquals(0, advice.cartCount(session));
    }

    private MemberVO member(Long memberNo) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        return member;
    }
}
