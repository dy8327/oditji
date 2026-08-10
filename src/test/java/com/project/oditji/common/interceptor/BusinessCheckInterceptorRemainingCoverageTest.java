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

/**
 * 사업자 인터셉터의 관리자 채팅 예외와 승인 상태 조건을 보완합니다.
 */
class BusinessCheckInterceptorRemainingCoverageTest {

    private BusinessCheckInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new BusinessCheckInterceptor();
    }

    @Test
    void anonymousRequestShouldRedirectToLogin() throws Exception {
        MockHttpServletRequest request =
                request("/oditji/business/product/list");
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(
                request,
                response,
                new Object()));

        assertEquals(
                "/oditji/member/login",
                response.getRedirectedUrl());
    }

    @Test
    void adminShouldPassExactChatAndNestedChatPaths() throws Exception {
        MemberVO admin = member(1L, "ADMIN");

        MockHttpServletRequest exactChat = request("/oditji/chat");
        exactChat.getSession().setAttribute("loginMember", admin);

        MockHttpServletResponse exactResponse =
                new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(
                exactChat,
                exactResponse,
                new Object()));
        assertNull(exactResponse.getRedirectedUrl());

        MockHttpServletRequest nestedChat =
                request("/oditji/chat/room/free");
        nestedChat.getSession().setAttribute("loginMember", admin);

        assertTrue(interceptor.preHandle(
                nestedChat,
                new MockHttpServletResponse(),
                new Object()));
    }

    @Test
    void adminOutsideChatShouldStillRequireApprovedBusinessSession() throws Exception {
        MockHttpServletRequest request =
                request("/oditji/business/product/list");
        request.getSession().setAttribute(
                "loginMember",
                member(1L, "ADMIN"));

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(
                request,
                response,
                new Object()));
        assertEquals(403, response.getStatus());
    }

    @Test
    void adminPredicateShouldCoverNullNumberWrongNumberAndWrongRole() throws Exception {
        assertForbiddenChat(member(null, "ADMIN"));
        assertForbiddenChat(member(2L, "ADMIN"));
        assertForbiddenChat(member(1L, "BUSINESS"));
    }

    @Test
    void businessShouldRequireBothBusinessNumberAndApprovedStatus() throws Exception {
        MemberVO business = member(20L, "BUSINESS");

        MockHttpServletRequest missingNumber =
                request("/oditji/business/order");
        missingNumber.getSession().setAttribute("loginMember", business);
        missingNumber.getSession().setAttribute(
                "businessStatus",
                "APPROVED");

        MockHttpServletResponse missingNumberResponse =
                new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(
                missingNumber,
                missingNumberResponse,
                new Object()));
        assertEquals(403, missingNumberResponse.getStatus());

        MockHttpServletRequest missingStatus =
                request("/oditji/business/order");
        missingStatus.getSession().setAttribute("loginMember", business);
        missingStatus.getSession().setAttribute("businessNo", 20);

        assertFalse(interceptor.preHandle(
                missingStatus,
                new MockHttpServletResponse(),
                new Object()));

        MockHttpServletRequest waiting =
                request("/oditji/business/order");
        waiting.getSession().setAttribute("loginMember", business);
        waiting.getSession().setAttribute("businessNo", 20);
        waiting.getSession().setAttribute("businessStatus", "WAITING");

        assertFalse(interceptor.preHandle(
                waiting,
                new MockHttpServletResponse(),
                new Object()));

        MockHttpServletRequest approved =
                request("/oditji/business/order");
        approved.getSession().setAttribute("loginMember", business);
        approved.getSession().setAttribute("businessNo", 20);
        approved.getSession().setAttribute(
                "businessStatus",
                "APPROVED");

        assertTrue(interceptor.preHandle(
                approved,
                new MockHttpServletResponse(),
                new Object()));
    }

    private void assertForbiddenChat(MemberVO member) throws Exception {
        MockHttpServletRequest request =
                request("/oditji/chat/list");
        request.getSession().setAttribute("loginMember", member);

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(
                request,
                response,
                new Object()));
        assertEquals(403, response.getStatus());
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request =
                new MockHttpServletRequest();
        request.setContextPath("/oditji");
        request.setRequestURI(uri);
        return request;
    }

    private MemberVO member(Long memberNo, String role) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        member.setRole(role);
        return member;
    }
}
