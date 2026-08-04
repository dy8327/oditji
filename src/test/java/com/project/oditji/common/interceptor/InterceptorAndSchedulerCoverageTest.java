package com.project.oditji.common.interceptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.admin.scheduler.MemberDeleteScheduler;
import com.project.oditji.admin.service.AdminService;
import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.scheduler.EventStatusScheduler;
import com.project.oditji.common.dao.AccessLogDAO;
import com.project.oditji.common.vo.AccessLogVO;
import com.project.oditji.content.scheduler.ContentViewHistoryCleanupScheduler;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** 로그인·권한 인터셉터와 정리 스케줄러의 주요 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class InterceptorAndSchedulerCoverageTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private AccessLogDAO accessLogDAO;
    @Mock
    private AdminService adminService;
    @Mock
    private BusinessDAO businessDAO;
    @Mock
    private ContentService contentService;

    @BeforeEach
    void setUp() {
        lenient().when(request.getContextPath()).thenReturn("/oditji");
        lenient().when(request.getRequestURI())
                .thenReturn("/oditji/member/mypage");
    }

    @Test
    void loginInterceptorShouldAllowValidMemberAndRedirectAnonymousGet()
            throws Exception {
        LoginCheckInterceptor interceptor = new LoginCheckInterceptor();
        MemberVO member = member(10L, "USER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("loginMember")).thenReturn(member);

        assertTrue(interceptor.preHandle(request, response, new Object()));

        when(session.getAttribute("loginMember")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(session);
        when(request.getQueryString()).thenReturn("page=2");

        assertFalse(interceptor.preHandle(request, response, new Object()));
        verify(session).setAttribute(
                "redirectAfterLogin",
                "/member/mypage?page=2");
        verify(response).sendRedirect("/oditji/member/login");
    }

    @Test
    void loginInterceptorShouldRejectInvalidSessionMemberWithoutSavingPostPath()
            throws Exception {
        LoginCheckInterceptor interceptor = new LoginCheckInterceptor();
        MemberVO member = member(0L, "USER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("loginMember")).thenReturn(member);
        when(request.getMethod()).thenReturn("POST");

        assertFalse(interceptor.preHandle(request, response, new Object()));
        verify(response).sendRedirect("/oditji/member/login");
    }

    @Test
    void adminInterceptorShouldHandleAnonymousUserAndAdmin() throws Exception {
        AdminCheckInterceptor interceptor = new AdminCheckInterceptor();
        when(request.getSession(false)).thenReturn(null);
        assertFalse(interceptor.preHandle(request, response, new Object()));
        verify(response).sendRedirect("/oditji/member/login");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("loginMember"))
                .thenReturn(member(2L, "USER"))
                .thenReturn(member(1L, "ADMIN"));
        assertFalse(interceptor.preHandle(request, response, new Object()));
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void businessInterceptorShouldAllowApprovedBusinessAndAdminChat()
            throws Exception {
        BusinessCheckInterceptor interceptor = new BusinessCheckInterceptor();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("loginMember"))
                .thenReturn(member(1L, "ADMIN"));
        when(request.getRequestURI()).thenReturn("/oditji/chat/list");

        assertTrue(interceptor.preHandle(request, response, new Object()));

        when(session.getAttribute("loginMember"))
                .thenReturn(member(2L, "BUSINESS"));
        when(session.getAttribute("businessNo")).thenReturn(20L);
        when(session.getAttribute("businessStatus")).thenReturn("APPROVED");
        when(request.getRequestURI()).thenReturn("/oditji/business/dashboard");
        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void businessInterceptorShouldRejectAnonymousAndUnapprovedBusiness()
            throws Exception {
        BusinessCheckInterceptor interceptor = new BusinessCheckInterceptor();
        when(request.getSession(false)).thenReturn(null);
        assertFalse(interceptor.preHandle(request, response, new Object()));
        verify(response).sendRedirect("/oditji/member/login");

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("loginMember"))
                .thenReturn(member(2L, "BUSINESS"));
        when(session.getAttribute("businessStatus")).thenReturn("WAITING");
        assertFalse(interceptor.preHandle(request, response, new Object()));
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void accessLogInterceptorShouldResolveForwardedAndRemoteIpAndIgnoreFailure()
            throws Exception {
        AccessLogInterceptor interceptor = new AccessLogInterceptor(accessLogDAO);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("loginMember")).thenReturn(member(10L, "USER"));
        when(request.getHeader("X-Forwarded-For"))
                .thenReturn("1.2.3.4, 5.6.7.8");
        when(request.getHeader("User-Agent")).thenReturn("JUnit");

        assertTrue(interceptor.preHandle(request, response, new Object()));
        ArgumentCaptor<AccessLogVO> captor =
                ArgumentCaptor.forClass(AccessLogVO.class);
        verify(accessLogDAO).insertAccessLog(captor.capture());
        assertTrue(Long.valueOf(10L).equals(captor.getValue().getMemberNo()));
        assertTrue("1.2.3.4".equals(captor.getValue().getAccessIp()));

        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        doThrow(new IllegalStateException("DB 오류"))
                .when(accessLogDAO).insertAccessLog(any());
        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void schedulersShouldDelegateAndCleanupShouldSwallowFailure() {
        new MemberDeleteScheduler(adminService)
                .deleteExpiredWithdrawMembers();
        verify(adminService).deleteExpiredWithdrawMembers();

        when(businessDAO.updateExpiredEventStatus())
                .thenReturn(2)
                .thenReturn(0);
        EventStatusScheduler eventScheduler =
                new EventStatusScheduler(businessDAO);
        eventScheduler.endExpiredEvents();
        eventScheduler.endExpiredEvents();
        verify(businessDAO, org.mockito.Mockito.times(2))
                .updateExpiredEventStatus();

        when(contentService.deleteExpiredContentViewHistory())
                .thenReturn(3)
                .thenThrow(new IllegalStateException("삭제 실패"));
        ContentViewHistoryCleanupScheduler cleanupScheduler =
                new ContentViewHistoryCleanupScheduler(contentService);
        cleanupScheduler.cleanupOnApplicationReady();
        cleanupScheduler.cleanupEveryDay();
        verify(contentService, org.mockito.Mockito.times(2))
                .deleteExpiredContentViewHistory();
    }

    private MemberVO member(Long memberNo, String role) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        member.setRole(role);
        return member;
    }
}
