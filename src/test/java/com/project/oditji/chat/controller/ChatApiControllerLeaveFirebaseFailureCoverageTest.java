package com.project.oditji.chat.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import com.project.oditji.chat.common.ChatResult;
import com.project.oditji.chat.service.ChatService;
import com.project.oditji.chat.service.FirebaseChatService;
import com.project.oditji.chat.vo.ChatResponseVO;
import com.project.oditji.chat.vo.ChatRoomVO;

/** 자유방 나가기 중 Firebase 권한 해제 실패 catch 경로를 보완합니다. */
class ChatApiControllerLeaveFirebaseFailureCoverageTest {

    private ChatService chatService;
    private FirebaseChatService firebaseChatService;
    private ChatApiController controller;

    @BeforeEach
    void setUp() {
        chatService = mock(ChatService.class);
        firebaseChatService = mock(FirebaseChatService.class);
        controller = new ChatApiController(chatService, firebaseChatService);
    }

    @Test
    void leaveShouldReturnSafeFailureWhenFirebaseSystemMessageWriteFails() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("memberNo", 10L);
        session.setAttribute("businessNo", 20);
        session.setAttribute("role", "BUSINESS");
        session.setAttribute("businessName", "사업자");

        ChatRoomVO room = new ChatRoomVO();
        room.setRoomId("room");
        room.setRoomType("PUBLIC");

        when(chatService.getChatRoom("room")).thenReturn(room);
        when(chatService.isChatRoomMember("room", 20)).thenReturn(true);
        when(chatService.willRoomBeEmptyAfterLeave("room", 20)).thenReturn(false);
        when(firebaseChatService.isEnabled()).thenReturn(true);
        doThrow(new IllegalStateException("firebase"))
                .when(firebaseChatService)
                .addSystemMessage("room", "사업자님이 나갔습니다.");

        ChatResponseVO result = controller.leaveRoom("room", session);

        assertFalse(result.isSuccess());
        assertEquals(ChatResult.FAIL, result.getCode());
        assertEquals(
                "채팅방 권한 해제에 실패했습니다. 다시 시도해주세요.",
                result.getMessage());
    }
}
