package com.project.oditji.member.vo;

import java.time.LocalDateTime;

/**
 * 소셜 로그인 회원 조회 결과입니다.
 *
 * MEMBER 공통 속성은 MemberVO에서 상속하고,
 * MEMBER_SOCIAL 조인 결과만 이 클래스에서 추가로 보관합니다.
 */
public class MemberSocialJoinVO extends MemberVO {

    private int socialNo;
    private String provider;
    private String providerUserId;
    private LocalDateTime socialCreatedAt;

    public int getSocialNo() {
        return socialNo;
    }

    public void setSocialNo(int socialNo) {
        this.socialNo = socialNo;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    public LocalDateTime getSocialCreatedAt() {
        return socialCreatedAt;
    }

    public void setSocialCreatedAt(LocalDateTime socialCreatedAt) {
        this.socialCreatedAt = socialCreatedAt;
    }
}
