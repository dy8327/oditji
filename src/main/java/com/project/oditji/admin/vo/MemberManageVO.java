package com.project.oditji.admin.vo;

import com.project.oditji.member.vo.MemberVO;

/**
 * 관리자 회원 관리 화면의 조회 전용 VO입니다.
 *
 * 회원 기본 정보는 MemberVO에서 상속하고,
 * 관리자 화면에서 계산하거나 조인하는 값만 추가합니다.
 */
public class MemberManageVO extends MemberVO {

    private String snsYn;
    private Integer remainingDeleteDays;

    public String getSnsYn() {
        return snsYn;
    }

    public void setSnsYn(String snsYn) {
        this.snsYn = snsYn;
    }

    public Integer getRemainingDeleteDays() {
        return remainingDeleteDays;
    }

    public void setRemainingDeleteDays(Integer remainingDeleteDays) {
        this.remainingDeleteDays = remainingDeleteDays;
    }
}
