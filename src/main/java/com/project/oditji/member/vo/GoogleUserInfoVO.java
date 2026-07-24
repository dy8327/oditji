package com.project.oditji.member.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Google OpenID Connect UserInfo 응답 VO입니다.
 *
 * ODITJI에서는 다음 정보만 사용합니다.
 * - sub: MEMBER_SOCIAL에 저장할 Google 고유 사용자 식별자
 * - name: ODITJI의 닉네임으로 사용할 Google 표시 이름
 * - picture: ODITJI의 프로필 이미지로 사용할 URL
 *
 * 이메일은 요청하지도 않고 이 VO에도 매핑하지 않습니다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleUserInfoVO {

    private String sub;
    private String name;
    private String picture;

    public GoogleUserInfoVO() {
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    /**
     * Google에는 별도의 nickname 표준 클레임이 없으므로
     * 표시 가능한 이름인 name을 ODITJI 닉네임 원본값으로 사용합니다.
     */
    public String getNickname() {

        if (name == null || name.isBlank()) {
            return "구글회원";
        }

        return name.trim();
    }

    public String getProfileImageUrl() {

        if (picture == null || picture.isBlank()) {
            return null;
        }

        return picture.trim();
    }
}
