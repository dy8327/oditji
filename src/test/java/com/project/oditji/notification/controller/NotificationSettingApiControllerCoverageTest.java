package com.project.oditji.notification.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.notification.vo.NotificationSettingItemVO;
import com.project.oditji.notification.vo.NotificationSettingUpdateVO;

/**
 * [알림 수신 설정 추가]
 * 비로그인 차단, 정상 조회/저장, 잘못된 카테고리 입력 분기를 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class NotificationSettingApiControllerCoverageTest {

    @Mock
    private NotificationService notificationService;

    private NotificationSettingApiController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationSettingApiController(notificationService);
    }

    private MockHttpSession loginSession(long memberNo) {
        MockHttpSession session = new MockHttpSession();
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        session.setAttribute("loginMember", member);
        return session;
    }

    @Test
    void getSettingListShouldReturnUnauthorizedWhenNotLoggedIn() {

        ResponseEntity<Map<String, Object>> response =
                controller.getSettingList(new MockHttpSession());

        assertEquals(401, response.getStatusCode().value());
        assertFalse((Boolean) response.getBody().get("success"));
        verifyNoInteractions(notificationService);
    }

    @Test
    void getSettingListShouldReturnItemsForLoginMember() {

        List<NotificationSettingItemVO> items = List.of(
                new NotificationSettingItemVO("RESTOCK", "재입고 알림", "설명", true));
        when(notificationService.getSettingItems(1L)).thenReturn(items);

        ResponseEntity<Map<String, Object>> response =
                controller.getSettingList(loginSession(1L));

        assertEquals(200, response.getStatusCode().value());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals(items, response.getBody().get("settingList"));
    }

    @Test
    void updateSettingShouldReturnUnauthorizedWhenNotLoggedIn() {

        NotificationSettingUpdateVO request = new NotificationSettingUpdateVO();
        request.setNoticeCategory("RESTOCK");
        request.setEnabled(false);

        ResponseEntity<Map<String, Object>> response =
                controller.updateSetting(request, new MockHttpSession());

        assertEquals(401, response.getStatusCode().value());
        verifyNoInteractions(notificationService);
    }

    @Test
    void updateSettingShouldReturnBadRequestForInvalidCategory() {

        NotificationSettingUpdateVO request = new NotificationSettingUpdateVO();
        request.setNoticeCategory("INVALID");
        request.setEnabled(true);

        org.mockito.Mockito.doThrow(new IllegalArgumentException("올바르지 않은 알림 카테고리입니다."))
                .when(notificationService)
                .updateSetting(1L, "INVALID", true);

        ResponseEntity<Map<String, Object>> response =
                controller.updateSetting(request, loginSession(1L));

        assertEquals(400, response.getStatusCode().value());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    void updateSettingShouldReturnSuccessForValidCategory() {

        NotificationSettingUpdateVO request = new NotificationSettingUpdateVO();
        request.setNoticeCategory("RESTOCK");
        request.setEnabled(false);

        ResponseEntity<Map<String, Object>> response =
                controller.updateSetting(request, loginSession(1L));

        assertEquals(200, response.getStatusCode().value());
        assertTrue((Boolean) response.getBody().get("success"));
        org.mockito.Mockito.verify(notificationService).updateSetting(1L, "RESTOCK", false);
    }
}
