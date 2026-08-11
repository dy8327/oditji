package com.project.oditji.chat.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.chat.vo.ChatRoomVO;
import com.project.oditji.member.vo.MemberVO;

/** 채팅 세션 유틸의 복합 조건과 세션 키 fallback 잔여 분기를 보완합니다. */
class ChatSessionSupportTailConditionCoverageTest {

    @Test
    void adminCheckShouldCoverEveryShortCircuitPosition() {
        MockHttpSession session = new MockHttpSession();

        assertFalse(ChatSessionSupport.isAdmin(session));

        session.setAttribute("memberNo", 2L);
        session.setAttribute("role", "ADMIN");
        assertFalse(ChatSessionSupport.isAdmin(session));

        session.setAttribute("memberNo", 1L);
        session.setAttribute("role", "USER");
        assertFalse(ChatSessionSupport.isAdmin(session));

        session.setAttribute("role", "ADMIN");
        assertTrue(ChatSessionSupport.isAdmin(session));
        assertEquals(1, ChatSessionSupport.getChatBusinessNo(session));
        assertEquals("ODITJI 관리자", ChatSessionSupport.getChatDisplayName(session));
    }

    @Test
    void businessAccessAndSessionFallbacksShouldCoverBothSides() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("memberNo", "not-number");
        session.setAttribute("loginMemberNo", 22);
        session.setAttribute("role", "   ");
        session.setAttribute("businessNo", 33L);

        MemberVO loginMember = new MemberVO();
        loginMember.setMemberNo(44L);
        loginMember.setRole("BUSINESS");
        session.setAttribute("loginMember", loginMember);

        assertEquals(22L, ChatSessionSupport.getSessionMemberNo(session));
        assertEquals("BUSINESS", ChatSessionSupport.getSessionRole(session));
        assertEquals(33, ChatSessionSupport.getSessionBusinessNo(session));
        assertTrue(ChatSessionSupport.hasChatAccess(session));

        session.removeAttribute("loginMemberNo");
        assertEquals(44L, ChatSessionSupport.getSessionMemberNo(session));

        session.setAttribute("businessNo", "invalid");
        assertNull(ChatSessionSupport.getSessionBusinessNo(session));
        assertFalse(ChatSessionSupport.hasChatAccess(session));
    }

    @Test
    void displayNameAndModelAttributesShouldCoverBlankAndFallbackNames() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("memberNo", 7L);
        session.setAttribute("role", "BUSINESS");
        session.setAttribute("businessNo", 8);
        session.setAttribute("businessName", "   ");
        session.setAttribute("loginDisplayName", " 표시 이름 ");

        assertEquals(" 표시 이름 ", ChatSessionSupport.getChatDisplayName(session));

        session.setAttribute("loginDisplayName", " ");
        assertEquals("사업자", ChatSessionSupport.getChatDisplayName(session));

        session.setAttribute("businessName", "상점");
        assertEquals("상점", ChatSessionSupport.getChatDisplayName(session));

        ExtendedModelMap model = new ExtendedModelMap();
        ChatSessionSupport.addLoginChatAttributes(session, model);

        assertEquals(7L, model.get("memberNo"));
        assertEquals(8, model.get("businessNo"));
        assertEquals("상점", model.get("businessName"));
        assertEquals("BUSINESS", model.get("role"));
        assertEquals(Boolean.FALSE, model.get("isAdmin"));
    }

    @Test
    void noticeFilterShouldHandleNullEmptyAndMixedRooms() {
        assertTrue(ChatSessionSupport.filterNoticeRooms(null).isEmpty());
        assertTrue(ChatSessionSupport.filterNoticeRooms(List.of()).isEmpty());

        ChatRoomVO notice = new ChatRoomVO();
        notice.setRoomType("NOTICE");
        ChatRoomVO publicRoom = new ChatRoomVO();
        publicRoom.setRoomType("PUBLIC");

        assertEquals(
                List.of(notice),
                ChatSessionSupport.filterNoticeRooms(List.of(publicRoom, notice)));
    }
}
