package com.project.oditji.chat.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.oditji.chat.common.ChatResult;
import com.project.oditji.chat.service.ChatService;
import com.project.oditji.chat.vo.ChatResponseVO;
import com.project.oditji.chat.vo.ChatRoomVO;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/chat/api")
public class ChatApiController {

    private static final long ADMIN_MEMBER_NO = 1L;
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROOM_TYPE_NOTICE = "NOTICE";

    private final ChatService chatService;

    public ChatApiController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 로그인 권한에 맞는 채팅방 목록을 JSON으로 반환합니다.
     * 관리자는 공지방만, 사업자는 공지방과 자유방 전체를 확인합니다.
     */
    @GetMapping("/rooms")
    public List<ChatRoomVO> getRoomList(HttpSession session) {

        if (!hasChatAccess(session)) {
            return List.of();
        }

        List<ChatRoomVO> roomList = chatService.getChatRoomList();

        if (isAdmin(session)) {
            return filterNoticeRooms(roomList);
        }

        return roomList;
    }

    /**
     * 사업자는 자신이 참여 중인 자유방 목록을 반환합니다.
     * 관리자는 자유방을 이용하지 않으므로 공지방 목록만 반환합니다.
     */
    @GetMapping("/rooms/my")
    public List<ChatRoomVO> getMyRoomList(HttpSession session) {

        if (!hasChatAccess(session)) {
            return List.of();
        }

        if (isAdmin(session)) {
            return filterNoticeRooms(chatService.getChatRoomList());
        }

        Integer businessNo = getSessionBusinessNo(session);
        return chatService.getMyChatRoomList(businessNo);
    }

    /**
     * 권한이 있는 채팅방 상세 정보만 반환합니다.
     */
    @GetMapping("/rooms/{roomId}")
    public ChatRoomVO getRoom(
            @PathVariable("roomId") String roomId,
            HttpSession session) {

        if (!hasChatAccess(session)) {
            return null;
        }

        ChatRoomVO room = chatService.getChatRoom(roomId);

        if (room == null) {
            return null;
        }

        boolean noticeRoom = ROOM_TYPE_NOTICE.equals(room.getRoomType());

        if (isAdmin(session)) {
            return noticeRoom ? room : null;
        }

        if (noticeRoom) {
            return room;
        }

        Integer businessNo = getSessionBusinessNo(session);

        return chatService.isChatRoomMember(roomId, businessNo)
                ? room
                : null;
    }

    /**
     * 자유방 참가 API입니다.
     * 관리자는 자유방 참가를 할 수 없고 공지방만 이용합니다.
     */
    @PostMapping("/join")
    public ChatResponseVO joinRoom(
            @RequestParam("roomId") String roomId,
            HttpSession session) {

        if (!hasChatAccess(session)) {
            return new ChatResponseVO(
                    false,
                    ChatResult.FAIL,
                    "로그인한 관리자 또는 사업자 정보를 확인할 수 없습니다.");
        }

        ChatRoomVO room = chatService.getChatRoom(roomId);

        if (room == null) {
            return new ChatResponseVO(
                    false,
                    ChatResult.ROOM_NOT_FOUND,
                    "존재하지 않는 채팅방입니다.");
        }

        if (ROOM_TYPE_NOTICE.equals(room.getRoomType())) {
            return new ChatResponseVO(
                    true,
                    ChatResult.SUCCESS,
                    "공지방으로 이동합니다.");
        }

        if (isAdmin(session)) {
            return new ChatResponseVO(
                    false,
                    ChatResult.FAIL,
                    "관리자는 공지방만 이용할 수 있습니다.");
        }

        Integer businessNo = getSessionBusinessNo(session);
        int result = chatService.joinChatRoom(roomId, businessNo);

        return switch (result) {
            case ChatResult.SUCCESS ->
                new ChatResponseVO(true, result, "채팅방에 참가했습니다.");

            case ChatResult.ALREADY_JOINED ->
                new ChatResponseVO(true, result, "이미 참가한 채팅방입니다.");

            case ChatResult.ROOM_FULL ->
                new ChatResponseVO(false, result, "채팅방 정원이 가득 찼습니다.");

            case ChatResult.ROOM_NOT_FOUND ->
                new ChatResponseVO(false, result, "존재하지 않는 채팅방입니다.");

            default ->
                new ChatResponseVO(false, result, "채팅방 참가에 실패했습니다.");
        };
    }

