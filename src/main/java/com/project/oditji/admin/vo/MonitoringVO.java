package com.project.oditji.admin.vo;

import java.util.Date;

/**
 * 모니터링 VO
 * MEMBER + ACCESS_LOG(최근 접속일/IP) + PRODUCT_CLICK_LOG(콘텐츠 이용수/상품 클릭수) 집계
 *
 * - contentUseCount: PRODUCT_CLICK_LOG 에서 회원별 CONTENT_NO distinct 카운트
 * - productClickCount: PRODUCT_CLICK_LOG 에서 회원별 전체 클릭 로그 카운트
 */
public class MonitoringVO {

    private Long memberNo;
    private String nickname;
    private Date lastAccessAt;
    private Long contentUseCount;
    private Long productClickCount;
    private String accessIp;

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Date getLastAccessAt() {
        return lastAccessAt;
    }

    public void setLastAccessAt(Date lastAccessAt) {
        this.lastAccessAt = lastAccessAt;
    }

    public Long getContentUseCount() {
        return contentUseCount;
    }

    public void setContentUseCount(Long contentUseCount) {
        this.contentUseCount = contentUseCount;
    }

    public Long getProductClickCount() {
        return productClickCount;
    }

    public void setProductClickCount(Long productClickCount) {
        this.productClickCount = productClickCount;
    }

    public String getAccessIp() {
        return accessIp;
    }

    public void setAccessIp(String accessIp) {
        this.accessIp = accessIp;
    }
}
