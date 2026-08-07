package com.project.oditji.chat.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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

import com.project.oditji.chat.service.ChatService;
import com.project.oditji.chat.service.FirebaseChatService;
import com.project.oditji.chat.vo.ChatRoomVO;
import com.project.oditji.chat.vo.FirebaseChatTokenVO;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** Firebase 채팅 토큰 API의 비활성/관리자/사업자/실패 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class ChatApiControllerFirebaseCoverageTest {

    private static final String ROLE_BUSINESS = "BUSINESS";
    private static final String BUSINESS_NAME = "사업자";

    @Mock
    private ChatService chatService;

    @Mock
    private FirebaseChatService firebaseChatService;

    @Mock
    private HttpSession session;

    @Mock
    private HttpServletResponse response;

    private final Map<String, Object> sessionValues = new HashMap<String, Object>();
    private ChatApiController controller;

    @BeforeEach
    void setUp() {
        controller = new ChatApiController(chatService, firebaseChatService);
        when(session.getAttribute(any(String.class)))
                .thenAnswer(invocation -> sessionValues.get(invocation.getArgument(0)));
    }

    @Test
    void anonymousShouldReturnLoginFailureAndReportFirebaseState() {
        when(firebaseChatService.isEnabled()).thenReturn(true);

        FirebaseChatTokenVO result = controller.getFirebaseToken(session, response);

        assertFalse(result.isSuccess());
        assertTrue(result.isEnabled());
        assertNull(result.getToken());
        verify(response).setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        verify(response).setHeader("Pragma", "no-cache");
        verify(firebaseChatService, never()).createCustomToken(anyLong(), anyInt(), anyString(), anyString());
    }

    @Test
    void loggedInUserShouldReturnDisabledResultWhenFirebaseIsDisabled() {
        loginBusiness(20L, 8);
        when(firebaseChatService.isEnabled()).thenReturn(false);

        FirebaseChatTokenVO result = controller.getFirebaseToken(session, response);

        assertFalse(result.isSuccess());
        assertFalse(result.isEnabled());
        assertEquals("Firebase 채팅 인증이 비활성화되어 있습니다.", result.getMessage());
    }

    @Test
    void businessShouldSynchronizeJoinedRoomsAndCreateToken() {
        loginBusiness(21L, 9);
        ChatRoomVO room = room("free", "PUBLIC");
        when(firebaseChatService.isEnabled()).thenReturn(true);
        when(chatService.getChatRoomList()).thenReturn(List.of(room));
        when(chatService.getMyChatRoomList(9)).thenReturn(List.of(room));
        when(firebaseChatService.createCustomToken(21L, 9, ROLE_BUSINESS, BUSINESS_NAME))
                .thenReturn("token-business");
        when(firebaseChatService.createUid(21L)).thenReturn("member-21");

        FirebaseChatTokenVO result = controller.getFirebaseToken(session, response);

        assertTrue(result.isSuccess());
        assertEquals("token-business", result.getToken());
        assertEquals("member-21", result.getUid());
        verify(firebaseChatService).synchronizeCurrentUser(
                List.of(room), List.of(room), 21L, 9, ROLE_BUSINESS, BUSINESS_NAME);
    }

    @Test
    void adminShouldSynchronizeWithEmptyJoinedRoomList() {
        loginAdmin();
        ChatRoomVO notice = room("notice", "NOTICE");
        when(firebaseChatService.isEnabled()).thenReturn(true);
        when(chatService.getChatRoomList()).thenReturn(List.of(notice));
        when(firebaseChatService.createCustomToken(1L, 1, "ADMIN", "ODITJI 관리자"))
                .thenReturn("token-admin");
        when(firebaseChatService.createUid(1L)).thenReturn("member-1");

        FirebaseChatTokenVO result = controller.getFirebaseToken(session, response);

        assertTrue(result.isSuccess());
        verify(chatService, never()).getMyChatRoomList(anyInt());
        verify(firebaseChatService).synchronizeCurrentUser(
                List.of(notice), List.of(), 1L, 1, "ADMIN", "ODITJI 관리자");
    }

    @Test
    void firebasePreparationExceptionsShouldBecomeSafeFailureResponse() {
        loginBusiness(22L, 10);
        when(firebaseChatService.isEnabled()).thenReturn(true);
        when(chatService.getChatRoomList()).thenReturn(List.of());
        when(chatService.getMyChatRoomList(10)).thenReturn(List.of());
        doThrow(new IllegalStateException("firebase"))
                .when(firebaseChatService)
                .synchronizeCurrentUser(anyList(), anyList(), eq(22L), eq(10), eq(ROLE_BUSINESS), eq(BUSINESS_NAME));

        FirebaseChatTokenVO result = controller.getFirebaseToken(session, response);

        assertFalse(result.isSuccess());
        assertTrue(result.isEnabled());
        assertEquals("Firebase 채팅 인증 준비에 실패했습니다.", result.getMessage());
    }

    private void loginBusiness(long memberNo, int businessNo) {
        sessionValues.put("loginMemberNo", memberNo);
        sessionValues.put("businessNo", businessNo);
        sessionValues.put("role", ROLE_BUSINESS);
        sessionValues.put("businessName", BUSINESS_NAME);
    }

    private void loginAdmin() {
        MemberVO member = new MemberVO();
        member.setMemberNo(1L);
        member.setMemberName("관리자");
        member.setRole("ADMIN");
        sessionValues.put("loginMember", member);
        sessionValues.put("loginMemberNo", 1L);
        sessionValues.put("role", "ADMIN");
    }

    private ChatRoomVO room(String roomId, String roomType) {
        ChatRoomVO room = new ChatRoomVO();
        room.setRoomId(roomId);
        room.setRoomType(roomType);
        return room;
    }
}
