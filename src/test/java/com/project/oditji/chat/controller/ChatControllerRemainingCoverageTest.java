package com.project.oditji.chat.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.chat.service.ChatService;
import com.project.oditji.chat.service.FirebaseChatService;
import com.project.oditji.chat.vo.ChatRoomVO;

import jakarta.servlet.http.HttpSession;

/**
 * ChatControllerCoverageTest에서 남은 Firebase 연동 및 참가 표시 분기를 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class ChatControllerRemainingCoverageTest {

    @Mock
    private ChatService chatService;

    @Mock
    private FirebaseChatService firebaseChatService;

    @Mock
    private HttpSession session;

    private final Map<String, Object> sessionValues =
            new HashMap<String, Object>();

    private ChatController controller;

    @BeforeEach
    void setUp() {
        controller = new ChatController(
                chatService,
                firebaseChatService);

        lenient().when(session.getAttribute(anyString()))
                .thenAnswer(invocation ->
                        sessionValues.get(invocation.getArgument(0)));
    }

    @Test
    void roomListShouldCoverNullAndMalformedJoinedRoomEntries() {
        loginBusiness(60L, 14, "사업자");

        ChatRoomVO joined = room("joined", "PUBLIC", 14);
        ChatRoomVO notJoined = room("other", "PUBLIC", 15);
        ChatRoomVO nullId = room(null, "PUBLIC", 14);

        List<ChatRoomVO> allRooms =
                new ArrayList<ChatRoomVO>(
                        Arrays.asList(
                                null,
                                joined,
                                notJoined,
                                nullId));

        List<ChatRoomVO> joinedRooms =
                new ArrayList<ChatRoomVO>(
                        Arrays.asList(
                                null,
                                room(null, "PUBLIC", 14),
                                room("joined", "PUBLIC", 14)));

        when(chatService.getChatRoomList()).thenReturn(allRooms);
        when(chatService.getMyChatRoomList(14))
                .thenReturn(joinedRooms);

        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals(
                "chat/roomList",
                controller.roomList(session, model));

        assertSame(allRooms, model.get("roomList"));
        assertTrue(joined.isJoined());
        assertFalse(notJoined.isJoined());
        assertFalse(nullId.isJoined());
    }

    @Test
    void roomListAndMyRoomListShouldHandleNullRoomLists() {
        loginBusiness(61L, 15, "사업자");

        when(chatService.getChatRoomList()).thenReturn(null);
        when(chatService.getMyChatRoomList(15)).thenReturn(null);

        ExtendedModelMap roomListModel =
                new ExtendedModelMap();

        assertEquals(
                "chat/roomList",
                controller.roomList(session, roomListModel));
        assertNull(roomListModel.get("roomList"));

        ExtendedModelMap myRoomListModel =
                new ExtendedModelMap();

        assertEquals(
                "chat/roomList",
                controller.myRoomList(session, myRoomListModel));
        assertNull(myRoomListModel.get("roomList"));
    }

    @Test
    void roomListShouldTreatNullJoinedListAsNoJoinedRooms() {
        loginBusiness(62L, 16, "사업자");

        ChatRoomVO room = room("free", "PUBLIC", 16);

        when(chatService.getChatRoomList())
                .thenReturn(List.of(room));
        when(chatService.getMyChatRoomList(16))
                .thenReturn(null);

        controller.roomList(
                session,
                new ExtendedModelMap());

        assertFalse(room.isJoined());
    }

    @Test
    void adminShouldBeAllowedToOpenNoticeRoom() {
        loginAdmin();

        ChatRoomVO notice =
                room("notice-admin", "NOTICE", 1);

        when(chatService.getChatRoom("notice-admin"))
                .thenReturn(notice);

        ExtendedModelMap model =
                new ExtendedModelMap();

        assertEquals(
                "chat/room",
                controller.room(
                        "notice-admin",
                        session,
                        model));

        assertSame(notice, model.get("room"));
        assertEquals(
                Boolean.TRUE,
                model.get("isNoticeRoom"));
        assertEquals(
                Boolean.FALSE,
                model.get("isJoined"));

        verify(
                chatService,
                never())
                .isChatRoomMember(
                        "notice-admin",
                        1);
    }

    @Test
    void businessCreateShouldSynchronizeFirebaseRoomAndMember() {
        loginBusiness(
                71L,
                17,
                "Firebase사업자");

        when(firebaseChatService.isEnabled())
                .thenReturn(true);

        ChatRoomVO request =
                room(null, "NOTICE", 999);
        request.setMaxMember(-1);

        when(chatService.createChatRoom(request))
                .thenReturn("firebase-free");

        assertEquals(
                "redirect:/chat/room/firebase-free",
                controller.create(
                        request,
                        session));

        assertEquals(17, request.getCreatedBy());
        assertEquals("PUBLIC", request.getRoomType());
        assertEquals("N", request.getIsDefault());
        assertEquals(100, request.getMaxMember());

        verify(firebaseChatService)
                .synchronizeRoom(request);

        verify(firebaseChatService)
                .addRoomMember(
                        "firebase-free",
                        71L,
                        17,
                        "BUSINESS",
                        "Firebase사업자");
    }

    @Test
    void adminCreateShouldSynchronizeRoomWithoutAddingMember() {
        loginAdmin();

        when(firebaseChatService.isEnabled())
                .thenReturn(true);

        ChatRoomVO request =
                room(null, "PUBLIC", 99);

        when(chatService.createChatRoom(request))
                .thenReturn("firebase-notice");

        assertEquals(
                "redirect:/chat/room/firebase-notice",
                controller.create(
                        request,
                        session));

        verify(firebaseChatService)
                .synchronizeRoom(request);

        verify(
                firebaseChatService,
                never())
                .addRoomMember(
                        anyString(),
                        anyLong(),
                        anyInt(),
                        anyString(),
                        anyString());
    }

    @Test
    void createShouldReturnListWhenFirebaseSynchronizationFails() {
        loginBusiness(
                72L,
                18,
                "동기화실패사업자");

        when(firebaseChatService.isEnabled())
                .thenReturn(true);

        ChatRoomVO request =
                room(null, "PUBLIC", 18);

        when(chatService.createChatRoom(request))
                .thenReturn("firebase-error");

        doThrow(
                new IllegalStateException(
                        "firebase sync failed"))
                .when(firebaseChatService)
                .synchronizeRoom(request);

        assertEquals(
                "redirect:/chat/list",
                controller.create(
                        request,
                        session));

        verify(
                firebaseChatService,
                never())
                .addRoomMember(
                        anyString(),
                        anyLong(),
                        anyInt(),
                        anyString(),
                        anyString());
    }

    @Test
    void disabledFirebaseServiceShouldSkipCreateSynchronization() {
        loginBusiness(
                73L,
                19,
                "비활성Firebase");

        when(firebaseChatService.isEnabled())
                .thenReturn(false);

        ChatRoomVO request =
                room(null, "PUBLIC", 19);
        request.setMaxMember(30);

        when(chatService.createChatRoom(request))
                .thenReturn("no-firebase");

        assertEquals(
                "redirect:/chat/room/no-firebase",
                controller.create(
                        request,
                        session));

        verify(
                firebaseChatService,
                never())
                .synchronizeRoom(request);

        verify(
                firebaseChatService,
                never())
                .addRoomMember(
                        anyString(),
                        anyLong(),
                        anyInt(),
                        anyString(),
                        anyString());
    }

    @Test
    void deleteShouldDeactivateFirebaseBeforeOracleDelete() {
        loginBusiness(
                80L,
                20,
                "삭제사업자");

        when(firebaseChatService.isEnabled())
                .thenReturn(true);

        ChatRoomVO ownRoom =
                room("delete-firebase", "PUBLIC", 20);

        when(chatService.getChatRoom("delete-firebase"))
                .thenReturn(ownRoom);
        when(chatService.deleteChatRoom("delete-firebase"))
                .thenReturn(true);

        assertEquals(
                "redirect:/chat/list",
                controller.delete(
                        "delete-firebase",
                        session));

        InOrder inOrder =
                inOrder(
                        firebaseChatService,
                        chatService);

        inOrder.verify(firebaseChatService)
                .deactivateRoom("delete-firebase");
        inOrder.verify(chatService)
                .deleteChatRoom("delete-firebase");
    }

    private void loginAdmin() {
        sessionValues.put("memberNo", 1L);
        sessionValues.put("role", "ADMIN");
    }

    private void loginBusiness(
            Long memberNo,
            int businessNo,
            String businessName) {

        sessionValues.put(
                "loginMemberNo",
                memberNo);
        sessionValues.put(
                "businessNo",
                businessNo);
        sessionValues.put(
                "role",
                "BUSINESS");
        sessionValues.put(
                "businessName",
                businessName);
    }

    private ChatRoomVO room(
            String roomId,
            String roomType,
            int createdBy) {

        ChatRoomVO room =
                new ChatRoomVO();

        room.setRoomId(roomId);
        room.setRoomType(roomType);
        room.setCreatedBy(createdBy);

        return room;
    }
}
