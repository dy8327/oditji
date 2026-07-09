package com.project.oditji.verify.vo;

public class AdultVerifyCompleteVO {

    private String verifyId;
    private boolean success;
    private boolean adult;
    private String message;
    private String redirectUrl;

    public AdultVerifyCompleteVO() {
    }

    public AdultVerifyCompleteVO(boolean success, boolean adult, String message, String redirectUrl) {
        this.success = success;
        this.adult = adult;
        this.message = message;
        this.redirectUrl = redirectUrl;
    }

    public static AdultVerifyCompleteVO success(String redirectUrl) {
        return new AdultVerifyCompleteVO(true, true, "성인인증이 완료되었습니다.", redirectUrl);
    }

    public static AdultVerifyCompleteVO fail(String message) {
        return new AdultVerifyCompleteVO(false, false, message, null);
    }

    public String getVerifyId() { return verifyId; }
    public void setVerifyId(String verifyId) { this.verifyId = verifyId; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public boolean isAdult() { return adult; }
    public void setAdult(boolean adult) { this.adult = adult; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRedirectUrl() { return redirectUrl; }
    public void setRedirectUrl(String redirectUrl) { this.redirectUrl = redirectUrl; }
}