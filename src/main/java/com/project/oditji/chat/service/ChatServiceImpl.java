package com.project.oditji.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.chat.dao.ChatDAO;
import com.project.oditji.chat.vo.ChatRoomVO;
import com.project.oditji.chat.common.ChatResult;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatDAO chatDAO;

    ChatServiceImpl(ChatDAO chatDAO) {
        this.chatDAO = chatDAO;
    }

    @Override
    public List<ChatRoomVO> getChatRoomList() {
        return chatDAO.selectChatRoomList();
    }

    @Override
    public List<ChatRoomVO> getMyChatRoomList(int businessNo) {
        return chatDAO.selectMyChatRoomList(businessNo);
    }

    @Override
    public ChatRoomVO getChatRoom(String roomId) {
        return chatDAO.selectChatRoom(roomId);
    }

    @Override
    public String createChatRoom(ChatRoomVO chatRoom) {

        // 시퀀스 조회
        int seq = chatDAO.getNextRoomSequence();

        // ROOM_ID 생성
        String roomId = "ROOM_" + seq;

        chatRoom.setRoomId(roomId);

        // 채팅방 생성
        int result = chatDAO.insertChatRoom(chatRoom);

        if(result == 0) {
            return null;
        }

        // 방장 자동 참가
        chatDAO.insertChatRoomMember(
                roomId,
                chatRoom.getCreatedBy());

        return roomId;
    }

    @Override
    public boolean updateChatRoom(ChatRoomVO chatRoom) {

        return chatDAO.updateChatRoom(chatRoom) > 0;
    }

    @Override
    public boolean deleteChatRoom(String roomId) {

        ChatRoomVO room = chatDAO.selectChatRoom(roomId);

        if(room == null) {
            return false;
        }

        // 기본 채팅방 삭제 금지
        if("Y".equals(room.getIsDefault())) {
            return false;
        }

        return chatDAO.deleteChatRoom(roomId) > 0;
    }

    @Override
    @Transactional
    public int joinChatRoom(String roomId, int businessNo) {

        // 이미 참가 중인지 확인
        int exists = chatDAO.existsChatRoomMember(roomId, businessNo);

        if (exists > 0) {
            return ChatResult.ALREADY_JOINED;
        }

        ChatRoomVO room = chatDAO.selectChatRoom(roomId);

        if (room == null) {
            return ChatResult.ROOM_NOT_FOUND;
        }

        // 정원 초과
        if (room.getMemberCount() >= room.getMaxMember()) {
            return ChatResult.ROOM_FULL;
        }

        int result = chatDAO.insertChatRoomMember(roomId, businessNo);

        if (result > 0) {
            return ChatResult.SUCCESS;
        }

        return ChatResult.FAIL;
    }

    @Override
    public boolean leaveChatRoom(String roomId, int businessNo) {

        return chatDAO.deleteChatRoomMember(roomId, businessNo) > 0;
    }

}