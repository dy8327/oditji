package com.project.oditji.common.vo;

import java.util.Date;

/**
 * 접속 로그 VO (테이블: ACCESS_LOG)
 * 요청마다 AccessLogInterceptor 가 이 VO를 채워 INSERT 한다.
 */
public class AccessLogVO {

    private Long accessLogNo;
    private Long memberNo;      // 비로그인 요청은 NULL
    private String accessIp;
    private String userAgent;
    private String accessUrl;
    private Date createdAt;

    public Long getAccessLogNo() {
        return accessLogNo;
    }

    public void setAccessLogNo(Long accessLogNo) {
        this.accessLogNo = accessLogNo;
    }

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public String getAccessIp() {
        return accessIp;
    }

    public void setAccessIp(String accessIp) {
        this.accessIp = accessIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
