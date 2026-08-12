package com.project.oditji.chat.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.project.oditji.chat.dao.ChatDAO;
import com.project.oditji.chat.vo.ChatNotificationRoomVO;
import com.project.oditji.chat.vo.ChatRoomVO;

/**
 * 채팅 서비스에 남은 관리자 여부와 마지막 참가자 방 비활성화 성공 분기를 검증합니다.
 */
class ChatServiceImplUltimateResidualCoverageTest {

    private ChatDAO chatDAO;
    private ChatServiceImpl service;

    @BeforeEach
    void setUp() {
        chatDAO = mock(ChatDAO.class);
        service = new ChatServiceImpl(chatDAO);
    }

    @Test
    void notificationRoomsShouldPassNonAdminFlagAsZero() {
        List<ChatNotificationRoomVO> rooms = List.of(new ChatNotificationRoomVO());
        when(chatDAO.selectNotificationRoomList(10L, 3, 0)).thenReturn(rooms);

        assertSame(rooms, service.getNotificationRoomList(10L, 3, false));

        verify(chatDAO).selectNotificationRoomList(10L, 3, 0);
    }

    @Test
    void lastPublicParticipantShouldDeactivateRoomSuccessfully() {
        ChatRoomVO room = new ChatRoomVO();
        room.setRoomType("PUBLIC");

        when(chatDAO.selectChatRoom("ROOM_LAST")).thenReturn(room);
        when(chatDAO.deleteChatRoomMember("ROOM_LAST", 7)).thenReturn(1);
        when(chatDAO.countChatRoomMembers("ROOM_LAST")).thenReturn(0);
        when(chatDAO.deleteChatRoom("ROOM_LAST")).thenReturn(1);

        assertTrue(service.leaveChatRoom("ROOM_LAST", 7));

        verify(chatDAO).deleteChatRoom("ROOM_LAST");
    }
}
