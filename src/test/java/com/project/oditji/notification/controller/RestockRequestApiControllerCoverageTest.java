package com.project.oditji.notification.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.notification.service.RestockRequestService;

/**
 * 재입고 알림 API의 로그인 여부와 상품/옵션별 위임 분기를 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class RestockRequestApiControllerCoverageTest {

    private static final Long MEMBER_NO = 10L;
    private static final Long PRODUCT_NO = 20L;
    private static final Long OPTION_NO = 30L;
    private static final String KEY_LOGIN = "login";
    private static final String KEY_REQUESTED = "requested";
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_MESSAGE = "message";

    @Mock
    private RestockRequestService restockRequestService;

    private RestockRequestApiController controller;

    @BeforeEach
    void setUp() {
        controller = new RestockRequestApiController(restockRequestService);
    }

    @Test
    void getRequestStatusShouldReturnLoggedOutStateWithoutCallingService() {
        MockHttpSession session = new MockHttpSession();

        ResponseEntity<Map<String, Object>> response =
                controller.getRequestStatus(PRODUCT_NO, null, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get(KEY_LOGIN));
        assertFalse((Boolean) response.getBody().get(KEY_REQUESTED));
        verifyNoInteractions(restockRequestService);
    }

    @Test
    void getRequestStatusShouldReturnProductRequestStateForLoggedInMember() {
        MockHttpSession session = loginSession(MEMBER_NO);
        when(restockRequestService.isRequested(MEMBER_NO, PRODUCT_NO, null))
                .thenReturn(true);

        ResponseEntity<Map<String, Object>> response =
                controller.getRequestStatus(PRODUCT_NO, null, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get(KEY_LOGIN));
        assertTrue((Boolean) response.getBody().get(KEY_REQUESTED));
        verify(restockRequestService).isRequested(MEMBER_NO, PRODUCT_NO, null);
    }

    @Test
    void getRequestStatusShouldDelegateOptionNumberAndReturnFalseState() {
        MockHttpSession session = loginSession(MEMBER_NO);
        when(restockRequestService.isRequested(MEMBER_NO, PRODUCT_NO, OPTION_NO))
                .thenReturn(false);

        ResponseEntity<Map<String, Object>> response =
                controller.getRequestStatus(PRODUCT_NO, OPTION_NO, session);

        assertTrue((Boolean) response.getBody().get(KEY_LOGIN));
        assertFalse((Boolean) response.getBody().get(KEY_REQUESTED));
        verify(restockRequestService).isRequested(MEMBER_NO, PRODUCT_NO, OPTION_NO);
    }

    @Test
    void requestRestockShouldReturnUnauthorizedWhenMemberIsNotLoggedIn() {
        MockHttpSession session = new MockHttpSession();

        ResponseEntity<Map<String, Object>> response =
                controller.requestRestock(PRODUCT_NO, OPTION_NO, session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get(KEY_SUCCESS));
        assertEquals("로그인이 필요합니다.", response.getBody().get(KEY_MESSAGE));
        verifyNoInteractions(restockRequestService);
    }

    @Test
    void requestRestockShouldDelegateProductRequestAndReturnSuccess() {
        MockHttpSession session = loginSession(MEMBER_NO);

        ResponseEntity<Map<String, Object>> response =
                controller.requestRestock(PRODUCT_NO, null, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get(KEY_SUCCESS));
        assertTrue((Boolean) response.getBody().get(KEY_REQUESTED));
        assertEquals("재입고 알림 신청이 완료되었습니다.", response.getBody().get(KEY_MESSAGE));
        verify(restockRequestService)
                .requestRestockNotification(MEMBER_NO, PRODUCT_NO, null);
    }

    @Test
    void requestRestockShouldDelegateOptionRequest() {
        MockHttpSession session = loginSession(MEMBER_NO);

        controller.requestRestock(PRODUCT_NO, OPTION_NO, session);

        verify(restockRequestService)
                .requestRestockNotification(MEMBER_NO, PRODUCT_NO, OPTION_NO);
    }

    @Test
    void cancelRestockShouldReturnUnauthorizedWhenMemberIsNotLoggedIn() {
        MockHttpSession session = new MockHttpSession();

        ResponseEntity<Map<String, Object>> response =
                controller.cancelRestock(PRODUCT_NO, null, session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get(KEY_SUCCESS));
        assertEquals("로그인이 필요합니다.", response.getBody().get(KEY_MESSAGE));
        verifyNoInteractions(restockRequestService);
    }

    @Test
    void cancelRestockShouldDelegateProductRequestAndReturnSuccess() {
        MockHttpSession session = loginSession(MEMBER_NO);

        ResponseEntity<Map<String, Object>> response =
                controller.cancelRestock(PRODUCT_NO, null, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get(KEY_SUCCESS));
        assertFalse((Boolean) response.getBody().get(KEY_REQUESTED));
        assertEquals("재입고 알림 신청이 취소되었습니다.", response.getBody().get(KEY_MESSAGE));
        verify(restockRequestService)
                .cancelRestockNotification(MEMBER_NO, PRODUCT_NO, null);
    }

    @Test
    void cancelRestockShouldDelegateOptionRequest() {
        MockHttpSession session = loginSession(MEMBER_NO);

        controller.cancelRestock(PRODUCT_NO, OPTION_NO, session);

        verify(restockRequestService)
                .cancelRestockNotification(MEMBER_NO, PRODUCT_NO, OPTION_NO);
    }

    private MockHttpSession loginSession(Long memberNo) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginMember", member);
        return session;
    }
}
