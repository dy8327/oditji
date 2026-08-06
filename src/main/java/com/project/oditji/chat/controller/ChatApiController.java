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
import com.project.oditji.chat.support.ChatSessionSupport;
import com.project.oditji.chat.vo.ChatNotificationContextVO;
import com.project.oditji.chat.vo.ChatNotificationRoomVO;
import com.project.oditji.chat.vo.ChatParticipantReadVO;
import com.project.oditji.chat.vo.ChatReadStateVO;
import com.project.oditji.chat.vo.ChatResponseVO;
import com.project.oditji.chat.vo.ChatRoomVO;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/chat/api")
public class ChatApiController {

    private static final String MESSAGE_LOGIN_INFO_NOT_FOUND = "로그인한 관리자 또는 사업자 정보를 확인할 수 없습니다.";
    private static final String MESSAGE_ROOM_NOT_FOUND = "존재하지 않는 채팅방입니다.";
    private static final String RESPONSE_SUCCESS = "success";
    private static final String RESPONSE_MESSAGE = "message";
    private static final String RESPONSE_WILL_DELETE_ROOM = "willDeleteRoom";

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

        if (!ChatSessionSupport.hasChatAccess(session)) {
            return List.of();
        }

        List<ChatRoomVO> roomList = chatService.getChatRoomList();

        if (ChatSessionSupport.isAdmin(session)) {
            return ChatSessionSupport.filterNoticeRooms(roomList);
        }

