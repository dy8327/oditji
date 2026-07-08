package com.project.oditji.chat.vo;

import java.util.Date;

public class ChatRoomMemberVO {

    private String roomId;
    private int businessNo;
    private Date joinDate;
    private String description;
    private String isDefault;

    public ChatRoomMemberVO() {

    }

    public ChatRoomMemberVO(String roomId,
            int businessNo,
            Date joinDate,
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

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
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