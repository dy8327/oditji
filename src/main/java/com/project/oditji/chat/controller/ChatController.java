package com.project.oditji.chat.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.oditji.chat.service.ChatService;
import com.project.oditji.chat.vo.ChatRoomVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private static final long ADMIN_MEMBER_NO = 1L;
    private static final int ADMIN_BUSINESS_NO = 1;
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROOM_TYPE_NOTICE = "NOTICE";
    private static final String ROOM_TYPE_PUBLIC = "PUBLIC";

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 채팅방 목록 화면입니다.
     *
     * 관리자는 공지방만 조회하고,
     * 승인된 사업자는 공지방과 자유방 전체를 조회합니다.
     */
    @GetMapping("/list")
    public String roomList(HttpSession session, Model model) {

        if (!hasChatAccess(session)) {
            return "redirect:/member/login";
        }

        boolean admin = isAdmin(session);
        List<ChatRoomVO> roomList = chatService.getChatRoomList();

        if (admin) {
            roomList = filterNoticeRooms(roomList);
        }

        addLoginChatAttributes(session, model);
        model.addAttribute("roomList", roomList);

        return "chat/roomList";
    }

    /**
     * 현재 로그인 사업자가 참여 중인 자유방 목록입니다.
     *
     * 관리자는 자유방을 이용하지 않으므로 공지방 목록으로 이동합니다.
     */
    @GetMapping("/my")
    public String myRoomList(HttpSession session, Model model) {

        if (!hasChatAccess(session)) {
            return "redirect:/member/login";
        }

        if (isAdmin(session)) {
            return "redirect:/chat/list";
        }

        Integer businessNo = getSessionBusinessNo(session);
        List<ChatRoomVO> roomList =
                chatService.getMyChatRoomList(businessNo);

        addLoginChatAttributes(session, model);
        model.addAttribute("roomList", roomList);

        return "chat/roomList";
    }

    /**
     * 채팅방 상세 화면입니다.
     *
     * 관리자는 공지방에만 입장할 수 있습니다.
     * 사업자는 공지방을 열람할 수 있고,
     * 자유방은 참가 기록이 있는 경우에만 입장할 수 있습니다.
     */
    @GetMapping("/room/{roomId}")
    public String room(
            @PathVariable("roomId") String roomId,
            HttpSession session,
            Model model) {

        if (!hasChatAccess(session)) {
            return "redirect:/member/login";
        }

        ChatRoomVO room = chatService.getChatRoom(roomId);

        if (room == null) {
            return "redirect:/chat/list";
        }

        boolean admin = isAdmin(session);
        boolean noticeRoom = ROOM_TYPE_NOTICE.equals(room.getRoomType());

        /* 관리자는 직접 URL로 접근해도 자유방에 들어갈 수 없습니다. */
        if (admin && !noticeRoom) {
            return "redirect:/chat/list";
        }

        boolean joined = false;

        if (!admin && !noticeRoom) {
            Integer businessNo = getSessionBusinessNo(session);
            joined = chatService.isChatRoomMember(roomId, businessNo);

            if (!joined) {
                return "redirect:/chat/list";
            }
        }

        addLoginChatAttributes(session, model);
        model.addAttribute("room", room);
        model.addAttribute("isNoticeRoom", noticeRoom);
        model.addAttribute("isJoined", joined);

        return "chat/room";
    }

    /**
     * 채팅방 생성 화면입니다.
     *
     * 관리자는 공지방만 생성하고,
     * 사업자는 자유방만 생성합니다.
     */
    @GetMapping("/create")
    public String createForm(HttpSession session, Model model) {

        if (!hasChatAccess(session)) {
            return "redirect:/member/login";
        }

        addLoginChatAttributes(session, model);

        return "chat/createRoom";
    }

    /**
     * 채팅방을 생성합니다.
     *
     * 클라이언트가 전달한 방 종류와 생성자 번호를 신뢰하지 않고,
     * 로그인 권한에 따라 관리자는 공지방, 사업자는 자유방으로 강제합니다.
     */
    @PostMapping("/create")
    public String create(
            ChatRoomVO chatRoom,
            HttpSession session) {

        if (!hasChatAccess(session)) {
            return "redirect:/member/login";
        }

        boolean admin = isAdmin(session);
        Integer chatBusinessNo = getChatBusinessNo(session);

        chatRoom.setCreatedBy(chatBusinessNo);

        if (admin) {
            chatRoom.setRoomType(ROOM_TYPE_NOTICE);
            chatRoom.setIsDefault("Y");
            chatRoom.setMaxMember(9999);
        } else {
            chatRoom.setRoomType(ROOM_TYPE_PUBLIC);
            chatRoom.setIsDefault("N");

            if (chatRoom.getMaxMember() <= 0) {
                chatRoom.setMaxMember(100);
            }
        }

        String roomId = chatService.createChatRoom(chatRoom);

        if (roomId == null) {
            return "redirect:/chat/list";
        }

        return "redirect:/chat/room/" + roomId;
    }

    /**
     * 채팅방을 비활성화합니다.
     *
     * 관리자는 공지방만, 사업자는 자신이 만든 자유방만 처리할 수 있습니다.
     * 기본 공지방 삭제는 서비스에서 한 번 더 차단합니다.
     */
    @PostMapping("/delete")
    public String delete(
            @RequestParam("roomId") String roomId,
            HttpSession session) {

        if (!hasChatAccess(session)) {
            return "redirect:/member/login";
        }

        ChatRoomVO room = chatService.getChatRoom(roomId);

        if (room == null) {
            return "redirect:/chat/list";
        }

        boolean admin = isAdmin(session);
        boolean noticeRoom = ROOM_TYPE_NOTICE.equals(room.getRoomType());
        Integer businessNo = getSessionBusinessNo(session);

        boolean canDelete = admin
                ? noticeRoom
                : !noticeRoom
                        && businessNo != null
                        && room.getCreatedBy() == businessNo;

        if (!canDelete) {
            return "redirect:/chat/list";
        }

        boolean result = chatService.deleteChatRoom(roomId);

        if (!result) {
            return "redirect:/chat/room/" + roomId;
        }

        return "redirect:/chat/list";
    }

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "chat controller ok";
    }

    /**
     * JSP에서 공통으로 사용하는 로그인 채팅 정보를 전달합니다.
     */
    private void addLoginChatAttributes(
            HttpSession session,
            Model model) {

        model.addAttribute("memberNo", getLongSessionValue(session, "memberNo"));
        model.addAttribute("businessNo", getChatBusinessNo(session));
        model.addAttribute("businessName", getChatDisplayName(session));
        model.addAttribute("role", session.getAttribute("role"));
        model.addAttribute("isAdmin", isAdmin(session));
    }

    /**
     * 관리자 여부를 MEMBER_NO=1, ROLE=ADMIN 기준으로 확인합니다.
     *
     * 일반 관리자 로그인 세션에는 businessNo가 없을 수 있으므로
     * 관리자 판별 조건에서 session.businessNo는 요구하지 않습니다.
     */
    private boolean isAdmin(HttpSession session) {

        Long memberNo = getLongSessionValue(session, "memberNo");
        Object role = session.getAttribute("role");

        return memberNo != null
                && memberNo == ADMIN_MEMBER_NO
                && ROLE_ADMIN.equals(String.valueOf(role));
    }

    /**
     * 관리자 또는 사업자 채팅 접근 가능 여부를 확인합니다.
     */
    private boolean hasChatAccess(HttpSession session) {
        return isAdmin(session) || getSessionBusinessNo(session) != null;
    }

    /**
     * 실제 로그인 사업자의 세션 사업자 번호를 반환합니다.
     */
    private Integer getSessionBusinessNo(HttpSession session) {

        Object value = session.getAttribute("businessNo");

        if (value instanceof Number number) {
            return number.intValue();
        }

        return null;
    }

    /**
     * 채팅에서 사용할 사업자 번호를 반환합니다.
     * 관리자 세션에 businessNo가 없어도 고정 관리자 사업자 번호 1을 사용합니다.
     */
    private Integer getChatBusinessNo(HttpSession session) {

        if (isAdmin(session)) {
            return ADMIN_BUSINESS_NO;
        }

        return getSessionBusinessNo(session);
    }

    /**
     * 세션의 숫자 값을 Long으로 변환합니다.
     */
    private Long getLongSessionValue(
            HttpSession session,
            String attributeName) {

        Object value = session.getAttribute(attributeName);

        if (value instanceof Number number) {
            return number.longValue();
        }

        return null;
    }

    /**
     * 채팅 화면에 보여줄 이름을 반환합니다.
     */
    private String getChatDisplayName(HttpSession session) {

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

    /**
     * 관리자 목록에는 공지방만 남깁니다.
     */
    private List<ChatRoomVO> filterNoticeRooms(List<ChatRoomVO> roomList) {

        if (roomList == null || roomList.isEmpty()) {
            return List.of();
        }

        return roomList.stream()
                .filter(room -> ROOM_TYPE_NOTICE.equals(room.getRoomType()))
                .toList();
    }
}
