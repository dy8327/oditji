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
import com.project.oditji.chat.support.ChatSessionSupport;
import com.project.oditji.chat.vo.ChatRoomVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private static final String ROOM_TYPE_PUBLIC = "PUBLIC";
    private static final String REDIRECT_MEMBER_LOGIN = "redirect:/member/login";
    private static final String REDIRECT_CHAT_LIST = "redirect:/chat/list";

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

        if (!ChatSessionSupport.hasChatAccess(session)) {
            return REDIRECT_MEMBER_LOGIN;
        }

        boolean admin = ChatSessionSupport.isAdmin(session);
        List<ChatRoomVO> roomList = chatService.getChatRoomList();

        if (admin) {
            roomList = ChatSessionSupport.filterNoticeRooms(roomList);
        }

        ChatSessionSupport.addLoginChatAttributes(session, model);
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

        if (!ChatSessionSupport.hasChatAccess(session)) {
            return REDIRECT_MEMBER_LOGIN;
        }

        if (ChatSessionSupport.isAdmin(session)) {
            return REDIRECT_CHAT_LIST;
        }

        Integer businessNo = ChatSessionSupport.getSessionBusinessNo(session);
        List<ChatRoomVO> roomList =
                chatService.getMyChatRoomList(businessNo);

        ChatSessionSupport.addLoginChatAttributes(session, model);
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

        if (!ChatSessionSupport.hasChatAccess(session)) {
            return REDIRECT_MEMBER_LOGIN;
        }

        ChatRoomVO room = chatService.getChatRoom(roomId);

        if (room == null) {
            return REDIRECT_CHAT_LIST;
        }

        boolean admin = ChatSessionSupport.isAdmin(session);
        boolean noticeRoom = ChatSessionSupport.ROOM_TYPE_NOTICE.equals(room.getRoomType());

        /* 관리자는 직접 URL로 접근해도 자유방에 들어갈 수 없습니다. */
        if (admin && !noticeRoom) {
            return REDIRECT_CHAT_LIST;
        }

        boolean joined = false;

        if (!admin && !noticeRoom) {
            Integer businessNo = ChatSessionSupport.getSessionBusinessNo(session);
            joined = chatService.isChatRoomMember(roomId, businessNo);

            if (!joined) {
                return REDIRECT_CHAT_LIST;
            }
        }

        ChatSessionSupport.addLoginChatAttributes(session, model);
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

        if (!ChatSessionSupport.hasChatAccess(session)) {
            return REDIRECT_MEMBER_LOGIN;
        }

        ChatSessionSupport.addLoginChatAttributes(session, model);

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

        if (!ChatSessionSupport.hasChatAccess(session)) {
            return REDIRECT_MEMBER_LOGIN;
        }

        boolean admin = ChatSessionSupport.isAdmin(session);
        Integer chatBusinessNo = ChatSessionSupport.getChatBusinessNo(session);

        chatRoom.setCreatedBy(chatBusinessNo);

        if (admin) {
            chatRoom.setRoomType(ChatSessionSupport.ROOM_TYPE_NOTICE);
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
            return REDIRECT_CHAT_LIST;
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

        if (!ChatSessionSupport.hasChatAccess(session)) {
            return REDIRECT_MEMBER_LOGIN;
        }

        ChatRoomVO room = chatService.getChatRoom(roomId);

        if (room == null) {
            return REDIRECT_CHAT_LIST;
        }

        boolean admin = ChatSessionSupport.isAdmin(session);
        boolean noticeRoom = ChatSessionSupport.ROOM_TYPE_NOTICE.equals(room.getRoomType());
        Integer businessNo = ChatSessionSupport.getSessionBusinessNo(session);

        boolean canDelete = admin
                ? noticeRoom
                : !noticeRoom
                        && businessNo != null
                        && room.getCreatedBy() == businessNo;

        if (!canDelete) {
            return REDIRECT_CHAT_LIST;
        }

        boolean result = chatService.deleteChatRoom(roomId);

        if (!result) {
            return "redirect:/chat/room/" + roomId;
        }

        return REDIRECT_CHAT_LIST;
    }

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "chat controller ok";
    }


}
