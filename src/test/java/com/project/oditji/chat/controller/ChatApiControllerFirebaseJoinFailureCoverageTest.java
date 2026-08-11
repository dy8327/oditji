package com.project.oditji.chat.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import com.project.oditji.chat.service.ChatService;
import com.project.oditji.chat.service.FirebaseChatService;
import com.project.oditji.chat.vo.ChatResponseVO;
import com.project.oditji.chat.common.ChatResult;
import com.project.oditji.chat.vo.ChatRoomVO;

/** 자유방 참가 시 Firebase 동기화 실패와 비활성화 short-circuit 분기를 보완합니다. */
class ChatApiControllerFirebaseJoinFailureCoverageTest {

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
    void joinShouldReturnSafeFailureWhenFirebaseRoomSynchronizationFails() {
        MockHttpSession session = businessSession();
        ChatRoomVO room = room();

        when(chatService.getChatRoom("room")).thenReturn(room);
        when(chatService.joinChatRoom("room", 20)).thenReturn(ChatResult.SUCCESS);
        when(firebaseChatService.isEnabled()).thenReturn(true);
        doThrow(new IllegalStateException("firebase"))
                .when(firebaseChatService)
                .synchronizeRoom(room);

        ChatResponseVO result = controller.joinRoom("room", session);

        assertFalse(result.isSuccess());
        assertEquals(ChatResult.FAIL, result.getCode());
        assertEquals(
                "채팅방 권한 동기화에 실패했습니다. 다시 시도해주세요.",
                result.getMessage());
    }

    @Test
    void alreadyJoinedShouldSkipFirebaseCallsWhenFirebaseIsDisabled() {
        MockHttpSession session = businessSession();
        ChatRoomVO room = room();

        when(chatService.getChatRoom("room")).thenReturn(room);
        when(chatService.joinChatRoom("room", 20)).thenReturn(ChatResult.ALREADY_JOINED);
        when(firebaseChatService.isEnabled()).thenReturn(false);

        ChatResponseVO result = controller.joinRoom("room", session);

        assertTrue(result.isSuccess());
        assertEquals(ChatResult.ALREADY_JOINED, result.getCode());
        assertEquals("이미 참가한 채팅방입니다.", result.getMessage());
        verify(firebaseChatService, never()).synchronizeRoom(room);
    }

    private MockHttpSession businessSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("memberNo", 10L);
        session.setAttribute("businessNo", 20);
        session.setAttribute("role", "BUSINESS");
        session.setAttribute("businessName", "사업자");
        return session;
    }

    private ChatRoomVO room() {
        ChatRoomVO room = new ChatRoomVO();
        room.setRoomId("room");
        room.setRoomType("PUBLIC");
        return room;
    }
}
