package com.project.oditji.chat.vo;

/**
 * 기존 ODITJI 세션 사용자를 Firebase Authentication에 연결하기 위한
 * Custom Token 응답 객체입니다.
 */
public class FirebaseChatTokenVO {

    private boolean success;
    private boolean enabled;
    private String token;
    private String uid;
    private String message;

    public FirebaseChatTokenVO() {
    }

    public FirebaseChatTokenVO(
            boolean success,
            boolean enabled,
            String token,
            String uid,
            String message) {

        this.success = success;
        this.enabled = enabled;
        this.token = token;
        this.uid = uid;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
