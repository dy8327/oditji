package com.project.oditji.chat.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.project.oditji.chat.service.ChatService;
import com.project.oditji.chat.service.FirebaseChatService;
import com.project.oditji.chat.vo.ChatRoomVO;
import com.project.oditji.chat.vo.FirebaseChatTokenVO;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Firebase 토큰 필드 검증과 자유방 접근의 단락 조건을 보완합니다.
 */
class ChatApiControllerFinalConditionCoverageTest {

    private ChatService chatService;
    private FirebaseChatService firebaseChatService;
    private ChatApiController controller;

    @BeforeEach
    void setUp() {
        chatService = mock(ChatService.class);
        firebaseChatService = mock(FirebaseChatService.class);
        controller = new ChatApiController(chatService, firebaseChatService);
        when(firebaseChatService.isEnabled()).thenReturn(true);
    }

    @Test
    void tokenShouldRejectMissingMemberAfterBusinessAccessWasResolved() {
        HttpSession session = mock(HttpSession.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(session.getAttribute("businessNo")).thenReturn(20);

        FirebaseChatTokenVO result = controller.getFirebaseToken(session, response);

        assertFalse(result.isSuccess());
    }

    @Test
    void tokenShouldRejectMissingRoleAfterMemberAndBusinessAreResolved() {
        HttpSession session = mock(HttpSession.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(session.getAttribute("memberNo")).thenReturn(10L);
        when(session.getAttribute("businessNo")).thenReturn(20);

        FirebaseChatTokenVO result = controller.getFirebaseToken(session, response);

        assertFalse(result.isSuccess());
    }

    @Test
    void tokenShouldReachMissingBusinessOperandAfterAccessCheck() {
        HttpSession session = mock(HttpSession.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(session.getAttribute("memberNo")).thenReturn(10L);
        when(session.getAttribute("role")).thenReturn("BUSINESS");
        when(session.getAttribute("businessNo")).thenReturn(20).thenReturn((Object) null);

        FirebaseChatTokenVO result = controller.getFirebaseToken(session, response);

        assertFalse(result.isSuccess());
    }

    @Test
    void publicRoomShouldReturnNullWhenBusinessNumberDisappearsAfterAccessCheck() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("memberNo")).thenReturn(10L);
        when(session.getAttribute("role")).thenReturn("BUSINESS");
        when(session.getAttribute("businessNo")).thenReturn(20).thenReturn((Object) null);

        ChatRoomVO room = new ChatRoomVO();
        room.setRoomType("PUBLIC");
        when(chatService.getChatRoom("ROOM_1")).thenReturn(room);

        assertNull(controller.getRoom("ROOM_1", session));
    }
}
