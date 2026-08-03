package com.project.oditji.chat.vo;

import java.util.Date;

/**
 * 메시지별 미열람 참여자 수 계산에 사용하는 참여자 읽음 정보입니다.
 */
public class ChatParticipantReadVO {

    private Long memberNo;
    private int businessNo;
    private String displayName;
    private Date joinedAt;
    private String lastReadMessageId;
    private Long lastReadEpochMs;

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public int getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(int businessNo) {
        this.businessNo = businessNo;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Date getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Date joinedAt) {
        this.joinedAt = joinedAt;
    }

    /**
     * JavaScript에서 Firestore Timestamp와 바로 비교할 수 있도록
     * 채팅 참여 시작 시각을 epoch millisecond로 반환합니다.
     */
    public long getJoinedAtEpochMs() {
        return joinedAt == null ? 0L : joinedAt.getTime();
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
