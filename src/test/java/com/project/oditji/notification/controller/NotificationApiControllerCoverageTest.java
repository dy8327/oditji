package com.project.oditji.notification.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.notification.vo.NotificationContextVO;
import com.project.oditji.notification.vo.NotificationResponseVO;
import com.project.oditji.notification.vo.NotificationVO;

/** 공통 알림 API의 비로그인, 권한 확인, 읽음 처리 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class NotificationApiControllerCoverageTest {

    @Mock
    private NotificationService notificationService;

    private NotificationApiController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationApiController(notificationService);
    }

    @Test
    void contextShouldReturnEmptyForNullOrInvalidSessionMember() {
        NotificationContextVO nullSession = controller.getContext(null);
        assertEquals(0, nullSession.getUnreadCount());
        assertTrue(nullSession.getNotificationList().isEmpty());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginMember", "not-a-member");
        NotificationContextVO wrongType = controller.getContext(session);
        assertEquals(0, wrongType.getUnreadCount());

        MemberVO member = new MemberVO();
        member.setMemberNo(0L);
        session.setAttribute("loginMember", member);
        NotificationContextVO invalidNumber = controller.getContext(session);
        assertEquals(0, invalidNumber.getUnreadCount());

        verifyNoInteractions(notificationService);
    }

    @Test
    void contextShouldDelegateForValidMember() {
        MemberVO member = member(10L);
        MockHttpSession session = session(member);
        NotificationContextVO expected = new NotificationContextVO(
                1,
                List.of(new NotificationVO()));
        when(notificationService.getUnreadContext(10L)).thenReturn(expected);

        NotificationContextVO actual = controller.getContext(session);

        assertEquals(expected, actual);
        verify(notificationService).getUnreadContext(10L);
    }

    @Test
    void markAsReadShouldRejectMissingAccess() {
        NotificationResponseVO response = controller.markAsRead(3L, null);

        assertFalse(response.isSuccess());
        assertEquals("알림 읽음 처리 권한이 없습니다.", response.getMessage());
        verifyNoInteractions(notificationService);
    }

    @Test
    void markAsReadShouldReturnUpdatedAndAlreadyReadMessages() {
        MockHttpSession session = session(member(20L));
        when(notificationService.markAsRead(7L, 20L))
                .thenReturn(true)
                .thenReturn(false);

        NotificationResponseVO updated = controller.markAsRead(7L, session);
        NotificationResponseVO unchanged = controller.markAsRead(7L, session);

        assertTrue(updated.isSuccess());
        assertEquals("알림을 읽음 처리했습니다.", updated.getMessage());
        assertFalse(unchanged.isSuccess());
        assertEquals("이미 읽었거나 존재하지 않는 알림입니다.", unchanged.getMessage());
    }

    @Test
    void markAllAsReadShouldRejectMissingAccess() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginMember", new MemberVO());

        NotificationResponseVO response = controller.markAllAsRead(session);

        assertFalse(response.isSuccess());
        assertEquals("알림 읽음 처리 권한이 없습니다.", response.getMessage());
        verifyNoInteractions(notificationService);
    }

    @Test
    void markAllAsReadShouldDelegateForValidMember() {
        MockHttpSession session = session(member(30L));

        NotificationResponseVO response = controller.markAllAsRead(session);

        assertTrue(response.isSuccess());
        assertEquals("알림을 모두 읽음 처리했습니다.", response.getMessage());
        verify(notificationService).markAllAsRead(30L);
    }

    private MemberVO member(Long memberNo) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        return member;
    }

    private MockHttpSession session(MemberVO member) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginMember", member);
        return session;
    }
}
