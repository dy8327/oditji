package com.project.oditji.chat.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.chat.service.ChatService;
import com.project.oditji.chat.service.FirebaseChatService;
import com.project.oditji.chat.vo.ChatRoomVO;

import jakarta.servlet.http.HttpSession;

/** 채팅 삭제 권한과 Firebase 활성화 helper의 잔여 조건을 보완합니다. */
class ChatControllerResidualConditionClosure2Test {

    private ChatService chatService;
    private HttpSession session;
    private Map<String, Object> sessionValues;

    @BeforeEach
    void setUp() {
        chatService = mock(ChatService.class);
        session = mock(HttpSession.class);
        sessionValues = new HashMap<String, Object>();
        lenient().when(session.getAttribute(anyString()))
                .thenAnswer(invocation ->
                        sessionValues.get(invocation.getArgument(0)));
    }

    @Test
    void businessShouldNotDeleteNoticeRoomEvenWhenCreatorMatches() {
        ChatController controller = new ChatController(chatService);
        sessionValues.put("loginMemberNo", 90L);
        sessionValues.put("businessNo", 30);
        sessionValues.put("role", "BUSINESS");

        ChatRoomVO noticeRoom = new ChatRoomVO();
        noticeRoom.setRoomId("notice-business");
        noticeRoom.setRoomType("NOTICE");
        noticeRoom.setCreatedBy(30);

        when(chatService.getChatRoom("notice-business"))
                .thenReturn(noticeRoom);

        assertEquals(
                "redirect:/chat/list",
                controller.delete(
                        "notice-business",
                        session));

        verify(chatService, never())
                .deleteChatRoom("notice-business");
    }

    @Test
    void firebaseEnabledHelperShouldCoverNullDisabledAndEnabledServices() {
        ChatController nullFirebaseController =
                new ChatController(chatService);

        Boolean nullResult = ReflectionTestUtils.invokeMethod(
                nullFirebaseController,
                "isFirebaseChatEnabled");
        assertFalse(nullResult.booleanValue());

        FirebaseChatService firebaseChatService =
                mock(FirebaseChatService.class);
        ChatController firebaseController =
                new ChatController(
                        chatService,
                        firebaseChatService);

        when(firebaseChatService.isEnabled())
                .thenReturn(false, true);

        Boolean disabledResult = ReflectionTestUtils.invokeMethod(
                firebaseController,
                "isFirebaseChatEnabled");
        Boolean enabledResult = ReflectionTestUtils.invokeMethod(
                firebaseController,
                "isFirebaseChatEnabled");

        assertFalse(disabledResult.booleanValue());
        assertTrue(enabledResult.booleanValue());
    }
}
