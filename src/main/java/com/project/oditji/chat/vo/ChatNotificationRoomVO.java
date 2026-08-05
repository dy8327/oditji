package com.project.oditji.chat.vo;

import java.time.LocalDateTime;

import com.project.oditji.common.util.DateTimeUtil;

/**
 * 공통 헤더의 채팅 알림 계산에 필요한 채팅방 정보입니다.
 *
 * 헤더 배지는 미읽은 메시지 총개수가 아니라
 * 미읽은 메시지가 존재하는 채팅방 개수로 계산합니다.
 */
public class ChatNotificationRoomVO {

    private String roomId;
    private String roomName;
    private String roomType;
    private LocalDateTime accessStartAt;
    private String lastReadMessageId;
    private Long lastReadEpochMs;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public LocalDateTime getAccessStartAt() {
        return accessStartAt;
    }

    public void setAccessStartAt(LocalDateTime accessStartAt) {
        this.accessStartAt = accessStartAt;
    }

    /**
     * JavaScript에서 Firestore Timestamp와 바로 비교할 수 있도록
     * 채팅방 접근 시작 시각을 epoch millisecond로 반환합니다.
     */
    public long getAccessStartEpochMs() {
        return DateTimeUtil.toEpochMilli(accessStartAt);
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
}
