package com.project.oditji.chat.service;

import java.util.List;

import com.project.oditji.chat.vo.ChatRoomVO;

public interface ChatService {

    /** 전체 활성 채팅방 조회 */
    List<ChatRoomVO> getChatRoomList();

    /** 로그인 사업자가 참여 중인 자유방 조회 */
    List<ChatRoomVO> getMyChatRoomList(int businessNo);

    /** 채팅방 상세 조회 */
    ChatRoomVO getChatRoom(String roomId);

    /** 채팅방 생성 */
    String createChatRoom(ChatRoomVO chatRoom);

    /** 채팅방 수정 */
    boolean updateChatRoom(ChatRoomVO chatRoom);

    /** 채팅방 비활성화 */
    boolean deleteChatRoom(String roomId);

    /** 자유방 참가 */
    int joinChatRoom(String roomId, int businessNo);

    /** 자유방 나가기 */
    boolean leaveChatRoom(String roomId, int businessNo);

    /** 특정 사업자의 자유방 참가 여부 확인 */
    boolean isChatRoomMember(String roomId, int businessNo);
}
