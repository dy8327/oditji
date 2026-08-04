package com.project.oditji.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.chat.common.ChatResult;
import com.project.oditji.chat.dao.ChatDAO;
import com.project.oditji.chat.vo.ChatNotificationRoomVO;
import com.project.oditji.chat.vo.ChatParticipantReadVO;
import com.project.oditji.chat.vo.ChatReadStateVO;
import com.project.oditji.chat.vo.ChatRoomVO;

/** 채팅방 생성·참가·퇴장과 읽음 상태 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ChatDAO chatDAO;

    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatServiceImpl(chatDAO);
    }

    @Test
    void basicQueriesShouldDelegateToDao() {
        List<ChatRoomVO> rooms = List.of(new ChatRoomVO());
        ChatRoomVO room = new ChatRoomVO();
        when(chatDAO.selectChatRoomList()).thenReturn(rooms);
        when(chatDAO.selectMyChatRoomList(2)).thenReturn(rooms);
        when(chatDAO.selectChatRoom("ROOM_1")).thenReturn(room);

        assertSame(rooms, chatService.getChatRoomList());
        assertSame(rooms, chatService.getMyChatRoomList(2));
        assertSame(room, chatService.getChatRoom("ROOM_1"));
    }

    @Test
    void createPublicRoomShouldInsertRoomAndCreatorMembership() {
        ChatRoomVO room = createRoom("PUBLIC", 10, 100);
        when(chatDAO.getNextRoomSequence()).thenReturn(3);
        when(chatDAO.insertChatRoom(room)).thenReturn(1);
        when(chatDAO.insertChatRoomMember("ROOM_3", 7)).thenReturn(1);

        assertEquals("ROOM_3", chatService.createChatRoom(room));
        assertEquals("ROOM_3", room.getRoomId());
        verify(chatDAO).insertChatRoomMember("ROOM_3", 7);
    }

    @Test
    void createNoticeRoomShouldSkipMembershipAndHandleFailures() {
        ChatRoomVO notice = createRoom("NOTICE", 0, 100);
        when(chatDAO.getNextRoomSequence()).thenReturn(4);
        when(chatDAO.insertChatRoom(notice)).thenReturn(1);

        assertEquals("ROOM_4", chatService.createChatRoom(notice));
        verify(chatDAO, never()).insertChatRoomMember("ROOM_4", 7);

        ChatRoomVO failed = createRoom("PUBLIC", 0, 100);
        when(chatDAO.getNextRoomSequence()).thenReturn(5);
        when(chatDAO.insertChatRoom(failed)).thenReturn(0);
        assertNull(chatService.createChatRoom(failed));

        ChatRoomVO membershipFailed = createRoom("PUBLIC", 0, 100);
        when(chatDAO.getNextRoomSequence()).thenReturn(6);
        when(chatDAO.insertChatRoom(membershipFailed)).thenReturn(1);
        when(chatDAO.insertChatRoomMember("ROOM_6", 7)).thenReturn(0);
        assertThrows(
                IllegalStateException.class,
                () -> chatService.createChatRoom(membershipFailed));
    }

    @Test
    void updateAndDeleteShouldRespectDefaultRoomProtection() {
        ChatRoomVO room = new ChatRoomVO();
        when(chatDAO.updateChatRoom(room)).thenReturn(1).thenReturn(0);
        assertTrue(chatService.updateChatRoom(room));
        assertFalse(chatService.updateChatRoom(room));

        when(chatDAO.selectChatRoom("missing")).thenReturn(null);
        assertFalse(chatService.deleteChatRoom("missing"));

        ChatRoomVO defaultRoom = new ChatRoomVO();
        defaultRoom.setIsDefault("Y");
        when(chatDAO.selectChatRoom("default")).thenReturn(defaultRoom);
        assertFalse(chatService.deleteChatRoom("default"));
        verify(chatDAO, never()).deleteChatRoom("default");

        ChatRoomVO normalRoom = new ChatRoomVO();
        normalRoom.setIsDefault("N");
        when(chatDAO.selectChatRoom("normal")).thenReturn(normalRoom);
        when(chatDAO.deleteChatRoom("normal")).thenReturn(1);
        assertTrue(chatService.deleteChatRoom("normal"));
    }

    @Test
    void joinRoomShouldReturnAllDocumentedResultCodes() {
        when(chatDAO.selectChatRoom("missing")).thenReturn(null);
        assertEquals(ChatResult.ROOM_NOT_FOUND,
                chatService.joinChatRoom("missing", 1));

        ChatRoomVO notice = createRoom("NOTICE", 0, 100);
        when(chatDAO.selectChatRoom("notice")).thenReturn(notice);
        assertEquals(ChatResult.FAIL,
                chatService.joinChatRoom("notice", 1));

        ChatRoomVO room = createRoom("PUBLIC", 1, 2);
        when(chatDAO.selectChatRoom("room")).thenReturn(room);
        when(chatDAO.existsChatRoomMember("room", 1)).thenReturn(1);
        assertEquals(ChatResult.ALREADY_JOINED,
                chatService.joinChatRoom("room", 1));

        when(chatDAO.existsChatRoomMember("room", 2)).thenReturn(0);
        room.setMemberCount(2);
        assertEquals(ChatResult.ROOM_FULL,
                chatService.joinChatRoom("room", 2));

        room.setMemberCount(1);
        when(chatDAO.insertChatRoomMember("room", 2))
                .thenReturn(1)
                .thenReturn(0);
        assertEquals(ChatResult.SUCCESS,
                chatService.joinChatRoom("room", 2));
        assertEquals(ChatResult.FAIL,
                chatService.joinChatRoom("room", 2));
    }

    @Test
    void lastParticipantCheckShouldValidateRoomAndMembership() {
        when(chatDAO.selectChatRoom("missing")).thenReturn(null);
        assertFalse(chatService.willRoomBeEmptyAfterLeave("missing", 1));

        ChatRoomVO notice = createRoom("NOTICE", 0, 100);
        when(chatDAO.selectChatRoom("notice")).thenReturn(notice);
        assertFalse(chatService.willRoomBeEmptyAfterLeave("notice", 1));

        ChatRoomVO room = createRoom("PUBLIC", 0, 100);
        when(chatDAO.selectChatRoom("room")).thenReturn(room);
        when(chatDAO.existsChatRoomMember("room", 1)).thenReturn(0);
        assertFalse(chatService.willRoomBeEmptyAfterLeave("room", 1));

        when(chatDAO.existsChatRoomMember("room", 1)).thenReturn(1);
        when(chatDAO.countChatRoomMembers("room")).thenReturn(1).thenReturn(2);
        assertTrue(chatService.willRoomBeEmptyAfterLeave("room", 1));
        assertFalse(chatService.willRoomBeEmptyAfterLeave("room", 1));
    }

    @Test
    void leaveRoomShouldDeactivateLastRoomAndHandleFailure() {
        ChatRoomVO room = createRoom("PUBLIC", 0, 100);
        when(chatDAO.selectChatRoom("room")).thenReturn(room);
        when(chatDAO.deleteChatRoomMember("room", 1))
                .thenReturn(0)
                .thenReturn(1)
                .thenReturn(1);

        assertFalse(chatService.leaveChatRoom("room", 1));

        when(chatDAO.countChatRoomMembers("room")).thenReturn(2).thenReturn(0);
        assertTrue(chatService.leaveChatRoom("room", 1));

        when(chatDAO.deleteChatRoom("room")).thenReturn(0);
        assertThrows(
                IllegalStateException.class,
                () -> chatService.leaveChatRoom("room", 1));

        when(chatDAO.selectChatRoom("notice")).thenReturn(
                createRoom("NOTICE", 0, 100));
        assertFalse(chatService.leaveChatRoom("notice", 1));
    }

    @Test
    void membershipNotificationAndParticipantQueriesShouldDelegate() {
        when(chatDAO.existsChatRoomMember("room", 1)).thenReturn(1).thenReturn(0);
        assertTrue(chatService.isChatRoomMember("room", 1));
        assertFalse(chatService.isChatRoomMember("room", 1));

        List<ChatNotificationRoomVO> notificationRooms =
                List.of(new ChatNotificationRoomVO());
        when(chatDAO.selectNotificationRoomList(10L, 3, 1))
                .thenReturn(notificationRooms);
        assertSame(
                notificationRooms,
                chatService.getNotificationRoomList(10L, 3, true));

        when(chatDAO.selectChatRoom("missing")).thenReturn(null);
        assertTrue(chatService.getChatParticipantReadList("missing").isEmpty());

        List<ChatParticipantReadVO> participants =
                List.of(new ChatParticipantReadVO());
        when(chatDAO.selectChatRoom("notice")).thenReturn(
                createRoom("NOTICE", 0, 100));
        when(chatDAO.selectNoticeRoomParticipantReadList("notice"))
                .thenReturn(participants);
        assertSame(participants,
                chatService.getChatParticipantReadList("notice"));

        when(chatDAO.selectChatRoom("public")).thenReturn(
                createRoom("PUBLIC", 0, 100));
        when(chatDAO.selectPublicRoomParticipantReadList("public"))
                .thenReturn(participants);
        assertSame(participants,
                chatService.getChatParticipantReadList("public"));
    }

    @Test
    void readStateShouldValidateEveryRequiredFieldAndDelegate() {
        assertFalse(chatService.saveChatReadState(null));

        ChatReadStateVO state = new ChatReadStateVO();
        assertFalse(chatService.saveChatReadState(state));

        state.setRoomId("room");
        state.setMemberNo(10L);
        state.setLastReadMessageId("message");
        state.setLastReadEpochMs(-1L);
        assertFalse(chatService.saveChatReadState(state));

        state.setLastReadEpochMs(100L);
        when(chatDAO.mergeChatReadState(state)).thenReturn(1).thenReturn(0);
        assertTrue(chatService.saveChatReadState(state));
        assertFalse(chatService.saveChatReadState(state));

        state.setLastReadMessageId(" ");
        assertFalse(chatService.saveChatReadState(state));
    }

    private ChatRoomVO createRoom(
            String roomType,
            int memberCount,
            int maxMember) {

        ChatRoomVO room = new ChatRoomVO();
        room.setRoomType(roomType);
        room.setCreatedBy(7);
        room.setMemberCount(memberCount);
        room.setMaxMember(maxMember);
        room.setIsDefault("N");
        return room;
    }
}
