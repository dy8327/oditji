package com.project.oditji.admin.vo;

import java.time.LocalDateTime;

/**
 * 모니터링 VO
 * MEMBER + ACCESS_LOG(최근 접속일/IP) + PRODUCT_CLICK_LOG(상품 클릭수) 집계
 *
 * - memberId: MEMBER.MEMBER_ID (로그인 아이디). SNS 로그인 회원은 provider가
 *   발급한 원본 식별자를 그대로 저장해 값이 길 수 있어, 화면(monitoring.jsp)에서는
 *   memberManage.jsp와 동일한 .member-id-text 컴포넌트로 잘리지 않게 보여준다.
 * - productClickCount: PRODUCT_CLICK_LOG 에서 회원별 전체 클릭 로그 카운트
 */
public class MonitoringVO {

    private Long memberNo;
    private String memberId;
    private String nickname;
    private LocalDateTime lastAccessAt;
    private Long productClickCount;
    private String accessIp;

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public LocalDateTime getLastAccessAt() {
        return lastAccessAt;
    }

    public void setLastAccessAt(LocalDateTime lastAccessAt) {
        this.lastAccessAt = lastAccessAt;
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
