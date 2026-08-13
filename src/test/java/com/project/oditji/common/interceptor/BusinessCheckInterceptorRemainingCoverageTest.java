package com.project.oditji.common.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.member.vo.MemberVO;

/**
 * 사업자 인터셉터의 관리자 채팅 예외와 미승인 사업자 제한 분기를 보완합니다.
 */
class BusinessCheckInterceptorRemainingCoverageTest {

    private BusinessService businessService;
    private BusinessCheckInterceptor interceptor;

    @BeforeEach
    void setUp() {
        businessService = mock(BusinessService.class);
        interceptor = new BusinessCheckInterceptor(businessService);
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
    void adminOutsideChatShouldStillBeForbidden() throws Exception {
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
    void waitingBusinessShouldAccessMainButNotProtectedFeatures() throws Exception {
        MemberVO loginMember = member(20L, "BUSINESS");
        BusinessVO waitingBusiness = business(200L, "WAITING", "대기상점");
        when(businessService.getBusinessByMemberNo(20L)).thenReturn(waitingBusiness);

        MockHttpServletRequest mainRequest =
                request("/oditji/business/main");
        mainRequest.getSession().setAttribute("loginMember", loginMember);

        assertTrue(interceptor.preHandle(
                mainRequest,
                new MockHttpServletResponse(),
                new Object()));
        assertEquals(200L, mainRequest.getSession().getAttribute("businessNo"));
        assertEquals("대기상점", mainRequest.getSession().getAttribute("businessName"));
        assertEquals("WAITING", mainRequest.getSession().getAttribute("businessStatus"));

        MockHttpServletRequest protectedRequest =
                request("/oditji/business/product/list");
        protectedRequest.getSession().setAttribute("loginMember", loginMember);
        MockHttpServletResponse protectedResponse = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(
                protectedRequest,
                protectedResponse,
                new Object()));
        assertEquals(403, protectedResponse.getStatus());
    }

    @Test
    void rejectedBusinessShouldAlsoAccessOnlyMain() throws Exception {
        MemberVO loginMember = member(21L, "BUSINESS");
        when(businessService.getBusinessByMemberNo(21L))
                .thenReturn(business(210L, "REJECTED", "반려상점"));

        MockHttpServletRequest mainRequest = request("/oditji/business/main");
        mainRequest.getSession().setAttribute("loginMember", loginMember);
        assertTrue(interceptor.preHandle(
                mainRequest,
                new MockHttpServletResponse(),
                new Object()));

        MockHttpServletRequest chatRequest = request("/oditji/chat/list");
        chatRequest.getSession().setAttribute("loginMember", loginMember);
        MockHttpServletResponse chatResponse = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(
                chatRequest,
                chatResponse,
                new Object()));
        assertEquals(403, chatResponse.getStatus());
    }

    @Test
    void approvedBusinessShouldPassProtectedFeatures() throws Exception {
        MemberVO loginMember = member(22L, "BUSINESS");
        when(businessService.getBusinessByMemberNo(22L))
                .thenReturn(business(220L, "APPROVED", "승인상점"));

        MockHttpServletRequest request =
                request("/oditji/business/order/list");
        request.getSession().setAttribute("loginMember", loginMember);

        assertTrue(interceptor.preHandle(
                request,
                new MockHttpServletResponse(),
                new Object()));
    }

    @Test
    void missingBusinessRecordShouldBeForbidden() throws Exception {
        MemberVO loginMember = member(23L, "BUSINESS");
        when(businessService.getBusinessByMemberNo(23L)).thenReturn(null);

        MockHttpServletRequest request = request("/oditji/business/main");
        request.getSession().setAttribute("loginMember", loginMember);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(403, response.getStatus());
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

    private BusinessVO business(Long businessNo, String status, String name) {
        BusinessVO business = new BusinessVO();
        business.setBusinessNo(businessNo);
        business.setStatus(status);
        business.setBusinessName(name);
        return business;
    }
}
