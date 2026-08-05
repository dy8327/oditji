package com.project.oditji.wish.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.wish.service.WishService;
import com.project.oditji.wish.vo.WishVO;

import jakarta.servlet.http.HttpSession;

/** 상품 찜 토글의 로그인, 성공, 입력 오류와 서버 오류 응답을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class WishControllerCoverageTest {

    @Mock
    private WishService wishService;

    @Mock
    private HttpSession session;

    private WishController controller;

    @BeforeEach
    void setUp() {
        controller = new WishController(wishService);
    }

    @Test
    void anonymousShouldReturnUnauthorized() {
        ResponseEntity<Map<String, Object>> response =
                controller.toggleWish(wish(10), session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("로그인이 필요합니다.", response.getBody().get("message"));
    }

    @Test
    void successShouldAttachMemberAndReturnActiveState() {
        MemberVO member = member(1L);
        when(session.getAttribute("loginMember")).thenReturn(member);
        WishVO request = wish(11);
        when(wishService.toggleWish(request)).thenReturn(true);

        ResponseEntity<Map<String, Object>> response =
                controller.toggleWish(request, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("active"));
        assertEquals(11, response.getBody().get("productNo"));
        assertEquals(1L, request.getMemberNo());
    }

    @Test
    void validationFailureShouldReturnBadRequest() {
        when(session.getAttribute("loginMember")).thenReturn(member(2L));
        WishVO request = wish(null);
        when(wishService.toggleWish(request))
                .thenThrow(new IllegalArgumentException("상품 번호가 필요합니다."));

        ResponseEntity<Map<String, Object>> response =
                controller.toggleWish(request, session);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("상품 번호가 필요합니다.", response.getBody().get("message"));
    }

    @Test
    void unexpectedFailureShouldReturnServerError() {
        when(session.getAttribute("loginMember")).thenReturn(member(3L));
        WishVO request = wish(12);
        when(wishService.toggleWish(request))
                .thenThrow(new IllegalStateException("db"));

        ResponseEntity<Map<String, Object>> response =
                controller.toggleWish(request, session);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().containsKey("active"));
        assertEquals(
                "찜 처리 중 오류가 발생했습니다.",
                response.getBody().get("message"));
    }

    private WishVO wish(Integer productNo) {
        WishVO wish = new WishVO();
        wish.setProductNo(productNo);
        return wish;
    }

    private MemberVO member(Long memberNo) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        return member;
    }
}
