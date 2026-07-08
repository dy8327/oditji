package com.project.oditji.verify.vo;

public class IdentityVerifyResultVO {

    private boolean success;
    private String message;

    private String name;
    private String birthDate;
    private String phoneNumber;
    private String gender;
    private String adultYn;

    public static IdentityVerifyResultVO success(
            String message,
            String name,
            String birthDate,
            String phoneNumber,
            String gender,
            String adultYn
    ) {
        IdentityVerifyResultVO vo = new IdentityVerifyResultVO();
        vo.setSuccess(true);
        vo.setMessage(message);
        vo.setName(name);
        vo.setBirthDate(birthDate);
        vo.setPhoneNumber(phoneNumber);
        vo.setGender(gender);
        vo.setAdultYn(adultYn);
        return vo;
    }

    public static IdentityVerifyResultVO fail(String message) {
        IdentityVerifyResultVO vo = new IdentityVerifyResultVO();
        vo.setSuccess(false);
        vo.setMessage(message);
        vo.setAdultYn("N");
        return vo;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    
    public String getAdultYn() {
        return adultYn;
    }

    public void setAdultYn(String adultYn) {
        this.adultYn = adultYn;
    }
}