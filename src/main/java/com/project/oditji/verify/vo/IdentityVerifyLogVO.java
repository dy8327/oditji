package com.project.oditji.verify.vo;

public class IdentityVerifyLogVO {

    private int verifyNo;
    private Long memberNo;
    private String verifyId;
    private String verifyStatus;

    private String name;
    private String birthDate;
    private String phoneNumber;
    private String gender;
    private String adultYn;

    private String rawStatus;

    public IdentityVerifyLogVO() {
    }

    public int getVerifyNo() { return verifyNo; }
    public void setVerifyNo(int verifyNo) { this.verifyNo = verifyNo; }

    public Long getMemberNo() { return memberNo; }
    public void setMemberNo(Long memberNo) { this.memberNo = memberNo; }

    public String getVerifyId() { return verifyId; }
    public void setVerifyId(String verifyId) { this.verifyId = verifyId; }

    public String getVerifyStatus() { return verifyStatus; }
    public void setVerifyStatus(String verifyStatus) { this.verifyStatus = verifyStatus; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAdultYn() { return adultYn; }
    public void setAdultYn(String adultYn) { this.adultYn = adultYn; }

    public String getRawStatus() { return rawStatus; }
    public void setRawStatus(String rawStatus) { this.rawStatus = rawStatus; }
}