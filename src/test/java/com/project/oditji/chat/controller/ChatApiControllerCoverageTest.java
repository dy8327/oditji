package com.project.oditji.chat.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.chat.common.ChatResult;
import com.project.oditji.chat.service.ChatService;
import com.project.oditji.chat.vo.ChatNotificationContextVO;
import com.project.oditji.chat.vo.ChatNotificationRoomVO;
import com.project.oditji.chat.vo.ChatParticipantReadVO;
import com.project.oditji.chat.vo.ChatReadStateVO;
import com.project.oditji.chat.vo.ChatResponseVO;
import com.project.oditji.chat.vo.ChatRoomVO;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpSession;

/** 채팅 JSON API의 권한, 읽음, 참가와 나가기 결과를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class ChatApiControllerCoverageTest {

    @Mock
    private ChatService chatService;

    @Mock
    private HttpSession session;

    private final Map<String, Object> sessionValues = new HashMap<String, Object>();

    private ChatApiController controller;

    @BeforeEach
    void setUp() {
        controller = new ChatApiController(chatService);
        when(session.getAttribute(anyString()))
                .thenAnswer(invocation -> sessionValues.get(invocation.getArgument(0)));
    }

    @Test
    void roomListsShouldRespectAnonymousAdminAndBusinessAccess() {
        assertTrue(controller.getRoomList(session).isEmpty());
        assertTrue(controller.getMyRoomList(session).isEmpty());

        ChatRoomVO notice = room("notice", "NOTICE");
        ChatRoomVO free = room("free", "PUBLIC");
        loginAdmin();
        when(chatService.getChatRoomList()).thenReturn(List.of(notice, free));
        assertEquals(List.of(notice), controller.getRoomList(session));
        assertEquals(List.of(notice), controller.getMyRoomList(session));

        sessionValues.clear();
        loginBusiness(10L, 7);
        when(chatService.getMyChatRoomList(7)).thenReturn(List.of(free));
        assertEquals(List.of(notice, free), controller.getRoomList(session));
        assertEquals(List.of(free), controller.getMyRoomList(session));
    }

    @Test
    void adminFilterShouldHandleNullAndEmptyLists() {
        loginAdmin();
        when(chatService.getChatRoomList())
                .thenReturn(null)
                .thenReturn(List.of());

        assertTrue(controller.getRoomList(session).isEmpty());
        assertTrue(controller.getRoomList(session).isEmpty());
    }

    @Test
    void notificationContextShouldReturnDisabledAndEnabledContexts() {
        ChatNotificationContextVO anonymous = controller.getNotificationContext(session);
        assertFalse(anonymous.isEnabled());
        assertTrue(anonymous.getRoomList().isEmpty());

        loginAdmin();
        ChatNotificationRoomVO notificationRoom = new ChatNotificationRoomVO();
        notificationRoom.setRoomId("notice");
        when(chatService.getNotificationRoomList(1L, 1, true))
                .thenReturn(List.of(notificationRoom));

        ChatNotificationContextVO admin = controller.getNotificationContext(session);
        assertTrue(admin.isEnabled());
        assertEquals(1L, admin.getMemberNo());
        assertEquals(1, admin.getBusinessNo());
        assertEquals("ADMIN", admin.getRole());
        assertEquals(List.of(notificationRoom), admin.getRoomList());

        sessionValues.clear();
        loginBusinessWithMemberObject(20L, 8);
        when(chatService.getNotificationRoomList(20L, 8, false)).thenReturn(List.of());
        ChatNotificationContextVO business = controller.getNotificationContext(session);
        assertTrue(business.isEnabled());
        assertEquals(20L, business.getMemberNo());
        assertEquals("BUSINESS", business.getRole());
    }

    @Test
    void accessibleRoomAndReadersShouldEnforceRoomTypeAndMembership() {
        assertNull(controller.getRoom("none", session));
        assertTrue(controller.getRoomReaders("none", session).isEmpty());

        loginAdmin();
        ChatRoomVO free = room("free", "PUBLIC");
        when(chatService.getChatRoom("free")).thenReturn(free);
        assertNull(controller.getRoom("free", session));

        ChatRoomVO notice = room("notice", "NOTICE");
        when(chatService.getChatRoom("notice")).thenReturn(notice);
        ChatParticipantReadVO reader = new ChatParticipantReadVO();
        when(chatService.getChatParticipantReadList("notice")).thenReturn(List.of(reader));
        assertSame(notice, controller.getRoom("notice", session));
        assertEquals(List.of(reader), controller.getRoomReaders("notice", session));

        sessionValues.clear();
        loginBusiness(30L, 9);
        when(chatService.getChatRoom("free")).thenReturn(free);
        when(chatService.isChatRoomMember("free", 9)).thenReturn(false, true);
        assertNull(controller.getRoom("free", session));
        assertSame(free, controller.getRoom("free", session));
    }

    @Test
    void saveReadStateShouldValidateLoginAccessAndInput() {
        ChatResponseVO anonymous = controller.saveReadState("room", "m1", 1L, session);
        assertFalse(anonymous.isSuccess());

        loginBusiness(40L, 10);
        when(chatService.getChatRoom("missing")).thenReturn(null);
        ChatResponseVO inaccessible = controller.saveReadState("missing", "m1", 1L, session);
        assertEquals("읽음 처리 권한이 없는 채팅방입니다.", inaccessible.getMessage());

        ChatRoomVO notice = room("notice", "NOTICE");
        when(chatService.getChatRoom("notice")).thenReturn(notice);
        assertEquals(
                "마지막 읽음 위치가 올바르지 않습니다.",
                controller.saveReadState("notice", null, 1L, session).getMessage());
        assertEquals(
                "마지막 읽음 위치가 올바르지 않습니다.",
                controller.saveReadState("notice", " ", 1L, session).getMessage());
        assertEquals(
                "마지막 읽음 위치가 올바르지 않습니다.",
                controller.saveReadState("notice", "x".repeat(101), 1L, session).getMessage());
        assertEquals(
                "마지막 읽음 위치가 올바르지 않습니다.",
                controller.saveReadState("notice", "m1", -1L, session).getMessage());
    }

    @Test
    void saveReadStateShouldPassSessionMemberAndMapServiceResult() {
        loginBusiness(41L, 11);
        ChatRoomVO notice = room("notice", "NOTICE");
        when(chatService.getChatRoom("notice")).thenReturn(notice);
        when(chatService.saveChatReadState(org.mockito.ArgumentMatchers.any(ChatReadStateVO.class)))
                .thenReturn(true, false);

        ChatResponseVO saved = controller.saveReadState("notice", "message-1", 1234L, session);
        assertTrue(saved.isSuccess());
        assertEquals(ChatResult.SUCCESS, saved.getCode());

        ArgumentCaptor<ChatReadStateVO> captor = ArgumentCaptor.forClass(ChatReadStateVO.class);
        verify(chatService).saveChatReadState(captor.capture());
        ChatReadStateVO state = captor.getValue();
        assertEquals("notice", state.getRoomId());
        assertEquals(41L, state.getMemberNo());
        assertEquals("message-1", state.getLastReadMessageId());
        assertEquals(1234L, state.getLastReadEpochMs());

        ChatResponseVO failed = controller.saveReadState("notice", "message-2", 2000L, session);
        assertFalse(failed.isSuccess());
        assertEquals("채팅 읽음 위치 저장에 실패했습니다.", failed.getMessage());
    }

    @Test
    void joinRoomShouldHandleAccessRoomTypeAndAdmin() {
        assertEquals(ChatResult.FAIL, controller.joinRoom("room", session).getCode());

        loginBusiness(50L, 12);
        when(chatService.getChatRoom("missing")).thenReturn(null);
        assertEquals(ChatResult.ROOM_NOT_FOUND, controller.joinRoom("missing", session).getCode());

        ChatRoomVO notice = room("notice", "NOTICE");
        when(chatService.getChatRoom("notice")).thenReturn(notice);
        ChatResponseVO noticeResponse = controller.joinRoom("notice", session);
        assertTrue(noticeResponse.isSuccess());
        assertEquals("공지방으로 이동합니다.", noticeResponse.getMessage());

        sessionValues.clear();
        loginAdmin();
        ChatRoomVO free = room("free", "PUBLIC");
        when(chatService.getChatRoom("free")).thenReturn(free);
        assertEquals("관리자는 공지방만 이용할 수 있습니다.", controller.joinRoom("free", session).getMessage());
    }

    @Test
    void joinRoomShouldMapEveryServiceResult() {
        loginBusiness(51L, 13);
        ChatRoomVO free = room("free", "PUBLIC");
        when(chatService.getChatRoom("free")).thenReturn(free);
        when(chatService.joinChatRoom("free", 13)).thenReturn(
                ChatResult.SUCCESS,
                ChatResult.ALREADY_JOINED,
                ChatResult.ROOM_FULL,
                ChatResult.ROOM_NOT_FOUND,
                99);

        assertEquals("채팅방에 참가했습니다.", controller.joinRoom("free", session).getMessage());
        assertEquals("이미 참가한 채팅방입니다.", controller.joinRoom("free", session).getMessage());
        assertEquals("채팅방 정원이 가득 찼습니다.", controller.joinRoom("free", session).getMessage());
        assertEquals("존재하지 않는 채팅방입니다.", controller.joinRoom("free", session).getMessage());
        assertEquals("채팅방 참가에 실패했습니다.", controller.joinRoom("free", session).getMessage());
    }

    @Test
    void leaveCheckShouldHandleDeniedCases() {
        Map<String, Object> anonymous = controller.checkLeaveRoom("room", session);
        assertEquals(Boolean.FALSE, anonymous.get("success"));

        loginAdmin();
        assertEquals(
                "관리자는 자유방 나가기 기능을 사용할 수 없습니다.",
                controller.checkLeaveRoom("room", session).get("message"));

        sessionValues.clear();
        loginBusiness(60L, 14);
        when(chatService.getChatRoom("missing")).thenReturn(null);
        assertEquals(
                "존재하지 않는 채팅방입니다.",
                controller.checkLeaveRoom("missing", session).get("message"));

        ChatRoomVO notice = room("notice", "NOTICE");
        when(chatService.getChatRoom("notice")).thenReturn(notice);
        assertEquals(
                "공지방에서는 나가기 기능을 사용할 수 없습니다.",
                controller.checkLeaveRoom("notice", session).get("message"));

        ChatRoomVO free = room("free", "PUBLIC");
        when(chatService.getChatRoom("free")).thenReturn(free);
        when(chatService.isChatRoomMember("free", 14)).thenReturn(false);
        assertEquals(
                "채팅방 참여 정보를 찾을 수 없습니다.",
                controller.checkLeaveRoom("free", session).get("message"));
    }

    @Test
    void leaveCheckShouldExplainWhetherRoomWillBeDeleted() {
        loginBusiness(61L, 15);
        ChatRoomVO free = room("free", "PUBLIC");
        when(chatService.getChatRoom("free")).thenReturn(free);
        when(chatService.isChatRoomMember("free", 15)).thenReturn(true);
        when(chatService.willRoomBeEmptyAfterLeave("free", 15)).thenReturn(true, false);

        Map<String, Object> delete = controller.checkLeaveRoom("free", session);
        assertEquals(Boolean.TRUE, delete.get("success"));
        assertEquals(Boolean.TRUE, delete.get("willDeleteRoom"));
        assertEquals("나가면 참여자가 없어 채팅방이 삭제됩니다.", delete.get("message"));

        Map<String, Object> keep = controller.checkLeaveRoom("free", session);
        assertEquals(Boolean.FALSE, keep.get("willDeleteRoom"));
        assertEquals("채팅방에서 나갈 수 있습니다.", keep.get("message"));
    }

    @Test
    void leaveRoomShouldHandleDeniedCases() {
        assertEquals(ChatResult.FAIL, controller.leaveRoom("room", session).getCode());

        loginAdmin();
        assertEquals(
                "관리자는 자유방 나가기 기능을 사용할 수 없습니다.",
                controller.leaveRoom("room", session).getMessage());

        sessionValues.clear();
        loginBusiness(70L, 16);
        when(chatService.getChatRoom("missing")).thenReturn(null);
        assertEquals(ChatResult.ROOM_NOT_FOUND, controller.leaveRoom("missing", session).getCode());

        ChatRoomVO notice = room("notice", "NOTICE");
        when(chatService.getChatRoom("notice")).thenReturn(notice);
        assertEquals(
                "공지방에서는 나가기 기능을 사용할 수 없습니다.",
                controller.leaveRoom("notice", session).getMessage());
    }

    @Test
    void leaveRoomShouldMapDeletedKeptAndFailedResults() {
        loginBusiness(71L, 17);
        ChatRoomVO free = room("free", "PUBLIC");
        when(chatService.getChatRoom("free")).thenReturn(free);
        when(chatService.willRoomBeEmptyAfterLeave("free", 17)).thenReturn(true, false, false);
        when(chatService.leaveChatRoom("free", 17)).thenReturn(true, true, false);

        assertEquals(
                "채팅방에서 나갔으며, 참여자가 없어 채팅방이 삭제되었습니다.",
                controller.leaveRoom("free", session).getMessage());
        assertEquals("채팅방에서 나갔습니다.", controller.leaveRoom("free", session).getMessage());
        assertEquals(
                "채팅방 참여 정보를 찾을 수 없습니다.",
                controller.leaveRoom("free", session).getMessage());
    }

    private void loginAdmin() {
        sessionValues.put("memberNo", 1L);
        sessionValues.put("role", "ADMIN");
    }

    private void loginBusiness(Long memberNo, int businessNo) {
        sessionValues.put("loginMemberNo", memberNo);
        sessionValues.put("businessNo", businessNo);
        sessionValues.put("role", "BUSINESS");
    }

    private void loginBusinessWithMemberObject(Long memberNo, int businessNo) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        member.setRole("BUSINESS");
        sessionValues.put("loginMember", member);
        sessionValues.put("businessNo", businessNo);
    }

    private ChatRoomVO room(String roomId, String roomType) {
        ChatRoomVO room = new ChatRoomVO();
        room.setRoomId(roomId);
        room.setRoomType(roomType);
        return room;
    }
}