        return roomList;
    }

    /**
     * 사업자는 자신이 참여 중인 자유방 목록을 반환합니다.
     * 관리자는 자유방을 이용하지 않으므로 공지방 목록만 반환합니다.
     */
    @GetMapping("/rooms/my")
    public List<ChatRoomVO> getMyRoomList(HttpSession session) {

        if (!ChatSessionSupport.hasChatAccess(session)) {
            return List.of();
        }

        if (ChatSessionSupport.isAdmin(session)) {
            return ChatSessionSupport.filterNoticeRooms(chatService.getChatRoomList());
        }

        Integer businessNo = ChatSessionSupport.getSessionBusinessNo(session);
        return chatService.getMyChatRoomList(businessNo);
    }

    /**
     * 권한이 있는 채팅방 상세 정보만 반환합니다.
     */
    @GetMapping("/rooms/{roomId}")
    public ChatRoomVO getRoom(
            @PathVariable("roomId") String roomId,
            HttpSession session) {

        return getAccessibleRoom(roomId, session);
    }

    /**
     * 공통 헤더의 채팅 알림 계산에 필요한 정보를 반환합니다.
     *
     * 헤더 숫자는 미읽은 메시지 총개수가 아니라
     * 미읽은 메시지가 존재하는 채팅방 개수로 계산합니다.
     */
    @GetMapping("/notifications/context")
    public ChatNotificationContextVO getNotificationContext(
            HttpSession session) {

        Long memberNo = ChatSessionSupport.getSessionMemberNo(session);
        Integer sessionBusinessNo = ChatSessionSupport.getSessionBusinessNo(session);
        String role = ChatSessionSupport.getSessionRole(session);
        boolean admin = ChatSessionSupport.isAdmin(session);
        Integer notificationBusinessNo = admin
                ? Integer.valueOf(ChatSessionSupport.ADMIN_BUSINESS_NO)
                : sessionBusinessNo;

        if (!ChatSessionSupport.hasChatAccess(session) || memberNo == null) {
            return new ChatNotificationContextVO(
                    false,
                    memberNo,
                    notificationBusinessNo,
                    role,
                    List.of());
        }

        List<ChatNotificationRoomVO> roomList =
                chatService.getNotificationRoomList(
                        memberNo,
                        notificationBusinessNo,
                        admin);

        return new ChatNotificationContextVO(
                true,
                memberNo,
                notificationBusinessNo,
                role,
                roomList);
    }

    /**
     * 채팅방 참여자별 마지막 읽음 위치를 반환합니다.
     *
     * 자유방은 현재 CHAT_ROOM_MEMBER 참여자를 사용하고,
     * 공지방은 승인된 활성 사업자 전체를 논리적 참여자로 사용합니다.
     */
    @GetMapping("/rooms/{roomId}/readers")
    public List<ChatParticipantReadVO> getRoomReaders(
            @PathVariable("roomId") String roomId,
            HttpSession session) {

        if (getAccessibleRoom(roomId, session) == null) {
            return List.of();
        }

        return chatService.getChatParticipantReadList(roomId);
    }

    /**
     * 현재 사용자의 채팅방 마지막 읽음 위치를 저장합니다.
     *
     * 클라이언트가 전달한 MEMBER_NO는 사용하지 않고
     * 로그인 세션의 회원 번호만 사용합니다.
     */
    @PostMapping("/read")
    public ChatResponseVO saveReadState(
            @RequestParam("roomId") String roomId,
            @RequestParam("lastReadMessageId") String lastReadMessageId,
            @RequestParam("lastReadEpochMs") long lastReadEpochMs,
            HttpSession session) {

        Long memberNo = ChatSessionSupport.getSessionMemberNo(session);

        if (memberNo == null || !ChatSessionSupport.hasChatAccess(session)) {
            return new ChatResponseVO(
                    false,
                    ChatResult.FAIL,
                    MESSAGE_LOGIN_INFO_NOT_FOUND);
        }

        if (getAccessibleRoom(roomId, session) == null) {
            return new ChatResponseVO(
                    false,
                    ChatResult.FAIL,
                    "읽음 처리 권한이 없는 채팅방입니다.");
        }

        if (lastReadMessageId == null
                || lastReadMessageId.isBlank()
                || lastReadMessageId.length() > 100
                || lastReadEpochMs < 0L) {

            return new ChatResponseVO(
                    false,
                    ChatResult.FAIL,
                    "마지막 읽음 위치가 올바르지 않습니다.");
        }

        ChatReadStateVO readState = new ChatReadStateVO(
                roomId,
                memberNo,
                lastReadMessageId,
                lastReadEpochMs);

        boolean saved = chatService.saveChatReadState(readState);

        if (saved) {
            return new ChatResponseVO(
                    true,
                    ChatResult.SUCCESS,
                    "채팅 읽음 위치를 저장했습니다.");
        }

        return new ChatResponseVO(
                false,
                ChatResult.FAIL,
                "채팅 읽음 위치 저장에 실패했습니다.");
    }

    /**
     * 자유방 참가 API입니다.
     * 관리자는 자유방 참가를 할 수 없고 공지방만 이용합니다.
     */
    @PostMapping("/join")
    public ChatResponseVO joinRoom(
            @RequestParam("roomId") String roomId,
            HttpSession session) {

        if (!ChatSessionSupport.hasChatAccess(session)) {
            return new ChatResponseVO(
                    false,
                    ChatResult.FAIL,
                    MESSAGE_LOGIN_INFO_NOT_FOUND);
        }

        ChatRoomVO room = chatService.getChatRoom(roomId);

        if (room == null) {
            return new ChatResponseVO(
                    false,
                    ChatResult.ROOM_NOT_FOUND,
                    MESSAGE_ROOM_NOT_FOUND);
        }

        if (ChatSessionSupport.ROOM_TYPE_NOTICE.equals(room.getRoomType())) {
            return new ChatResponseVO(
                    true,
                    ChatResult.SUCCESS,
                    "공지방으로 이동합니다.");
        }

        if (ChatSessionSupport.isAdmin(session)) {
            return new ChatResponseVO(
                    false,
                    ChatResult.FAIL,
                    "관리자는 공지방만 이용할 수 있습니다.");
        }

        Integer businessNo = ChatSessionSupport.getSessionBusinessNo(session);
        int result = chatService.joinChatRoom(roomId, businessNo);

        return switch (result) {
            case ChatResult.SUCCESS ->
                new ChatResponseVO(true, result, "채팅방에 참가했습니다.");

            case ChatResult.ALREADY_JOINED ->
                new ChatResponseVO(true, result, "이미 참가한 채팅방입니다.");

            case ChatResult.ROOM_FULL ->
                new ChatResponseVO(false, result, "채팅방 정원이 가득 찼습니다.");

            case ChatResult.ROOM_NOT_FOUND ->
                new ChatResponseVO(false, result, MESSAGE_ROOM_NOT_FOUND);

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

        if (!ChatSessionSupport.hasChatAccess(session)) {
            response.put(RESPONSE_SUCCESS, false);
            response.put(RESPONSE_MESSAGE, MESSAGE_LOGIN_INFO_NOT_FOUND);
            response.put(RESPONSE_WILL_DELETE_ROOM, false);
            return response;
        }

        if (ChatSessionSupport.isAdmin(session)) {
            response.put(RESPONSE_SUCCESS, false);
            response.put(RESPONSE_MESSAGE, "관리자는 자유방 나가기 기능을 사용할 수 없습니다.");
            response.put(RESPONSE_WILL_DELETE_ROOM, false);
            return response;
        }

        Integer businessNo = ChatSessionSupport.getSessionBusinessNo(session);
        ChatRoomVO room = chatService.getChatRoom(roomId);

        if (room == null) {
            response.put(RESPONSE_SUCCESS, false);
            response.put(RESPONSE_MESSAGE, MESSAGE_ROOM_NOT_FOUND);
            response.put(RESPONSE_WILL_DELETE_ROOM, false);
            return response;
        }

        if (ChatSessionSupport.ROOM_TYPE_NOTICE.equals(room.getRoomType())) {
            response.put(RESPONSE_SUCCESS, false);
            response.put(RESPONSE_MESSAGE, "공지방에서는 나가기 기능을 사용할 수 없습니다.");
            response.put(RESPONSE_WILL_DELETE_ROOM, false);
            return response;
        }

        if (!chatService.isChatRoomMember(roomId, businessNo)) {
            response.put(RESPONSE_SUCCESS, false);
            response.put(RESPONSE_MESSAGE, "채팅방 참여 정보를 찾을 수 없습니다.");
            response.put(RESPONSE_WILL_DELETE_ROOM, false);
            return response;
        }

        boolean willDeleteRoom =
                chatService.willRoomBeEmptyAfterLeave(roomId, businessNo);

        response.put(RESPONSE_SUCCESS, true);
        response.put(RESPONSE_WILL_DELETE_ROOM, willDeleteRoom);
        response.put(
                RESPONSE_MESSAGE,
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

        if (!ChatSessionSupport.hasChatAccess(session)) {
            return new ChatResponseVO(
                    false,
                    ChatResult.FAIL,
                    MESSAGE_LOGIN_INFO_NOT_FOUND);
        }

        if (ChatSessionSupport.isAdmin(session)) {
            return new ChatResponseVO(
                    false,
                    ChatResult.FAIL,
                    "관리자는 자유방 나가기 기능을 사용할 수 없습니다.");
        }

        Integer businessNo = ChatSessionSupport.getSessionBusinessNo(session);
        ChatRoomVO room = chatService.getChatRoom(roomId);

        if (room == null) {
            return new ChatResponseVO(
                    false,
                    ChatResult.ROOM_NOT_FOUND,
                    MESSAGE_ROOM_NOT_FOUND);
        }

        if (ChatSessionSupport.ROOM_TYPE_NOTICE.equals(room.getRoomType())) {
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
     * 현재 로그인 사용자가 접근할 수 있는 채팅방인지 확인합니다.
     */
    private ChatRoomVO getAccessibleRoom(
            String roomId,
            HttpSession session) {

        if (!ChatSessionSupport.hasChatAccess(session)) {
            return null;
        }

        ChatRoomVO room = chatService.getChatRoom(roomId);

        if (room == null) {
            return null;
        }

        boolean noticeRoom = ChatSessionSupport.ROOM_TYPE_NOTICE.equals(room.getRoomType());

        if (ChatSessionSupport.isAdmin(session)) {
            return noticeRoom ? room : null;
        }

        if (noticeRoom) {
            return room;
        }

        Integer businessNo = ChatSessionSupport.getSessionBusinessNo(session);

        return businessNo != null
                && chatService.isChatRoomMember(roomId, businessNo)
                        ? room
                        : null;
    }


}
