package com.project.oditji.chat.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import com.project.oditji.chat.common.ChatResult;
import com.project.oditji.chat.service.ChatService;
import com.project.oditji.chat.service.FirebaseChatService;
import com.project.oditji.chat.vo.ChatResponseVO;
import com.project.oditji.chat.vo.ChatRoomVO;

/**
 * ChatApiController의 join/leave/checkLeave switch 및 삼항 분기를 보완합니다.
 */
class ChatApiControllerMoreConditionCoverageTest {

    private ChatService chatService;
    private FirebaseChatService firebaseChatService;
    private ChatApiController controller;

    @BeforeEach
    void setUp() {
        chatService = mock(ChatService.class);
        firebaseChatService = mock(FirebaseChatService.class);

        controller = new ChatApiController(
                chatService,
                firebaseChatService);
    }

    @Test
    void joinShouldCoverRoomFullNotFoundDefaultAndAlreadyJoinedSwitchCases() {
        MockHttpSession session = businessSession();

        ChatRoomVO room = room("FREE");

        when(chatService.getChatRoom("room"))
                .thenReturn(room);

        when(firebaseChatService.isEnabled())
                .thenReturn(false);

        when(chatService.joinChatRoom(
                "room",
                20))
                .thenReturn(
                        ChatResult.ROOM_FULL,
                        ChatResult.ROOM_NOT_FOUND,
                        -99,
                        ChatResult.ALREADY_JOINED);

        assertEquals(
                ChatResult.ROOM_FULL,
                controller.joinRoom(
                        "room",
                        session)
                        .getCode());

        assertEquals(
                ChatResult.ROOM_NOT_FOUND,
                controller.joinRoom(
                        "room",
                        session)
                        .getCode());

        assertEquals(
                -99,
                controller.joinRoom(
                        "room",
                        session)
                        .getCode());

        ChatResponseVO already =
                controller.joinRoom(
                        "room",
                        session);

        assertEquals(
                ChatResult.ALREADY_JOINED,
                already.getCode());
        assertEquals(
                "이미 참가한 채팅방입니다.",
                already.getMessage());
    }

    @Test
    void checkLeaveShouldCoverFalseAndTrueDeleteMessages() {
        MockHttpSession session = businessSession();
        ChatRoomVO room = room("FREE");

        when(chatService.getChatRoom("room"))
                .thenReturn(room);
        when(chatService.isChatRoomMember(
                "room",
                20))
                .thenReturn(true);
        when(chatService.willRoomBeEmptyAfterLeave(
                "room",
                20))
                .thenReturn(false, true);

        Map<String, Object> keep =
                controller.checkLeaveRoom(
                        "room",
                        session);

        assertEquals(
                Boolean.FALSE,
                keep.get("willDeleteRoom"));
        assertEquals(
                "채팅방에서 나갈 수 있습니다.",
                keep.get("message"));

        Map<String, Object> delete =
                controller.checkLeaveRoom(
                        "room",
                        session);

        assertEquals(
                Boolean.TRUE,
                delete.get("willDeleteRoom"));
        assertEquals(
                "나가면 참여자가 없어 채팅방이 삭제됩니다.",
                delete.get("message"));
    }

    @Test
    void leaveShouldCoverFirebaseRemoveAndDeactivateBranches() {
        MockHttpSession session = businessSession();
        ChatRoomVO room = room("FREE");

        room.setRoomId("room");
        room.setStatus("ACTIVE");
        room.setJoined(true);
        room.setMemberCount(2);

        when(chatService.getChatRoom("room"))
                .thenReturn(room);

        /*
         * 현재 소스에 자유방 참여 여부 선검증이 있어도
         * Firebase 분기까지 진입할 수 있도록 허용합니다.
         * 해당 선검증이 없는 버전에서도 불필요 stubbing 오류가
         * 발생하지 않도록 lenient 처리합니다.
         */
        lenient()
                .when(chatService.isChatRoomMember(
                        "room",
                        20))
                .thenReturn(true);

        when(firebaseChatService.isEnabled())
                .thenReturn(true);

        when(chatService.willRoomBeEmptyAfterLeave(
                anyString(),
                anyInt()))
                .thenReturn(
                        false,
                        true);

        lenient()
                .when(chatService.leaveChatRoom(
                        anyString(),
                        anyInt()))
                .thenReturn(true);

        doNothing()
                .when(firebaseChatService)
                .removeRoomMember(
                        anyString(),
                        anyLong());

        doNothing()
                .when(firebaseChatService)
                .deactivateRoom(
                        anyString());

        ChatResponseVO kept =
                controller.leaveRoom(
                        "room",
                        session);

        assertEquals(
                ChatResult.SUCCESS,
                kept.getCode());
        assertEquals(
                "채팅방에서 나갔습니다.",
                kept.getMessage());

        verify(firebaseChatService)
                .removeRoomMember(
                        "room",
                        10L);

        ChatResponseVO deleted =
                controller.leaveRoom(
                        "room",
                        session);

        assertEquals(
                ChatResult.SUCCESS,
                deleted.getCode());
        assertEquals(
                "채팅방에서 나갔으며, 참여자가 없어 채팅방이 삭제되었습니다.",
                deleted.getMessage());

        verify(firebaseChatService)
                .deactivateRoom("room");
    }

    private MockHttpSession businessSession() {
        MockHttpSession session =
                new MockHttpSession();

        session.setAttribute(
                "memberNo",
                10L);
        session.setAttribute(
                "businessNo",
                20);
        session.setAttribute(
                "role",
                "BUSINESS");
        session.setAttribute(
                "businessName",
                "사업자");

        return session;
    }

    private ChatRoomVO room(
            String type) {

        ChatRoomVO room =
                new ChatRoomVO();
        room.setRoomType(type);
        return room;
    }
}
