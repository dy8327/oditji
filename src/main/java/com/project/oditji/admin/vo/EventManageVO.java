package com.project.oditji.admin.vo;

import java.util.Date;

/**
 * 이벤트 관리 VO (테이블: EVENT)
 *
 * 주의: EVENT 테이블에는 사업자 연결(BUSINESS_NO), 요청 내용(DESCRIPTION),
 * 요청 유형(등록/수정/연장 구분) 컬럼이 존재하지 않는다.
 * eventManage.jsp 화면 호환을 위해 필드는 유지하되, businessName / description 은
 * 항상 null로 반환되며 tab(register/update/extend) 구분 없이 동일 목록을 조회한다.
 * (추후 EVENT 테이블에 BUSINESS_NO, REQUEST_TYPE, DESCRIPTION 컬럼 추가 필요)
 */
public class EventManageVO {

    private Long requestNo;      // = EVENT.EVENT_NO
    private String businessName; // DB 미지원 필드 (항상 null)
    private String eventTitle;   // = EVENT.TITLE
    private Date startDate;
    private Date endDate;
    private Date requestedAt;    // = EVENT.CREATED_AT
    private String status;       // EVENT.STATUS
    private String description;  // DB 미지원 필드 (항상 null)

    public Long getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(Long requestNo) {
        this.requestNo = requestNo;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Date requestedAt) {
        this.requestedAt = requestedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
