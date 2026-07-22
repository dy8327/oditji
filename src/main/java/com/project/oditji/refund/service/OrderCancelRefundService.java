package com.project.oditji.refund.service;

import java.util.List;

import com.project.oditji.refund.vo.OrderCancelRefundVO;

public interface OrderCancelRefundService {

    void requestOrderCancel(Long memberNo, Long orderNo, String reason);

    List<OrderCancelRefundVO> getBusinessCancelList(
            Long memberNo,
            String status);

    void approveCancel(Long memberNo, Long cancelNo);

    void rejectCancel(Long memberNo, Long cancelNo, String rejectReason);
}
