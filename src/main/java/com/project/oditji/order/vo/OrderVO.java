package com.project.oditji.order.vo;

import java.util.ArrayList;
import java.util.List;

import com.project.oditji.common.vo.OrderSummaryBaseVO;

/**
 * 사용자 주문 목록과 주문 상세 화면에 사용하는 주문 정보입니다.
 */
public class OrderVO extends OrderSummaryBaseVO {

    private static final long serialVersionUID = 1L;

    private Long memberNo;
    private String payMethod;
    private String pgProvider;
    private String fullCancelStatus;
    private String fullCancelRejectReason;
    private List<OrderItemVO> items = new ArrayList<OrderItemVO>();

    public Long getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(Long memberNo) {
        this.memberNo = memberNo;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getPgProvider() {
        return pgProvider;
    }

    public void setPgProvider(String pgProvider) {
        this.pgProvider = pgProvider;
    }

    public String getFullCancelStatus() {
        return fullCancelStatus;
    }

    public void setFullCancelStatus(String fullCancelStatus) {
        this.fullCancelStatus = fullCancelStatus;
    }

    public String getFullCancelRejectReason() {
        return fullCancelRejectReason;
    }

    public void setFullCancelRejectReason(String fullCancelRejectReason) {
        this.fullCancelRejectReason = fullCancelRejectReason;
    }

    public List<OrderItemVO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemVO> items) {
        this.items = items == null
                ? new ArrayList<OrderItemVO>()
                : items;
    }

    public boolean isAllCancelEligible() {
        return !items.isEmpty()
                && items.stream().allMatch(OrderItemVO::isCancelEligible);
    }

    public boolean isAllRefundEligible() {
        return !items.isEmpty()
                && items.stream().allMatch(OrderItemVO::isRefundEligible);
    }

    public boolean isAnyCancelEligible() {
        return items.stream().anyMatch(OrderItemVO::isCancelEligible);
    }

    public boolean isAnyRefundEligible() {
        return items.stream().anyMatch(OrderItemVO::isRefundEligible);
    }
}
