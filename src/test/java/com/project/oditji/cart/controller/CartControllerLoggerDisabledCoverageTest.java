package com.project.oditji.cart.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.project.oditji.cart.service.CartService;
import com.project.oditji.cart.vo.CartRequestVO;
import com.project.oditji.member.vo.MemberVO;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.servlet.http.HttpSession;

/** CartController의 ERROR 로그 guard false 분기를 보완합니다. */
class CartControllerLoggerDisabledCoverageTest {

    private CartService cartService;
    private HttpSession session;
    private CartController controller;
    private Logger controllerLogger;
    private Level previousLevel;

    @BeforeEach
    void setUp() {
        cartService = mock(CartService.class);
        session = mock(HttpSession.class);
        controller = new CartController(cartService);

        MemberVO member = new MemberVO();
        member.setMemberNo(99L);
        when(session.getAttribute("loginMember")).thenReturn(member);

        controllerLogger = (Logger) LoggerFactory.getLogger(CartController.class);
        previousLevel = controllerLogger.getLevel();
        controllerLogger.setLevel(Level.OFF);
    }

    @AfterEach
    void tearDown() {
        controllerLogger.setLevel(previousLevel);
    }

    @Test
    void unexpectedAddFailureShouldReturnFallbackWhenErrorLoggingIsDisabled() {
        CartRequestVO request = request();
        when(cartService.addCartItem(99L, 10, 20L, 3))
                .thenThrow(new IllegalStateException("db"));

        Map<String, Object> response = controller.addCartItem(request, session);

        assertEquals(Boolean.FALSE, response.get("success"));
        assertEquals("장바구니 처리 중 오류가 발생했습니다.", response.get("message"));
    }

    @Test
    void unexpectedUpdateFailureShouldReturnFallbackWhenErrorLoggingIsDisabled() {
        CartRequestVO request = request();
        doThrow(new IllegalStateException("db"))
                .when(cartService)
                .updateCartItemQuantity(99L, 30L, 3);

        Map<String, Object> response = controller.updateCartItem(request, session);

        assertEquals(Boolean.FALSE, response.get("success"));
        assertEquals("수량 변경 중 오류가 발생했습니다.", response.get("message"));
    }

    private CartRequestVO request() {
        CartRequestVO request = new CartRequestVO();
        request.setProductNo(10);
        request.setOptionNo(20L);
        request.setQuantity(3);
        request.setCartItemNo(30L);
        return request;
    }
}
