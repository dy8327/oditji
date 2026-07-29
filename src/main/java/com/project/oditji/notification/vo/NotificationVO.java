package com.project.oditji.notification.vo;

import java.util.Date;

/**
 * 일반 회원, 사업자, 관리자가 공통으로 사용하는 알림 정보를 전달합니다.
 */
public class NotificationVO {

    private Long notificationNo;
    private Long receiverMemberNo;
    private String notificationType;
    private String title;
    private String message;
    private String linkUrl;
    private String referenceType;
    private Long referenceNo;
    private String isRead;
    private Date createdAt;
    private Date readAt;

    public Long getNotificationNo() {
        return notificationNo;
    }

    public void setNotificationNo(Long notificationNo) {
        this.notificationNo = notificationNo;
    }

    public Long getReceiverMemberNo() {
        return receiverMemberNo;
    }

    public void setReceiverMemberNo(Long receiverMemberNo) {
        this.receiverMemberNo = receiverMemberNo;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(Long referenceNo) {
        this.referenceNo = referenceNo;
    }

    public String getIsRead() {
        return isRead;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getReadAt() {
        return readAt;
    }

    public void setReadAt(Date readAt) {
        this.readAt = readAt;
    }

    /**
     * 브라우저에서 채팅 알림과 업무 알림을 최신순으로 정렬할 때 사용합니다.
     */
    public long getCreatedAtEpochMs() {
        return createdAt == null ? 0L : createdAt.getTime();
    }

    /**
     * 알림 상세 목록에 표시할 간단한 분류명을 반환합니다.
     */
    public String getTypeLabel() {

        if (notificationType == null) {
            return "업무";
        }

        if (notificationType.contains("REPORT")) {
            return "신고";
        }

        if (notificationType.contains("DELIVERY")) {
            return "배송";
        }

        if (notificationType.contains("ORDER")) {
            return "주문";
        }

        if (notificationType.contains("CANCEL")) {
            return "취소";
        }

        if (notificationType.contains("SETTLEMENT")) {
            return "정산";
        }

        if (notificationType.contains("EVENT")) {
            return "이벤트";
        }

        if (notificationType.contains("PRODUCT")) {
            return "상품";
        }

        if (notificationType.contains("BUSINESS")) {
            return "사업자";
        }

        return "업무";
    }
}
