package com.project.oditji.member.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 네이버 회원 프로필 조회 응답 VO입니다.
 *
 * 네이버 응답은 resultcode, message, response 구조이며,
 * 실제 회원 정보는 response 내부에 포함됩니다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverUserInfoVO {

    private String resultcode;
    private String message;
    private Response response;

    /**
     * 네이버 프로필 응답의 실제 회원 정보입니다.
     *
     * 사용자가 정보 제공에 동의하지 않은 선택 항목은
     * null로 전달될 수 있으므로 모든 필드를 선택적으로 처리합니다.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {

        private String id;
        private String nickname;
        private String name;
        private String email;
        private String mobile;

        @JsonProperty("profile_image")
        private String profileImage;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getProfileImage() {
            return profileImage;
        }

        public void setProfileImage(String profileImage) {
            this.profileImage = profileImage;
        }
    }

    public String getResultcode() {
        return resultcode;
    }

    public void setResultcode(String resultcode) {
        this.resultcode = resultcode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    /**
     * MEMBER_SOCIAL.PROVIDER_USER_ID에 저장할 네이버 고유 식별자를 반환합니다.
     */
    public String getProviderUserId() {
        return response == null ? null : response.getId();
    }

    /**
     * 화면과 세션에서 사용할 이름을 반환합니다.
     * 이름이 없으면 닉네임, 둘 다 없으면 기본 문구를 사용합니다.
     */
    public String getDisplayName() {
        String name = getName();

        if (name != null && !name.isBlank()) {
            return name;
        }

        String nickname = getNickname();

        if (nickname != null && !nickname.isBlank()) {
            return nickname;
        }

        return "네이버회원";
    }

    /**
     * 회원 닉네임으로 사용할 값을 반환합니다.
     */
    public String getSafeNickname() {
        String nickname = getNickname();

        if (nickname != null && !nickname.isBlank()) {
            return nickname;
        }

        String name = getName();

        if (name != null && !name.isBlank()) {
            return name;
        }

        return "네이버회원";
    }

    public String getName() {
        return response == null ? null : response.getName();
    }

    public String getNickname() {
        return response == null ? null : response.getNickname();
    }

    public String getEmail() {
        return response == null ? null : response.getEmail();
    }

    public String getMobile() {
        return response == null ? null : response.getMobile();
    }

    public String getProfileImage() {
        return response == null ? null : response.getProfileImage();
    }
}
