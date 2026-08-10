package com.project.oditji.common.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpSession;

/**
 * 로그인 인터셉터의 세션 타입, 회원번호 및 로그인 후 복귀 경로 분기를 검증합니다.
 */
class LoginCheckInterceptorCoverageTest {

    private LoginCheckInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new LoginCheckInterceptor();
    }

    @Test
    void validLoginMemberShouldPassImmediately() throws Exception {
        MockHttpServletRequest request = request("GET", "/oditji/member/mypage", null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        MemberVO loginMember = new MemberVO();
        loginMember.setMemberNo(10L);
        request.getSession().setAttribute("loginMember", loginMember);

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertNull(response.getRedirectedUrl());
    }

    @Test
    void wrongSessionTypeShouldRedirectAndStoreQueryPath() throws Exception {
        MockHttpServletRequest request = request(
                "GET",
                "/oditji/product/list",
                "page=2&sort=recent");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.getSession().setAttribute("loginMember", "invalid");

        assertFalse(interceptor.preHandle(request, response, new Object()));

        HttpSession session = request.getSession(false);
        assertEquals(
                "/product/list?page=2&sort=recent",
                session.getAttribute("redirectAfterLogin"));
        assertEquals(
                "/oditji/member/login",
                response.getRedirectedUrl());
    }

    @Test
    void nullAndZeroMemberNumbersShouldBeTreatedAsLoggedOut() throws Exception {
        MemberVO nullNumberMember = new MemberVO();
        assertLoggedOutMember(nullNumberMember, null);

        MemberVO zeroNumberMember = new MemberVO();
        zeroNumberMember.setMemberNo(0L);
        assertLoggedOutMember(zeroNumberMember, "   ");
    }

    @Test
    void postRequestShouldRedirectWithoutCreatingRedirectSession() throws Exception {
        MockHttpServletRequest request =
                request("POST", "/oditji/cart/add", null);
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertNull(request.getSession(false));
        assertEquals(
                "/oditji/member/login",
                response.getRedirectedUrl());
    }

    private void assertLoggedOutMember(
            MemberVO member,
            String queryString) throws Exception {

        MockHttpServletRequest request =
                request("GET", "/oditji/favorite/list", queryString);
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.getSession().setAttribute("loginMember", member);

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(
                "/favorite/list",
                request.getSession(false)
                        .getAttribute("redirectAfterLogin"));
    }

    private MockHttpServletRequest request(
            String method,
            String requestUri,
            String queryString) {

        MockHttpServletRequest request =
                new MockHttpServletRequest();
        request.setMethod(method);
        request.setContextPath("/oditji");
        request.setRequestURI(requestUri);
        request.setQueryString(queryString);
        return request;
    }
}
