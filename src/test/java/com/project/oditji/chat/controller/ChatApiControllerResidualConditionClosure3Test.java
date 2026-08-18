package com.project.oditji.chat.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import com.project.oditji.chat.service.ChatService;
import com.project.oditji.chat.vo.ChatNotificationContextVO;

/** 채팅 알림 context의 access/memberNo OR 조건 잔여 피연산자를 보완합니다. */
class ChatApiControllerResidualConditionClosure3Test {

    private ChatService chatService;
    private ChatApiController controller;

    @BeforeEach
    void setUp() {
        chatService = mock(ChatService.class);
        controller = new ChatApiController(chatService);
    }

    @Test
    void notificationContextShouldRejectMissingMemberAfterBusinessAccessSucceeds() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("businessNo", 20);
        session.setAttribute("role", "BUSINESS");

        ChatNotificationContextVO result = controller.getNotificationContext(session);

        assertFalse(result.isEnabled());
        assertTrue(result.getRoomList().isEmpty());
        verifyNoInteractions(chatService);
    }
}
