package com.project.oditji.chat.support;

import java.util.List;

import org.springframework.ui.Model;

import com.project.oditji.chat.vo.ChatRoomVO;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpSession;

/**
 * 채팅 MVC/API 컨트롤러가 공통으로 사용하는 로그인 세션 해석 로직입니다.
 *
 * 동일한 관리자 판별과 세션 키 호환 로직을 한 곳에서 관리하여
 * 컨트롤러 간 중복과 권한 판별 차이를 방지합니다.
 */
public final class ChatSessionSupport {

    public static final int ADMIN_BUSINESS_NO = 1;
    public static final String ROOM_TYPE_NOTICE = "NOTICE";

    private static final long ADMIN_MEMBER_NO = 1L;
    private static final String ROLE_ADMIN = "ADMIN";

    private ChatSessionSupport() {
        // 인스턴스 생성 방지
    }

    public static void addLoginChatAttributes(
            HttpSession session,
            Model model) {

        model.addAttribute("memberNo", getSessionMemberNo(session));
        model.addAttribute("businessNo", getChatBusinessNo(session));
        model.addAttribute("businessName", getChatDisplayName(session));
        model.addAttribute("role", getSessionRole(session));
        model.addAttribute("isAdmin", isAdmin(session));
    }

    public static boolean isAdmin(HttpSession session) {

        Long memberNo = getSessionMemberNo(session);
        String role = getSessionRole(session);

        return memberNo != null
                && memberNo.longValue() == ADMIN_MEMBER_NO
                && ROLE_ADMIN.equals(role);
    }

    public static boolean hasChatAccess(HttpSession session) {
        return isAdmin(session) || getSessionBusinessNo(session) != null;
    }

    public static Integer getSessionBusinessNo(HttpSession session) {

        Object value = session.getAttribute("businessNo");

        if (value instanceof Number number) {
            return number.intValue();
        }

        return null;
    }

    public static Integer getChatBusinessNo(HttpSession session) {

        if (isAdmin(session)) {
            return ADMIN_BUSINESS_NO;
        }

        return getSessionBusinessNo(session);
    }

    public static Long getSessionMemberNo(HttpSession session) {

        Long memberNo = getLongSessionValue(session, "memberNo");

        if (memberNo != null) {
            return memberNo;
        }

        memberNo = getLongSessionValue(session, "loginMemberNo");

        if (memberNo != null) {
            return memberNo;
        }

        Object loginMember = session.getAttribute("loginMember");

        if (loginMember instanceof MemberVO member) {
            return member.getMemberNo();
        }

        return null;
    }

    public static String getSessionRole(HttpSession session) {

        Object role = session.getAttribute("role");

        if (role != null && !String.valueOf(role).isBlank()) {
            return String.valueOf(role);
        }

        Object loginMember = session.getAttribute("loginMember");

        if (loginMember instanceof MemberVO member) {
            return member.getRole();
        }

        return null;
    }

    public static String getChatDisplayName(HttpSession session) {

        if (isAdmin(session)) {
            return "ODITJI 관리자";
        }

        Object businessName = session.getAttribute("businessName");

        if (businessName != null
                && !String.valueOf(businessName).isBlank()) {

            return String.valueOf(businessName);
        }

        Object displayName = session.getAttribute("loginDisplayName");

        if (displayName != null
                && !String.valueOf(displayName).isBlank()) {

            return String.valueOf(displayName);
        }

        return "사업자";
    }

    public static List<ChatRoomVO> filterNoticeRooms(List<ChatRoomVO> roomList) {

        if (roomList == null || roomList.isEmpty()) {
            return List.of();
        }

        return roomList.stream()
                .filter(room -> ROOM_TYPE_NOTICE.equals(room.getRoomType()))
                .toList();
    }

    private static Long getLongSessionValue(
            HttpSession session,
            String attributeName) {

        Object value = session.getAttribute(attributeName);

        if (value instanceof Number number) {
            return number.longValue();
        }

        return null;
    }
}
