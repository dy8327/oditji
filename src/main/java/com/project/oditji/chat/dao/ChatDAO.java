package com.project.oditji.chat.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.chat.vo.ChatNotificationRoomVO;
import com.project.oditji.chat.vo.ChatParticipantReadVO;
import com.project.oditji.chat.vo.ChatReadStateVO;
import com.project.oditji.chat.vo.ChatRoomVO;

@Mapper
public interface ChatDAO {

    /**
     * 전체 채팅방 조회
     */
    List<ChatRoomVO> selectChatRoomList();

    /**
     * 내가 참여한 채팅방 조회
     */
    List<ChatRoomVO> selectMyChatRoomList(int businessNo);

    /**
     * 채팅방 상세 조회
     */
    ChatRoomVO selectChatRoom(String roomId);

    /**
     * 채팅방 생성
     */
    int insertChatRoom(ChatRoomVO chatRoom);

    /**
     * 채팅방 수정
     */
    int updateChatRoom(ChatRoomVO chatRoom);

    /**
     * 채팅방 삭제 대신 비활성화 처리
     */
    int deleteChatRoom(String roomId);

    /**
     * 채팅방 참가
     */
    int insertChatRoomMember(
            @Param("roomId") String roomId,
            @Param("businessNo") int businessNo);

    /**
     * 채팅방 나가기
     */
    int deleteChatRoomMember(
            @Param("roomId") String roomId,
            @Param("businessNo") int businessNo);

    /**
     * 참가 여부 확인
     */
    int existsChatRoomMember(
            @Param("roomId") String roomId,
            @Param("businessNo") int businessNo);

    /**
     * 현재 채팅방 참여 인원 수 조회
     */
    int countChatRoomMembers(String roomId);

    /**
     * 채팅방 시퀀스 조회
     */
    int getNextRoomSequence();

    /**
     * 현재 사용자가 알림을 받아야 하는 채팅방과 마지막 읽음 위치를 조회합니다.
     *
     * 관리자는 공지방만, 사업자는 공지방과 현재 참가 중인 자유방만 조회합니다.
     */
    List<ChatNotificationRoomVO> selectNotificationRoomList(
            @Param("memberNo") long memberNo,
            @Param("businessNo") Integer businessNo,
            @Param("adminFlag") int adminFlag);

    /**
     * 자유방의 현재 참여자별 마지막 읽음 위치를 조회합니다.
     */
    List<ChatParticipantReadVO> selectPublicRoomParticipantReadList(
            String roomId);

    /**
     * 공지방을 열람하는 승인 사업자별 마지막 읽음 위치를 조회합니다.
     */
    List<ChatParticipantReadVO> selectNoticeRoomParticipantReadList(
            String roomId);

    /**
     * 사용자별 채팅방 마지막 읽음 위치를 저장하거나 앞으로 이동시킵니다.
     */
    int mergeChatReadState(ChatReadStateVO readState);
}
