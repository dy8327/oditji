package com.project.oditji.chat.vo;

import java.time.LocalDateTime;

public class ChatRoomVO {

    /* ==========================
     * CHAT_ROOM 테이블 컬럼
     * ========================== */

    private String roomId;
    private String roomName;
    private String roomType;
    private String roomDescription;
    private int createdBy;
    private LocalDateTime createdAt;
    private String status;

    /* ==========================
     * 조회용 컬럼
     * ========================== */

    // 채팅방 생성자 이름
    private String creatorName;

    // 현재 참여 인원
    private int memberCount;

    // 최근 메시지
    private String lastMessage;

    // 최근 메시지 시간
    private LocalDateTime lastMessageTime;

    //최대 참여 인원 수
    private int maxMember;

    private String isDefault;
    

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

    public String getRoomDescription() {
        return roomDescription;
    }

    public void setRoomDescription(String roomDescription) {
        this.roomDescription = roomDescription;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getMaxMember() {
        return maxMember;
    }

    public void setMaxMember(int maxMember) {
        this.maxMember = maxMember;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public String toString() {
        return "ChatRoomVO [roomId=" + roomId
                + ", roomName=" + roomName
                + ", roomType=" + roomType
                + ", createdBy=" + createdBy
                + ", createdAt=" + createdAt
                + ", status=" + status
                + ", creatorName=" + creatorName
                + ", memberCount=" + memberCount
                + ", lastMessage=" + lastMessage
                + ", lastMessageTime=" + lastMessageTime
                + ", roomDescription=" + roomDescription
                + "]";
    }

}