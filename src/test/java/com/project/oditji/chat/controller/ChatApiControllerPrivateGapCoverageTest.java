package com.project.oditji.chat.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.chat.service.ChatService;
import com.project.oditji.chat.service.FirebaseChatService;
import com.project.oditji.chat.vo.ChatRoomVO;

/** 채팅 API의 Firebase null guard와 사용자별 접근 가능 방 helper 잔여 분기를 보완합니다. */
class ChatApiControllerPrivateGapCoverageTest {

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
    void firebaseEnabledShouldCoverNullServiceDisabledAndEnabledBranches() {
        ChatApiController withoutFirebase = new ChatApiController(chatService);
        assertFalse(Boolean.TRUE.equals(
                ReflectionTestUtils.invokeMethod(withoutFirebase, "isFirebaseChatEnabled")));

        when(firebaseChatService.isEnabled()).thenReturn(false).thenReturn(true);
        assertFalse(Boolean.TRUE.equals(
                ReflectionTestUtils.invokeMethod(controller, "isFirebaseChatEnabled")));
        assertTrue(Boolean.TRUE.equals(
                ReflectionTestUtils.invokeMethod(controller, "isFirebaseChatEnabled")));
    }

    @Test
    void accessibleRoomShouldCoverNoAccessMissingNoticeBusinessMembershipAndAdminRestriction() {
        MockHttpSession anonymous = new MockHttpSession();
        assertNull(accessible("room", anonymous));

        MockHttpSession business = businessSession();
        when(chatService.getChatRoom("missing")).thenReturn(null);
        assertNull(accessible("missing", business));

        ChatRoomVO notice = room("NOTICE");
        when(chatService.getChatRoom("notice")).thenReturn(notice);
        assertSame(notice, accessible("notice", business));

        ChatRoomVO publicRoom = room("PUBLIC");
        when(chatService.getChatRoom("public")).thenReturn(publicRoom);
        when(chatService.isChatRoomMember("public", 20)).thenReturn(false).thenReturn(true);
        assertNull(accessible("public", business));
        assertSame(publicRoom, accessible("public", business));

        MockHttpSession admin = adminSession();
        when(chatService.getChatRoom("admin-public")).thenReturn(publicRoom);
        when(chatService.getChatRoom("admin-notice")).thenReturn(notice);
        assertNull(accessible("admin-public", admin));
        assertSame(notice, accessible("admin-notice", admin));
    }

    private ChatRoomVO accessible(String roomId, MockHttpSession session) {
        return ReflectionTestUtils.invokeMethod(controller, "getAccessibleRoom", roomId, session);
    }

    private MockHttpSession businessSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("businessNo", 20);
        session.setAttribute("role", "BUSINESS");
        return session;
    }

    private MockHttpSession adminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("memberNo", 1L);
        session.setAttribute("role", "ADMIN");
        return session;
    }

    private ChatRoomVO room(String type) {
        ChatRoomVO room = new ChatRoomVO();
        room.setRoomId("room");
        room.setRoomType(type);
        return room;
    }
}
