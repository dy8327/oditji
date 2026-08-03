package com.project.oditji.admin.vo;

/**
 * 상품 관리 화면 상단 통계 카드용 VO입니다.
 *
 * 전체·승인 대기·승인 완료 건수는 ApprovalStatVO에서 공통으로 제공하고,
 * 상품 관리에만 필요한 삭제 요청 건수만 추가로 보관합니다.
 */
public class ProductStatVO extends ApprovalStatVO {

    private long deleteRequestedCount;

    public long getDeleteRequestedCount() {
        return deleteRequestedCount;
    }

    public void setDeleteRequestedCount(long deleteRequestedCount) {
        this.deleteRequestedCount = deleteRequestedCount;
    }
}
