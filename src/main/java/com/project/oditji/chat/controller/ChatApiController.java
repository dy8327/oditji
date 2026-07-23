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

    private static final String ROOM_TYPE_NOTICE = "NOTICE";

    private final ChatService chatService;

    public ChatApiController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 전체 채팅방 목록을 JSON으로 반환합니다.
     */
    @GetMapping("/rooms")
    public List<ChatRoomVO> getRoomList() {
        return chatService.getChatRoomList();
    }

    /**
     * 로그인 사업자가 참여 중인 자유방 목록을 반환합니다.
     */
    @GetMapping("/rooms/my")
    public List<ChatRoomVO> getMyRoomList(HttpSession session) {

        Integer businessNo = getLoginBusinessNo(session);

        if (businessNo == null) {
            return List.of();
        }

        return chatService.getMyChatRoomList(businessNo);
    }

    /**
     * 채팅방 상세 정보를 반환합니다.
     */
    @GetMapping("/rooms/{roomId}")
    public ChatRoomVO getRoom(
            @PathVariable("roomId") String roomId) {

        return chatService.getChatRoom(roomId);
    }

    /**
     * 자유방 참가 API입니다.
     * 클라이언트가 전달한 businessNo를 사용하지 않고 로그인 세션 값을 사용합니다.
     */
    @PostMapping("/join")
    public ChatResponseVO joinRoom(
            @RequestParam("roomId") String roomId,
            HttpSession session) {

        Integer businessNo = getLoginBusinessNo(session);

        if (businessNo == null) {
            return new ChatResponseVO(
                    false,
                    ChatResult.FAIL,
                    "로그인한 사업자 정보를 확인할 수 없습니다.");
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
     * 나가기 버튼을 누른 사용자가 마지막 참여자인지 확인합니다.
     * 마지막 참여자라면 화면에서 채팅방 삭제 안내를 한 번 더 표시합니다.
     */
    @GetMapping("/leave/check")
    public Map<String, Object> checkLeaveRoom(
            @RequestParam("roomId") String roomId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        Integer businessNo = getLoginBusinessNo(session);

        if (businessNo == null) {
            response.put("success", false);
            response.put("message", "로그인한 사업자 정보를 확인할 수 없습니다.");
            response.put("willDeleteRoom", false);
            return response;
        }

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

        Integer businessNo = getLoginBusinessNo(session);

        if (businessNo == null) {
            return new ChatResponseVO(
                    false,
                    ChatResult.FAIL,
                    "로그인한 사업자 정보를 확인할 수 없습니다.");
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
     * 로그인 세션의 사업자 번호를 반환합니다.
     */
    private Integer getLoginBusinessNo(HttpSession session) {

        Object value = session.getAttribute("businessNo");

        if (value instanceof Number number) {
            return number.intValue();
        }

        return null;
    }
}
