package com.project.oditji.chat.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import com.project.oditji.chat.common.ChatResult;
import com.project.oditji.chat.service.ChatService;
import com.project.oditji.chat.service.FirebaseChatService;
import com.project.oditji.chat.vo.ChatResponseVO;
import com.project.oditji.chat.vo.ChatRoomVO;
import com.project.oditji.member.vo.MemberVO;

/**
 * ChatApiController의 읽음검증 OR 조건과 접근권한 ternary 분기를 보완합니다.
 */
class ChatApiControllerAdditionalConditionCoverageTest {

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = mock(ChatService.class);
    }

    @Test
    void saveReadStateShouldCoverAccessFalseAfterMemberNumberExists() {
        ChatApiController controller =
                new ChatApiController(chatService);

        MockHttpSession session =
                new MockHttpSession();
        session.setAttribute("memberNo", 10L);

        ChatResponseVO response =
                controller.saveReadState(
                        "room",
                        "message",
                        0L,
                        session);

        assertEquals(ChatResult.FAIL, response.getCode());
    }

    @Test
    void saveReadStateShouldCoverBlankLongAndNegativeReadPositions() {
        ChatApiController controller =
                new ChatApiController(chatService);

        MockHttpSession session = businessSession();

        ChatRoomVO room = room("FREE");
        when(chatService.getChatRoom("room"))
                .thenReturn(room);
        when(chatService.isChatRoomMember(
                "room",
                20))
                .thenReturn(true);

        assertEquals(
                ChatResult.FAIL,
                controller.saveReadState(
                        "room",
                        "   ",
                        0L,
                        session)
                        .getCode());

        assertEquals(
                ChatResult.FAIL,
                controller.saveReadState(
                        "room",
                        "x".repeat(101),
                        0L,
                        session)
                        .getCode());

        assertEquals(
                ChatResult.FAIL,
                controller.saveReadState(
                        "room",
                        "message",
                        -1L,
                        session)
                        .getCode());
    }

    @Test
    void accessibleRoomShouldCoverAdminFreeNoticeAndBusinessMembershipBranches() {
        ChatApiController controller =
                new ChatApiController(chatService);

        ChatRoomVO free = room("FREE");
        ChatRoomVO notice = room("NOTICE");

        when(chatService.getChatRoom("free"))
                .thenReturn(free);
        when(chatService.getChatRoom("notice"))
                .thenReturn(notice);

        MockHttpSession admin = adminSession();

        assertNull(
                controller.getRoom(
                        "free",
                        admin));
        assertSame(
                notice,
                controller.getRoom(
                        "notice",
                        admin));

        MockHttpSession business = businessSession();

        when(chatService.isChatRoomMember(
                "free",
                20))
                .thenReturn(false, true);

        assertNull(
                controller.getRoom(
                        "free",
                        business));
        assertSame(
                free,
                controller.getRoom(
                        "free",
                        business));
    }

    @Test
    void firebaseEnabledPredicateShouldCoverNullAndDisabledService() {
        ChatApiController nullFirebase =
                new ChatApiController(chatService);

        MockHttpSession session = businessSession();

        assertEquals(
                Boolean.FALSE,
                nullFirebase.getFirebaseToken(
                        session,
                        new org.springframework.mock.web.MockHttpServletResponse())
                        .isEnabled());

        FirebaseChatService firebase =
                mock(FirebaseChatService.class);
        when(firebase.isEnabled())
                .thenReturn(false);

        ChatApiController disabled =
                new ChatApiController(
                        chatService,
                        firebase);

        assertEquals(
                Boolean.FALSE,
                disabled.getFirebaseToken(
                        session,
                        new org.springframework.mock.web.MockHttpServletResponse())
                        .isEnabled());
    }

    private MockHttpSession businessSession() {
        MockHttpSession session =
                new MockHttpSession();
        session.setAttribute("memberNo", 10L);
        session.setAttribute("businessNo", 20);
        session.setAttribute("role", "BUSINESS");
        return session;
    }

    private MockHttpSession adminSession() {
        MockHttpSession session =
                new MockHttpSession();

        MemberVO admin = new MemberVO();
        admin.setMemberNo(1L);
        admin.setRole("ADMIN");

        session.setAttribute("loginMember", admin);
        return session;
    }

    private ChatRoomVO room(String type) {
        ChatRoomVO room = new ChatRoomVO();
        room.setRoomType(type);
        return room;
    }
}
