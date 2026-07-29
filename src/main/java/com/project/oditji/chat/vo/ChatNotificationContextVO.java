package com.project.oditji.chat.vo;

import java.util.List;

/**
 * 공통 헤더 알림 스크립트에 전달하는 현재 사용자의 채팅 알림 컨텍스트입니다.
 */
public class ChatNotificationContextVO {

    private boolean enabled;
    private Long memberNo;
    private Integer businessNo;
    private String role;
    private List<ChatNotificationRoomVO> roomList;

    public ChatNotificationContextVO() {
        this.roomList = List.of();
    }

    public ChatNotificationContextVO(
            boolean enabled,
            Long memberNo,
            Integer businessNo,
            String role,
            List<ChatNotificationRoomVO> roomList) {

        this.enabled = enabled;
        this.memberNo = memberNo;
        this.businessNo = businessNo;
        this.role = role;
        this.roomList = roomList == null ? List.of() : roomList;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public Integer getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(Integer businessNo) {
        this.businessNo = businessNo;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<ChatNotificationRoomVO> getRoomList() {
        return roomList;
    }

    public void setRoomList(List<ChatNotificationRoomVO> roomList) {
        this.roomList = roomList == null ? List.of() : roomList;
    }
}
