package com.project.oditji.admin.vo;

import com.project.oditji.common.vo.BusinessBaseVO;

/**
 * 사업자 관리 VO (테이블: BUSINESS + MEMBER 조인, GRADE_POLICY 참조)
 */
public class BusinessManageVO extends BusinessBaseVO {

    private String memberId;
    private String email;

    /**
     * 결제 완료 후 취소되지 않은 주문상품을 기준으로 계산한 누적 실매출입니다.
     */
    private Long totalSales;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(Long totalSales) {
        this.totalSales = totalSales;
    }
}
