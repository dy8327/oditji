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
     * 현재 사용자가 자유방에서 나갈 경우
     * 참여 인원이 0명이 되는지 확인합니다.
     */
    boolean willRoomBeEmptyAfterLeave(
            String roomId,
            int businessNo);

    /**
     * 자유방에서 나갑니다.
     * 마지막 참여자가 나간 경우 채팅방을 비활성화합니다.
     */
    boolean leaveChatRoom(String roomId, int businessNo);

    /**
     * 자유방 참가 여부 확인
     */
    boolean isChatRoomMember(String roomId, int businessNo);
}
