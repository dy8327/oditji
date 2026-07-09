package com.project.oditji.verify.vo;

public class AdultVerifyReadyVO {

    private String storeId;
    private String easyChannelKey;
    private String smsChannelKey;
    private String verifyId;

    public AdultVerifyReadyVO() {
    }

    public AdultVerifyReadyVO(String storeId, String easyChannelKey, String smsChannelKey, String verifyId) {
        this.storeId = storeId;
        this.easyChannelKey = easyChannelKey;
        this.smsChannelKey = smsChannelKey;
        this.verifyId = verifyId;
    }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getEasyChannelKey() { return easyChannelKey; }
    public void setEasyChannelKey(String easyChannelKey) { this.easyChannelKey = easyChannelKey; }

    public String getSmsChannelKey() { return smsChannelKey; }
    public void setSmsChannelKey(String smsChannelKey) { this.smsChannelKey = smsChannelKey; }

    public String getVerifyId() { return verifyId; }
    public void setVerifyId(String verifyId) { this.verifyId = verifyId; }
}