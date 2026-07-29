package com.project.oditji.chat.vo;

/**
 * 사용자별 채팅방 마지막 읽음 위치를 전달하는 VO입니다.
 *
 * 메시지마다 읽은 사용자 배열을 저장하지 않고,
 * 사용자 한 명이 채팅방에서 마지막으로 읽은 Firestore 메시지 ID와
 * 해당 메시지의 epoch millisecond만 Oracle에 보관합니다.
 */
public class ChatReadStateVO {

    private String roomId;
    private Long memberNo;
    private String lastReadMessageId;
    private Long lastReadEpochMs;

    public ChatReadStateVO() {
    }

    public ChatReadStateVO(
            String roomId,
            Long memberNo,
            String lastReadMessageId,
            Long lastReadEpochMs) {

        this.roomId = roomId;
        this.memberNo = memberNo;
        this.lastReadMessageId = lastReadMessageId;
        this.lastReadEpochMs = lastReadEpochMs;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public String getLastReadMessageId() {
        return lastReadMessageId;
    }

    public void setLastReadMessageId(String lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }

    public Long getLastReadEpochMs() {
        return lastReadEpochMs;
    }

    public void setLastReadEpochMs(Long lastReadEpochMs) {
        this.lastReadEpochMs = lastReadEpochMs;
    }

    @Override
    public String toString() {
        return "ChatReadStateVO [roomId=" + roomId
                + ", memberNo=" + memberNo
                + ", lastReadMessageId=" + lastReadMessageId
                + ", lastReadEpochMs=" + lastReadEpochMs + "]";
    }
}
