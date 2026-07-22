package com.project.oditji.refund.service;

import java.util.List;

import com.project.oditji.refund.vo.OrderCancelRefundVO;

public interface OrderCancelRefundService {

    /* 주문 전체 취소 요청 */
    void requestOrderCancel(Long memberNo, Long orderNo, String reason);

    /* [부분 취소 기능 추가] 주문상품 한 건 취소 요청 */
    void requestOrderItemCancel(Long memberNo, Long orderItemNo, String reason);

    List<OrderCancelRefundVO> getBusinessCancelList(Long memberNo, String status);

    void approveCancel(Long memberNo, Long cancelNo);

    void rejectCancel(Long memberNo, Long cancelNo, String rejectReason);
}
