package com.project.oditji.member.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오 사용자 정보 조회 응답 VO
 *
 * 카카오 API 응답에는 프로젝트에서 사용하지 않는 필드도 포함되므로
 * 알 수 없는 JSON 필드는 무시하도록 설정한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserInfoVO {

    private Long id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    public KakaoUserInfoVO() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {

        private Profile profile;

        public KakaoAccount() {
        }

        public Profile getProfile() {
            return profile;
        }

        public void setProfile(Profile profile) {
            this.profile = profile;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {

        private String nickname;

        @JsonProperty("profile_image_url")
        private String profileImageUrl;

        @JsonProperty("thumbnail_image_url")
        private String thumbnailImageUrl;

        public Profile() {
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getProfileImageUrl() {
            return profileImageUrl;
        }

        public void setProfileImageUrl(String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
        }

        public String getThumbnailImageUrl() {
            return thumbnailImageUrl;
        }

        public void setThumbnailImageUrl(String thumbnailImageUrl) {
            this.thumbnailImageUrl = thumbnailImageUrl;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public KakaoAccount getKakaoAccount() {
        return kakaoAccount;
    }

    public void setKakaoAccount(KakaoAccount kakaoAccount) {
        this.kakaoAccount = kakaoAccount;
    }

    /**
     * 카카오 닉네임 반환
     *
     * 카카오 계정이나 프로필 또는 닉네임이 없으면
     * 기본 닉네임을 반환한다.
     */
    public String getNickname() {
        Profile profile = getProfile();

        if (profile == null) {
            return "카카오회원";
        }

        String nickname = profile.getNickname();

        if (nickname == null || nickname.isBlank()) {
            return "카카오회원";
        }

        return nickname;
    }

    /**
     * 카카오 프로필 원본 이미지 URL 반환
     */
    public String getProfileImageUrl() {
        Profile profile = getProfile();

        if (profile == null) {
            return null;
        }

        return profile.getProfileImageUrl();
    }

    /**
     * 카카오 프로필 썸네일 이미지 URL 반환
     */
    public String getThumbnailImageUrl() {
        Profile profile = getProfile();

        if (profile == null) {
            return null;
        }

        return profile.getThumbnailImageUrl();
    }

    /**
     * 중첩된 카카오 프로필 객체를 안전하게 반환한다.
     */
    private Profile getProfile() {
        if (kakaoAccount == null) {
            return null;
        }

        return kakaoAccount.getProfile();
    }
}