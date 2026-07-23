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
     * 전체 채팅방 목록 화면입니다.
     * 로그인한 관리자 또는 승인된 사업자만 접근할 수 있습니다.
     */
    @GetMapping("/list")
    public String roomList(HttpSession session, Model model) {

        Integer businessNo = getLoginBusinessNo(session);

        if (businessNo == null) {
            return "redirect:/member/login";
        }

        List<ChatRoomVO> roomList = chatService.getChatRoomList();

        addLoginChatAttributes(session, model);
        model.addAttribute("roomList", roomList);

        return "chat/roomList";
    }

    /**
     * 현재 로그인 사업자가 참여 중인 자유방 목록입니다.
     * 공지방은 참가 개념이 없으므로 전체 목록 화면에서 확인합니다.
     */
    @GetMapping("/my")
    public String myRoomList(HttpSession session, Model model) {

        Integer businessNo = getLoginBusinessNo(session);

        if (businessNo == null) {
            return "redirect:/member/login";
        }

        List<ChatRoomVO> roomList =
                chatService.getMyChatRoomList(businessNo);

        addLoginChatAttributes(session, model);
        model.addAttribute("roomList", roomList);

        return "chat/roomList";
    }

    /**
     * 채팅방 상세 화면입니다.
     * 공지방은 로그인 사업자라면 누구나 열람할 수 있고,
     * 자유방은 CHAT_ROOM_MEMBER에 참가 기록이 있는 사용자만 입장할 수 있습니다.
     */
    @GetMapping("/room/{roomId}")
    public String room(
            @PathVariable("roomId") String roomId,
            HttpSession session,
            Model model) {

        Integer businessNo = getLoginBusinessNo(session);

        if (businessNo == null) {
            return "redirect:/member/login";
        }

        ChatRoomVO room = chatService.getChatRoom(roomId);

        if (room == null) {
            return "redirect:/chat/list";
        }

        boolean noticeRoom = ROOM_TYPE_NOTICE.equals(room.getRoomType());
        boolean joined = chatService.isChatRoomMember(roomId, businessNo);

        if (!noticeRoom && !joined) {
            return "redirect:/chat/list";
        }

        addLoginChatAttributes(session, model);
        model.addAttribute("room", room);
        model.addAttribute("isNoticeRoom", noticeRoom);
        model.addAttribute("isJoined", joined);

        return "chat/room";
    }

    /**
     * 채팅방 생성 화면입니다.
     * 관리자는 공지방과 자유방을 만들 수 있고,
     * 일반 사업자는 자유방만 만들 수 있습니다.
     */
    @GetMapping("/create")
    public String createForm(HttpSession session, Model model) {

        Integer businessNo = getLoginBusinessNo(session);

        if (businessNo == null) {
            return "redirect:/member/login";
        }

        addLoginChatAttributes(session, model);

        return "chat/createRoom";
    }

    /**
     * 채팅방을 생성합니다.
     * 브라우저가 전달한 createdBy 값은 사용하지 않고 로그인 세션의 사업자 번호를 사용합니다.
     */
    @PostMapping("/create")
    public String create(
            ChatRoomVO chatRoom,
            HttpSession session) {

        Integer businessNo = getLoginBusinessNo(session);

        if (businessNo == null) {
            return "redirect:/member/login";
        }

        boolean admin = isAdmin(session);
        String requestedRoomType = normalizeRoomType(chatRoom.getRoomType());

        if (ROOM_TYPE_NOTICE.equals(requestedRoomType) && !admin) {
            return "redirect:/chat/list";
        }

        chatRoom.setCreatedBy(businessNo);
        chatRoom.setRoomType(requestedRoomType);

        if (ROOM_TYPE_NOTICE.equals(requestedRoomType)) {
            chatRoom.setIsDefault("Y");
            chatRoom.setMaxMember(9999);
        } else {
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
     * 기본 공지방은 서비스에서 삭제가 차단됩니다.
     */
    @PostMapping("/delete")
    public String delete(
            @RequestParam("roomId") String roomId,
            HttpSession session) {

        Integer businessNo = getLoginBusinessNo(session);

        if (businessNo == null) {
            return "redirect:/member/login";
        }

        ChatRoomVO room = chatService.getChatRoom(roomId);

        if (room == null) {
            return "redirect:/chat/list";
        }

        boolean canDelete =
                isAdmin(session)
                || room.getCreatedBy() == businessNo;

        if (!canDelete) {
            return "redirect:/chat/room/" + roomId;
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
        model.addAttribute("businessNo", getLoginBusinessNo(session));
        model.addAttribute("businessName", getBusinessDisplayName(session));
        model.addAttribute("role", session.getAttribute("role"));
        model.addAttribute("isAdmin", isAdmin(session));
    }

    /**
     * 관리자 여부를 MEMBER_NO=1, BUSINESS_NO=1, ROLE=ADMIN 기준으로 확인합니다.
     */
    private boolean isAdmin(HttpSession session) {

        Long memberNo = getLongSessionValue(session, "memberNo");
        Integer businessNo = getLoginBusinessNo(session);
        Object role = session.getAttribute("role");

        return memberNo != null
                && memberNo == ADMIN_MEMBER_NO
                && businessNo != null
                && businessNo == ADMIN_BUSINESS_NO
                && ROLE_ADMIN.equals(String.valueOf(role));
    }

    /**
     * 로그인 세션의 사업자 번호를 안전하게 변환합니다.
     */
    private Integer getLoginBusinessNo(HttpSession session) {

        Object value = session.getAttribute("businessNo");

        if (value instanceof Number number) {
            return number.intValue();
        }

        return null;
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
     * 채팅 화면에 보여줄 이름을 사업자명 우선으로 가져옵니다.
     */
    private String getBusinessDisplayName(HttpSession session) {

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
     * 허용되지 않은 방 유형은 자유방으로 강제합니다.
     */
    private String normalizeRoomType(String roomType) {

        if (ROOM_TYPE_NOTICE.equals(roomType)) {
            return ROOM_TYPE_NOTICE;
        }

        return ROOM_TYPE_PUBLIC;
    }
}
