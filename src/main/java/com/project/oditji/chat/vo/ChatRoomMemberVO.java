package com.project.oditji.chat.vo;

import java.time.LocalDateTime;

public class ChatRoomMemberVO {

    private String roomId;
    private int businessNo;
    private LocalDateTime joinDate;
    private String description;
    private String isDefault;

    public ChatRoomMemberVO() {

    }

    public ChatRoomMemberVO(String roomId,
            int businessNo,
            LocalDateTime joinDate,
            String description,
            String isDefault) {

        this.roomId = roomId;
        this.businessNo = businessNo;
        this.joinDate = joinDate;
        this.description = description;
        this.isDefault = isDefault;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getBusinessNo() {
        return businessNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBusinessNo(int businessNo) {
        this.businessNo = businessNo;
    }

    public LocalDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }

    @Override
    public String toString() {
        return "ChatRoomMemberVO [roomId=" + roomId
                + ", businessNo=" + businessNo
                + ", joinDate=" + joinDate + "]";
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }

}