package com.project.oditji.chat.controller;

import java.util.List;

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

@RestController
@RequestMapping("/chat/api")
public class ChatApiController {

    private final ChatService chatService;

    public ChatApiController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 전체 채팅방 조회(JSON)
     */
    @GetMapping("/rooms")
    public List<ChatRoomVO> getRoomList() {

        return chatService.getChatRoomList();

    }

    /**
     * 내가 참여한 채팅방(JSON)
     *
     * 현재는 테스트용
     * 로그인 이후 Session으로 변경
     */
    @GetMapping("/rooms/my")
    public List<ChatRoomVO> getMyRoomList(
            @RequestParam int businessNo) {

        return chatService.getMyChatRoomList(businessNo);

    }

    /**
     * 채팅방 상세조회(JSON)
     */
    @GetMapping("/rooms/{roomId}")
    public ChatRoomVO getRoom(
            @PathVariable String roomId) {

        return chatService.getChatRoom(roomId);

    }

    /**
     * 채팅방 참가
     */
    @PostMapping("/join")
    public ChatResponseVO joinRoom(
            @RequestParam String roomId,
            @RequestParam int businessNo) {

        int result = chatService.joinChatRoom(roomId, businessNo);

        ChatResponseVO response = new ChatResponseVO();

        switch (result) {

            case ChatResult.SUCCESS:

                response.setSuccess(true);
                response.setCode(result);
                response.setMessage("채팅방에 참가했습니다.");

                break;

            case ChatResult.ALREADY_JOINED:

                response.setSuccess(false);
                response.setCode(result);
                response.setMessage("이미 참가한 채팅방입니다.");

                break;

            case ChatResult.ROOM_FULL:

                response.setSuccess(false);
                response.setCode(result);
                response.setMessage("채팅방 정원이 가득 찼습니다.");

                break;

            case ChatResult.ROOM_NOT_FOUND:

                response.setSuccess(false);
                response.setCode(result);
                response.setMessage("존재하지 않는 채팅방입니다.");

                break;

            default:

                response.setSuccess(false);
                response.setCode(result);
                response.setMessage("채팅방 참가에 실패했습니다.");

        }

        return response;

    }

    /**
     * 채팅방 나가기
     */
    @PostMapping("/leave")
    public ChatResponseVO leaveRoom(
            @RequestParam("roomId") String roomId,
            @RequestParam("businessNo") int businessNo) {

        boolean result = chatService.leaveChatRoom(roomId, businessNo);

        if (result) {
            return new ChatResponseVO(
                    true,
                    ChatResult.SUCCESS,
                    "채팅방에서 나갔습니다."
            );
        }

        return new ChatResponseVO(
                false,
                ChatResult.FAIL,
                "채팅방 나가기에 실패했습니다."
        );
    }
}