package com.project.oditji.chat.service;

import java.util.List;

import com.project.oditji.chat.vo.ChatRoomVO;

public interface ChatService {

    /**
     * 전체 채팅방 조회
     */
    List<ChatRoomVO> getChatRoomList();

    /**
     * 내가 참여한 채팅방 조회
     */
    List<ChatRoomVO> getMyChatRoomList(int businessNo);

    /**
     * 채팅방 상세 조회
     */
    ChatRoomVO getChatRoom(String roomId);

    /**
     * 채팅방 생성
     */
    String createChatRoom(ChatRoomVO chatRoom);

    /**
     * 채팅방 수정
     */
    boolean updateChatRoom(ChatRoomVO chatRoom);

    /**
     * 채팅방 삭제
     */
    boolean deleteChatRoom(String roomId);

    /**
     * 채팅방 참가
     */
    int joinChatRoom(String roomId, int businessNo);

    /**
     * 채팅방 나가기
     */
    boolean leaveChatRoom(String roomId, int businessNo);

}