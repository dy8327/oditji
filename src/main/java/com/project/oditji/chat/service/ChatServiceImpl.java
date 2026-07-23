package com.project.oditji.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.chat.common.ChatResult;
import com.project.oditji.chat.dao.ChatDAO;
import com.project.oditji.chat.vo.ChatRoomVO;

@Service
public class ChatServiceImpl implements ChatService {

    private static final String ROOM_TYPE_NOTICE = "NOTICE";

    private final ChatDAO chatDAO;

    public ChatServiceImpl(ChatDAO chatDAO) {
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

    /**
     * 채팅방과 필요한 참가 정보를 하나의 트랜잭션으로 저장합니다.
     * 공지방은 전체 열람형이므로 CHAT_ROOM_MEMBER에 생성자를 넣지 않고,
     * 자유방만 생성자를 첫 참가자로 자동 등록합니다.
     */
    @Override
    @Transactional
    public String createChatRoom(ChatRoomVO chatRoom) {

        int sequence = chatDAO.getNextRoomSequence();
        String roomId = "ROOM_" + sequence;

        chatRoom.setRoomId(roomId);

        int roomInsertResult = chatDAO.insertChatRoom(chatRoom);

        if (roomInsertResult <= 0) {
            return null;
        }

        if (!ROOM_TYPE_NOTICE.equals(chatRoom.getRoomType())) {

            int memberInsertResult = chatDAO.insertChatRoomMember(
                    roomId,
                    chatRoom.getCreatedBy());

            if (memberInsertResult <= 0) {
                throw new IllegalStateException(
                        "채팅방 생성자 참가 정보 저장에 실패했습니다.");
            }
        }

        return roomId;
    }

    @Override
    public boolean updateChatRoom(ChatRoomVO chatRoom) {
        return chatDAO.updateChatRoom(chatRoom) > 0;
    }

    /**
     * 기본 공지방은 삭제할 수 없도록 보호합니다.
     */
    @Override
    public boolean deleteChatRoom(String roomId) {

        ChatRoomVO room = chatDAO.selectChatRoom(roomId);

        if (room == null) {
            return false;
        }

        if ("Y".equals(room.getIsDefault())) {
            return false;
        }

        return chatDAO.deleteChatRoom(roomId) > 0;
    }

    /**
     * 자유방만 참가할 수 있습니다.
     */
    @Override
    @Transactional
    public int joinChatRoom(String roomId, int businessNo) {

        ChatRoomVO room = chatDAO.selectChatRoom(roomId);

        if (room == null) {
            return ChatResult.ROOM_NOT_FOUND;
        }

        if (ROOM_TYPE_NOTICE.equals(room.getRoomType())) {
            return ChatResult.FAIL;
        }

        int exists = chatDAO.existsChatRoomMember(roomId, businessNo);

        if (exists > 0) {
            return ChatResult.ALREADY_JOINED;
        }

        if (room.getMemberCount() >= room.getMaxMember()) {
            return ChatResult.ROOM_FULL;
        }

        int result = chatDAO.insertChatRoomMember(roomId, businessNo);

        return result > 0
                ? ChatResult.SUCCESS
                : ChatResult.FAIL;
    }

    /**
     * 현재 로그인 사업자가 자유방의 마지막 참여자인지 확인합니다.
     *
     * 방이 없거나 공지방이거나, 현재 사용자가 참여 중이 아니라면
     * 마지막 참여자로 판단하지 않습니다.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean willRoomBeEmptyAfterLeave(
            String roomId,
            int businessNo) {

        ChatRoomVO room = chatDAO.selectChatRoom(roomId);

        if (room == null
                || ROOM_TYPE_NOTICE.equals(room.getRoomType())) {

            return false;
        }

        int membershipCount =
                chatDAO.existsChatRoomMember(roomId, businessNo);

        if (membershipCount <= 0) {
            return false;
        }

        return chatDAO.countChatRoomMembers(roomId) <= 1;
    }

    /**
     * 자유방 참가 기록을 삭제합니다.
     *
     * 삭제 후 참여 인원이 0명이 되면 방을 물리 삭제하지 않고
     * STATUS를 INACTIVE로 변경합니다. Firestore 메시지 기록은
     * 현재 단계에서는 유지되며, 방 목록과 상세 조회에서는 제외됩니다.
     */
    @Override
    @Transactional
    public boolean leaveChatRoom(String roomId, int businessNo) {

        ChatRoomVO room = chatDAO.selectChatRoom(roomId);

        if (room == null
                || ROOM_TYPE_NOTICE.equals(room.getRoomType())) {

            return false;
        }

        int deleteMemberResult =
                chatDAO.deleteChatRoomMember(roomId, businessNo);

        if (deleteMemberResult <= 0) {
            return false;
        }

        int remainingMemberCount =
                chatDAO.countChatRoomMembers(roomId);

        if (remainingMemberCount == 0) {

            int deactivateRoomResult =
                    chatDAO.deleteChatRoom(roomId);

            if (deactivateRoomResult <= 0) {
                throw new IllegalStateException(
                        "참여자가 없는 채팅방 비활성화에 실패했습니다.");
            }
        }

        return true;
    }

    @Override
    public boolean isChatRoomMember(String roomId, int businessNo) {
        return chatDAO.existsChatRoomMember(roomId, businessNo) > 0;
    }
}
