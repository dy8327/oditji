package com.project.oditji.wish.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.wish.service.WishService;
import com.project.oditji.wish.vo.WishVO;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.servlet.http.HttpSession;

/** WishController의 ERROR 로그 비활성화 분기를 검증합니다. */
class WishControllerLoggingGuardFinalCoverageTest {

    @Test
    void unexpectedFailureShouldReturnServerErrorWhenErrorLoggingIsDisabled() {
        WishService wishService = mock(WishService.class);
        HttpSession session = mock(HttpSession.class);
        MemberVO member = new MemberVO();
        member.setMemberNo(7L);
        when(session.getAttribute("loginMember")).thenReturn(member);

        WishVO request = new WishVO();
        request.setProductNo(91);
        when(wishService.toggleWish(request)).thenThrow(new IllegalStateException("db"));

        WishController controller = new WishController(wishService);
        Logger logger = (Logger) LoggerFactory.getLogger(WishController.class);
        Level originalLevel = logger.getLevel();
        try {
            logger.setLevel(Level.OFF);
            ResponseEntity<Map<String, Object>> response = controller.toggleWish(request, session);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        } finally {
            logger.setLevel(originalLevel);
        }
    }
}
