package com.project.oditji.subscription.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.subscription.service.SubscriptionCalculatorService;
import com.project.oditji.subscription.vo.SubscriptionSavedResultVO;

import jakarta.servlet.http.HttpSession;

/** 마이페이지 구독 계산 결과 조회/삭제 API의 인증 및 결과 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class SubscriptionApiControllerSavedResultCoverageTest {

    private static final Long MEMBER_NO = 41L;
    private static final String RESULT_ID = "SUBS_CONTROLLER_1";
    private static final String SESSION_LOGIN_MEMBER = "loginMember";
    private static final String RESPONSE_SUCCESS = "success";
    private static final String RESPONSE_MESSAGE = "message";

    @Mock
    private SearchContentPageCacheService searchContentPageCacheService;

    @Mock
    private SubscriptionCalculatorService subscriptionCalculatorService;

    @Mock
    private HttpSession session;

    private SubscriptionApiController controller;

    @BeforeEach
    void setUp() {
        controller = new SubscriptionApiController(
                searchContentPageCacheService,
                subscriptionCalculatorService);
    }

    @Test
    void getSavedResultsShouldReturnUnauthorizedWhenSessionHasNoLoginMember() {
        when(session.getAttribute(SESSION_LOGIN_MEMBER)).thenReturn(null);

        ResponseEntity<Map<String, Object>> response =
                controller.getSavedResults(session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody().get(RESPONSE_SUCCESS));
        assertEquals("로그인이 필요합니다.", response.getBody().get(RESPONSE_MESSAGE));

        verify(subscriptionCalculatorService, never())
                .getSavedResultsByMember(MEMBER_NO);
    }

    @Test
    void getSavedResultsShouldReturnMemberResults() {
        loginAs(MEMBER_NO);

        SubscriptionSavedResultVO savedResult = new SubscriptionSavedResultVO();
        savedResult.setResultId(RESULT_ID);
        List<SubscriptionSavedResultVO> expected = List.of(savedResult);

        when(subscriptionCalculatorService.getSavedResultsByMember(MEMBER_NO))
                .thenReturn(expected);

        ResponseEntity<Map<String, Object>> response =
                controller.getSavedResults(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody().get(RESPONSE_SUCCESS));
        assertSame(expected, response.getBody().get("savedResultList"));
    }

    @Test
    void deleteSavedResultShouldReturnUnauthorizedWhenSessionHasNoLoginMember() {
        when(session.getAttribute(SESSION_LOGIN_MEMBER)).thenReturn(null);

        ResponseEntity<Map<String, Object>> response =
                controller.deleteSavedResult(RESULT_ID, session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody().get(RESPONSE_SUCCESS));
        assertEquals("로그인이 필요합니다.", response.getBody().get(RESPONSE_MESSAGE));

        verify(subscriptionCalculatorService, never())
                .removeSavedResult(RESULT_ID, MEMBER_NO);
    }

    @Test
    void deleteSavedResultShouldReturnNotFoundWhenServiceCannotDelete() {
        loginAs(MEMBER_NO);
        when(subscriptionCalculatorService.removeSavedResult(RESULT_ID, MEMBER_NO))
                .thenReturn(false);

        ResponseEntity<Map<String, Object>> response =
                controller.deleteSavedResult(RESULT_ID, session);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody().get(RESPONSE_SUCCESS));
        assertEquals(
                "삭제할 저장 결과를 찾을 수 없습니다.",
                response.getBody().get(RESPONSE_MESSAGE));
    }

    @Test
    void deleteSavedResultShouldReturnOkWhenServiceDeletesResult() {
        loginAs(MEMBER_NO);
        when(subscriptionCalculatorService.removeSavedResult(RESULT_ID, MEMBER_NO))
                .thenReturn(true);

        ResponseEntity<Map<String, Object>> response =
                controller.deleteSavedResult(RESULT_ID, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody().get(RESPONSE_SUCCESS));
        assertEquals("저장 결과를 삭제했습니다.", response.getBody().get(RESPONSE_MESSAGE));
    }

    private void loginAs(Long memberNo) {
        MemberVO loginMember = new MemberVO();
        loginMember.setMemberNo(memberNo);
        when(session.getAttribute(SESSION_LOGIN_MEMBER))
                .thenReturn(loginMember);
    }
}
