package com.project.oditji.chat.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.chat.service.ChatService;
import com.project.oditji.chat.vo.ChatRoomVO;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpSession;

/** 채팅 화면 컨트롤러의 권한, 생성, 입장, 삭제 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class ChatControllerCoverageTest {

    @Mock
    private ChatService chatService;

    @Mock
    private HttpSession session;

    private final Map<String, Object> sessionValues = new HashMap<String, Object>();

    private ChatController controller;

    @BeforeEach
    void setUp() {
        controller = new ChatController(chatService);
        lenient().when(session.getAttribute(anyString()))
                .thenAnswer(invocation -> sessionValues.get(invocation.getArgument(0)));
    }

    @Test
    void anonymousMemberShouldBeRedirectedFromProtectedPages() {
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("redirect:/member/login", controller.roomList(session, model));
        assertEquals("redirect:/member/login", controller.myRoomList(session, model));
        assertEquals("redirect:/member/login", controller.room("room-1", session, model));
        assertEquals("redirect:/member/login", controller.createForm(session, model));
        assertEquals("redirect:/member/login", controller.create(new ChatRoomVO(), session));
        assertEquals("redirect:/member/login", controller.delete("room-1", session));
    }

    @Test
    void adminRoomListShouldContainOnlyNoticeRoomsAndAdminAttributes() {
        loginAdmin();
        ChatRoomVO notice = room("notice", "NOTICE", 1);
        ChatRoomVO publicRoom = room("public", "PUBLIC", 2);
        when(chatService.getChatRoomList()).thenReturn(List.of(notice, publicRoom));
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("chat/roomList", controller.roomList(session, model));

        @SuppressWarnings("unchecked")
        List<ChatRoomVO> result = (List<ChatRoomVO>) model.get("roomList");
        assertEquals(1, result.size());
        assertSame(notice, result.get(0));
        assertEquals(1L, model.get("memberNo"));
        assertEquals(1, model.get("businessNo"));
        assertEquals("ODITJI 관리자", model.get("businessName"));
        assertEquals("ADMIN", model.get("role"));
        assertEquals(Boolean.TRUE, model.get("isAdmin"));
    }

    @Test
    void businessRoomListAndMyRoomListShouldExposeBusinessSessionData() {
        loginBusiness(12L, 7, "사업자A");
        List<ChatRoomVO> allRooms = List.of(room("notice", "NOTICE", 1), room("free", "PUBLIC", 7));
        List<ChatRoomVO> myRooms = List.of(room("free", "PUBLIC", 7));
        when(chatService.getChatRoomList()).thenReturn(allRooms);
        when(chatService.getMyChatRoomList(7)).thenReturn(myRooms);

        ExtendedModelMap listModel = new ExtendedModelMap();
        assertEquals("chat/roomList", controller.roomList(session, listModel));
        assertSame(allRooms, listModel.get("roomList"));
        assertEquals("사업자A", listModel.get("businessName"));
        assertEquals(Boolean.FALSE, listModel.get("isAdmin"));

        ExtendedModelMap myModel = new ExtendedModelMap();
        assertEquals("chat/roomList", controller.myRoomList(session, myModel));
        assertSame(myRooms, myModel.get("roomList"));
        verify(chatService).getMyChatRoomList(7);
    }

    @Test
    void adminMyRoomListShouldRedirectToNoticeList() {
        loginAdmin();

        assertEquals("redirect:/chat/list", controller.myRoomList(session, new ExtendedModelMap()));
        verify(chatService, never()).getMyChatRoomList(1);
    }

    @Test
    void roomShouldHandleMissingAndUnauthorizedRooms() {
        loginBusiness(20L, 8, "사업자");
        when(chatService.getChatRoom("missing")).thenReturn(null);
        assertEquals(
                "redirect:/chat/list",
                controller.room("missing", session, new ExtendedModelMap()));

        ChatRoomVO freeRoom = room("free", "PUBLIC", 9);
        when(chatService.getChatRoom("free")).thenReturn(freeRoom);
        when(chatService.isChatRoomMember("free", 8)).thenReturn(false);
        assertEquals(
                "redirect:/chat/list",
                controller.room("free", session, new ExtendedModelMap()));

        sessionValues.clear();
        loginAdmin();
        when(chatService.getChatRoom("admin-free")).thenReturn(room("admin-free", "PUBLIC", 2));
        assertEquals(
                "redirect:/chat/list",
                controller.room("admin-free", session, new ExtendedModelMap()));
    }

    @Test
    void roomShouldAllowNoticeAndJoinedPublicRoom() {
        loginBusiness(21L, 9, "사업자");
        ChatRoomVO notice = room("notice", "NOTICE", 1);
        when(chatService.getChatRoom("notice")).thenReturn(notice);
        ExtendedModelMap noticeModel = new ExtendedModelMap();

        assertEquals("chat/room", controller.room("notice", session, noticeModel));
        assertSame(notice, noticeModel.get("room"));
        assertEquals(Boolean.TRUE, noticeModel.get("isNoticeRoom"));
        assertEquals(Boolean.FALSE, noticeModel.get("isJoined"));

        ChatRoomVO freeRoom = room("free", "PUBLIC", 9);
        when(chatService.getChatRoom("free")).thenReturn(freeRoom);
        when(chatService.isChatRoomMember("free", 9)).thenReturn(true);
        ExtendedModelMap freeModel = new ExtendedModelMap();

        assertEquals("chat/room", controller.room("free", session, freeModel));
        assertSame(freeRoom, freeModel.get("room"));
        assertEquals(Boolean.FALSE, freeModel.get("isNoticeRoom"));
        assertEquals(Boolean.TRUE, freeModel.get("isJoined"));
    }

    @Test
    void createFormShouldUseLoginMemberAndDisplayNameFallbacks() {
        MemberVO member = new MemberVO();
        member.setMemberNo(30L);
        member.setRole("BUSINESS");
        sessionValues.put("loginMember", member);
        sessionValues.put("businessNo", 10);
        sessionValues.put("loginDisplayName", "소셜사업자");
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("chat/createRoom", controller.createForm(session, model));
        assertEquals(30L, model.get("memberNo"));
        assertEquals("BUSINESS", model.get("role"));
        assertEquals("소셜사업자", model.get("businessName"));

        sessionValues.remove("loginDisplayName");
        ExtendedModelMap defaultModel = new ExtendedModelMap();
        controller.createForm(session, defaultModel);
        assertEquals("사업자", defaultModel.get("businessName"));
    }

    @Test
    void createShouldForceRoleSpecificRoomValues() {
        loginAdmin();
        when(chatService.createChatRoom(org.mockito.ArgumentMatchers.any(ChatRoomVO.class)))
                .thenReturn("notice-1");
        ChatRoomVO adminRequest = room("ignored", "PUBLIC", 99);

        assertEquals("redirect:/chat/room/notice-1", controller.create(adminRequest, session));
        assertEquals(1, adminRequest.getCreatedBy());
        assertEquals("NOTICE", adminRequest.getRoomType());
        assertEquals("Y", adminRequest.getIsDefault());
        assertEquals(9999, adminRequest.getMaxMember());

        sessionValues.clear();
        loginBusiness(40L, 11, "사업자");
        ChatRoomVO businessRequest = room("ignored", "NOTICE", 99);
        businessRequest.setMaxMember(0);
        when(chatService.createChatRoom(businessRequest)).thenReturn("free-1");

        assertEquals("redirect:/chat/room/free-1", controller.create(businessRequest, session));
        assertEquals(11, businessRequest.getCreatedBy());
        assertEquals("PUBLIC", businessRequest.getRoomType());
        assertEquals("N", businessRequest.getIsDefault());
        assertEquals(100, businessRequest.getMaxMember());
    }

    @Test
    void createShouldPreservePositiveMaximumAndRedirectWhenCreationFails() {
        loginBusiness(41L, 12, "사업자");
        ChatRoomVO request = new ChatRoomVO();
        request.setMaxMember(25);
        when(chatService.createChatRoom(request)).thenReturn(null);

        assertEquals("redirect:/chat/list", controller.create(request, session));
        assertEquals(25, request.getMaxMember());
    }

    @Test
    void deleteShouldEnforceOwnershipAndHandleServiceResult() {
        loginBusiness(50L, 13, "사업자");
        when(chatService.getChatRoom("missing")).thenReturn(null);
        assertEquals("redirect:/chat/list", controller.delete("missing", session));

        ChatRoomVO otherRoom = room("other", "PUBLIC", 99);
        when(chatService.getChatRoom("other")).thenReturn(otherRoom);
        assertEquals("redirect:/chat/list", controller.delete("other", session));
        verify(chatService, never()).deleteChatRoom("other");

        ChatRoomVO ownRoom = room("own", "PUBLIC", 13);
        when(chatService.getChatRoom("own")).thenReturn(ownRoom);
        when(chatService.deleteChatRoom("own")).thenReturn(false);
        assertEquals("redirect:/chat/room/own", controller.delete("own", session));

        ChatRoomVO ownRoom2 = room("own2", "PUBLIC", 13);
        when(chatService.getChatRoom("own2")).thenReturn(ownRoom2);
        when(chatService.deleteChatRoom("own2")).thenReturn(true);
        assertEquals("redirect:/chat/list", controller.delete("own2", session));
    }

    @Test
    void adminShouldDeleteOnlyNoticeRoom() {
        loginAdmin();
        ChatRoomVO publicRoom = room("public", "PUBLIC", 1);
        when(chatService.getChatRoom("public")).thenReturn(publicRoom);
        assertEquals("redirect:/chat/list", controller.delete("public", session));

        ChatRoomVO notice = room("notice", "NOTICE", 1);
        when(chatService.getChatRoom("notice")).thenReturn(notice);
        when(chatService.deleteChatRoom("notice")).thenReturn(true);
        assertEquals("redirect:/chat/list", controller.delete("notice", session));
        verify(chatService).deleteChatRoom("notice");
    }

    @Test
    void testEndpointShouldReturnHealthText() {
        assertEquals("chat controller ok", controller.test());
    }

    private void loginAdmin() {
        sessionValues.put("memberNo", 1L);
        sessionValues.put("role", "ADMIN");
    }

    private void loginBusiness(Long memberNo, int businessNo, String businessName) {
        sessionValues.put("loginMemberNo", memberNo);
        sessionValues.put("businessNo", businessNo);
        sessionValues.put("role", "BUSINESS");
        sessionValues.put("businessName", businessName);
    }

    private ChatRoomVO room(String roomId, String roomType, int createdBy) {
        ChatRoomVO room = new ChatRoomVO();
        room.setRoomId(roomId);
        room.setRoomType(roomType);
        room.setCreatedBy(createdBy);
        return room;
    }
}
