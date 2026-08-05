package com.project.oditji.business.vo;

import java.time.LocalDateTime;

import com.project.oditji.common.vo.DeliveryBaseVO;

/**
 * 사업자 배송 관리 화면과 처리에서 사용하는 VO입니다.
 *
 * 공통 배송·주문상품 필드는 DeliveryBaseVO에서 상속받고,
 * 사업자 관리 화면에만 필요한 회원·사업자·수정일 정보를 보관합니다.
 */
public class DeliveryManageVO extends DeliveryBaseVO {

    private Long memberNo;
    private Long businessNo;
    private LocalDateTime updatedAt;

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public Long getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(Long businessNo) {
        this.businessNo = businessNo;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