    /**
     * 나가기 버튼을 누른 사업자가 마지막 참여자인지 확인합니다.
     */
    @GetMapping("/leave/check")
    public Map<String, Object> checkLeaveRoom(
            @RequestParam("roomId") String roomId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (!hasChatAccess(session)) {
            response.put("success", false);
            response.put("message", "로그인한 관리자 또는 사업자 정보를 확인할 수 없습니다.");
            response.put("willDeleteRoom", false);
            return response;
        }

        if (isAdmin(session)) {
            response.put("success", false);
            response.put("message", "관리자는 자유방 나가기 기능을 사용할 수 없습니다.");
            response.put("willDeleteRoom", false);
            return response;
        }

        Integer businessNo = getSessionBusinessNo(session);
        ChatRoomVO room = chatService.getChatRoom(roomId);

        if (room == null) {
            response.put("success", false);
            response.put("message", "존재하지 않는 채팅방입니다.");
            response.put("willDeleteRoom", false);
            return response;
        }

        if (ROOM_TYPE_NOTICE.equals(room.getRoomType())) {
            response.put("success", false);
            response.put("message", "공지방에서는 나가기 기능을 사용할 수 없습니다.");
            response.put("willDeleteRoom", false);
            return response;
        }

        if (!chatService.isChatRoomMember(roomId, businessNo)) {
            response.put("success", false);
            response.put("message", "채팅방 참여 정보를 찾을 수 없습니다.");
            response.put("willDeleteRoom", false);
            return response;
        }

        boolean willDeleteRoom =
                chatService.willRoomBeEmptyAfterLeave(roomId, businessNo);

        response.put("success", true);
        response.put("willDeleteRoom", willDeleteRoom);
        response.put(
                "message",
                willDeleteRoom
                        ? "나가면 참여자가 없어 채팅방이 삭제됩니다."
                        : "채팅방에서 나갈 수 있습니다.");

        return response;
    }

    /**
     * 자유방 나가기 API입니다.
     * 마지막 참여자가 나가면 Service 트랜잭션에서 방을 비활성화합니다.
     */
    @PostMapping("/leave")
    public ChatResponseVO leaveRoom(
            @RequestParam("roomId") String roomId,
            HttpSession session) {

        if (!hasChatAccess(session)) {
            return new ChatResponseVO(
                    false,
                    ChatResult.FAIL,
                    "로그인한 관리자 또는 사업자 정보를 확인할 수 없습니다.");
        }

        if (isAdmin(session)) {
            return new ChatResponseVO(
                    false,
                    ChatResult.FAIL,
                    "관리자는 자유방 나가기 기능을 사용할 수 없습니다.");
        }

        Integer businessNo = getSessionBusinessNo(session);
        ChatRoomVO room = chatService.getChatRoom(roomId);

        if (room == null) {
            return new ChatResponseVO(
                    false,
                    ChatResult.ROOM_NOT_FOUND,
                    "존재하지 않는 채팅방입니다.");
        }

        if (ROOM_TYPE_NOTICE.equals(room.getRoomType())) {
            return new ChatResponseVO(
                    false,
                    ChatResult.FAIL,
                    "공지방에서는 나가기 기능을 사용할 수 없습니다.");
        }

        boolean roomWillBeDeleted =
                chatService.willRoomBeEmptyAfterLeave(roomId, businessNo);

        boolean result = chatService.leaveChatRoom(roomId, businessNo);

        if (result) {
            return new ChatResponseVO(
                    true,
                    ChatResult.SUCCESS,
                    roomWillBeDeleted
                            ? "채팅방에서 나갔으며, 참여자가 없어 채팅방이 삭제되었습니다."
                            : "채팅방에서 나갔습니다.");
        }

        return new ChatResponseVO(
                false,
                ChatResult.FAIL,
                "채팅방 참여 정보를 찾을 수 없습니다.");
    }

    /**
     * 관리자 여부를 MEMBER_NO=1, ROLE=ADMIN 기준으로 확인합니다.
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
