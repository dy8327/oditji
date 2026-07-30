package com.project.oditji.refund.service;

import java.time.LocalDate;
import java.util.List;

import com.project.oditji.refund.vo.OrderCancelRefundVO;

public interface OrderCancelRefundService {

    /* 주문 전체 취소 요청 */
    /* [추가] 사용자 취소/환불 내역 조건 조회 */
    List<OrderCancelRefundVO> getMemberCancelRefundHistory(
            Long memberNo, String historyType, String status, LocalDate startDate, LocalDate endDate);

    void requestOrderCancel(Long memberNo, Long orderNo, String reason);

    /* [추가] 선택한 여러 주문상품을 하나의 트랜잭션으로 요청 */
    void requestOrderItemsCancel(Long memberNo, List<Long> orderItemNos, String reason);

    /* [부분 취소 기능 추가] 주문상품 한 건 취소 요청 */
    void requestOrderItemCancel(Long memberNo, Long orderItemNo, String reason);

    List<OrderCancelRefundVO> getBusinessCancelList(Long memberNo, String status);

    void approveCancel(Long memberNo, Long cancelNo);

    void rejectCancel(Long memberNo, Long cancelNo, String rejectReason);
}