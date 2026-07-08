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

@Controller
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    /*
     * 임시 로그인 사업자 정보
     * 나중에 로그인/세션 기능이 완성되면 이 부분을 제거하고
     * HttpSession에서 로그인 사용자의 businessNo, businessName을 가져오면 됩니다.
     */
    private static final int TEMP_BUSINESS_NO = 1;
    private static final String TEMP_BUSINESS_NAME = "테스트사업자";

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 전체 채팅방 목록
     */
    @GetMapping("/list")
    public String roomList(Model model) {

        List<ChatRoomVO> roomList = chatService.getChatRoomList();

        model.addAttribute("roomList", roomList);

        /*
         * roomList.jsp에서 AJAX 참가, 채팅방 입장 시 사용할 임시 사업자 정보
         */
        model.addAttribute("businessNo", TEMP_BUSINESS_NO);
        model.addAttribute("businessName", TEMP_BUSINESS_NAME);

        return "chat/roomList";
    }

    /**
     * 내가 참여한 채팅방 목록
     *
     * 현재는 임시 사업자 번호 사용
     * 나중에 로그인 세션에서 businessNo를 가져오도록 변경 예정
     */
    @GetMapping("/my")
    public String myRoomList(Model model) {

        List<ChatRoomVO> roomList =
                chatService.getMyChatRoomList(TEMP_BUSINESS_NO);

        model.addAttribute("roomList", roomList);

        model.addAttribute("businessNo", TEMP_BUSINESS_NO);
        model.addAttribute("businessName", TEMP_BUSINESS_NAME);

        return "chat/roomList";
    }

    /**
     * 채팅방 상세 화면
     */
    @GetMapping("/room/{roomId}")
    public String room(
            @PathVariable("roomId") String roomId,
            Model model) {

        ChatRoomVO room = chatService.getChatRoom(roomId);

        if (room == null) {
            return "redirect:/chat/list";
        }

        model.addAttribute("room", room);

        /*
         * room.jsp에서 Firebase 메시지 전송 시 사용할 임시 사업자 정보
         */
        model.addAttribute("businessNo", TEMP_BUSINESS_NO);
        model.addAttribute("businessName", TEMP_BUSINESS_NAME);

        return "chat/room";
    }

    /**
     * 채팅방 생성 화면
     */
    @GetMapping("/create")
    public String createForm(Model model) {

        /*
         * createRoom.jsp에서 필요할 수 있는 임시 사업자 정보
         */
        model.addAttribute("businessNo", TEMP_BUSINESS_NO);
        model.addAttribute("businessName", TEMP_BUSINESS_NAME);

        return "chat/createRoom";
    }

    /**
     * 채팅방 생성
     */
    @PostMapping("/create")
    public String create(ChatRoomVO chatRoom) {

        /*
         * 현재는 로그인 기능이 없으므로 임시 사업자 번호를 생성자로 지정
         * 나중에 Session에서 가져온 businessNo로 교체하면 됩니다.
         */
        chatRoom.setCreatedBy(TEMP_BUSINESS_NO);

        if (chatRoom.getRoomType() == null || chatRoom.getRoomType().trim().equals("")) {
            chatRoom.setRoomType("PUBLIC");
        }

        if (chatRoom.getMaxMember() <= 0) {
            chatRoom.setMaxMember(100);
        }

        if (chatRoom.getIsDefault() == null || chatRoom.getIsDefault().trim().equals("")) {
            chatRoom.setIsDefault("N");
        }

        String roomId = chatService.createChatRoom(chatRoom);

        if (roomId == null) {
            return "redirect:/chat/list";
        }

        return "redirect:/chat/room/" + roomId;
    }

    /**
     * 채팅방 삭제
     */
    @PostMapping("/delete")
    public String delete(
            @RequestParam("roomId") String roomId) {

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
}