package com.project.oditji.common.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.project.oditji.common.dao.AccessLogDAO;
import com.project.oditji.common.vo.AccessLogVO;
import com.project.oditji.member.vo.MemberVO;

/**
 * 접속 로그 인터셉터의 비로그인, 프록시 IP, 길이 제한 및 예외 방어 분기를 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class AccessLogInterceptorCoverageTest {

    @Mock
    private AccessLogDAO accessLogDAO;

    private AccessLogInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new AccessLogInterceptor(accessLogDAO);
    }

    @Test
    void anonymousRequestShouldUseRemoteAddressAndAllowNullUserAgent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/content/list");
        request.setRemoteAddr("127.0.0.1");

        assertTrue(interceptor.preHandle(
                request,
                new MockHttpServletResponse(),
                new Object()));

        ArgumentCaptor<AccessLogVO> captor =
                ArgumentCaptor.forClass(AccessLogVO.class);
        verify(accessLogDAO).insertAccessLog(captor.capture());

        AccessLogVO saved = captor.getValue();
        assertNull(saved.getMemberNo());
        assertEquals("127.0.0.1", saved.getAccessIp());
        assertNull(saved.getUserAgent());
        assertEquals("/content/list", saved.getAccessUrl());
    }

    @Test
    void forwardedRequestShouldUseFirstIpAndLimitLongFields() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRemoteAddr("10.0.0.9");
        request.addHeader(
                "X-Forwarded-For",
                " 203.0.113.10 , 198.51.100.1 ");
        request.addHeader("User-Agent", "U".repeat(520));
        request.setRequestURI("/" + "a".repeat(520));

        MemberVO member = new MemberVO();
        member.setMemberNo(77L);
        request.getSession().setAttribute("loginMember", member);

        assertTrue(interceptor.preHandle(
                request,
                new MockHttpServletResponse(),
                new Object()));

        ArgumentCaptor<AccessLogVO> captor =
                ArgumentCaptor.forClass(AccessLogVO.class);
        verify(accessLogDAO).insertAccessLog(captor.capture());

        AccessLogVO saved = captor.getValue();
        assertEquals(77L, saved.getMemberNo());
        assertEquals("203.0.113.10", saved.getAccessIp());
        assertEquals(500, saved.getUserAgent().length());
        assertEquals(500, saved.getAccessUrl().length());
    }

    @Test
    void blankForwardedHeaderAndDaoFailureShouldNotBlockRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/order/create");
        request.setRemoteAddr("192.0.2.7");
        request.addHeader("X-Forwarded-For", "   ");

        doThrow(new IllegalStateException("db failure"))
                .when(accessLogDAO)
                .insertAccessLog(any(AccessLogVO.class));

        assertTrue(interceptor.preHandle(
                request,
                new MockHttpServletResponse(),
                new Object()));

        verify(accessLogDAO).insertAccessLog(any(AccessLogVO.class));
    }
}
