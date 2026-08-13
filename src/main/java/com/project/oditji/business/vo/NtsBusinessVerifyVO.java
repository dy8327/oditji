package com.project.oditji.business.vo;

public class NtsBusinessVerifyVO {

    // 진위확인 성공 여부
    private boolean valid;

    // 국세청 사업 상태
    // 예: 계속사업자 / 휴업자 / 폐업자
    private String businessStatus;

    // 사용자에게 보여줄 메시지
    private String message;

    public NtsBusinessVerifyVO() {
    }

    public NtsBusinessVerifyVO(
            boolean valid,
            String businessStatus,
            String message) {

        this.valid = valid;
        this.businessStatus = businessStatus;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getBusinessStatus() {
        return businessStatus;
    }

    public void setBusinessStatus(String businessStatus) {
        this.businessStatus = businessStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}