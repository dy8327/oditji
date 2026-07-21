package com.project.oditji.refund.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.refund.dao.OrderCancelRefundDAO;
import com.project.oditji.refund.vo.OrderCancelRefundVO;
import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.payment.dao.PaymentDAO;
import com.project.oditji.payment.service.PaymentService;
import com.project.oditji.payment.vo.PaymentVO;

@Service
public class OrderCancelRefundServiceImpl implements OrderCancelRefundService {

    private static final String WAITING = "WAITING";

    private final OrderCancelRefundDAO orderCancelRefundDAO;
    private final BusinessDAO businessDAO;
    private final PaymentDAO paymentDAO;
    private final PaymentService paymentService;

    public OrderCancelRefundServiceImpl(
            OrderCancelRefundDAO orderCancelRefundDAO,
            BusinessDAO businessDAO,
            PaymentDAO paymentDAO,
            PaymentService paymentService) {

        this.orderCancelRefundDAO = orderCancelRefundDAO;
        this.businessDAO = businessDAO;
        this.paymentDAO = paymentDAO;
        this.paymentService = paymentService;
    }

    /*
     * =========================================================
     * [주문 취소 요청 기능 추가]
     *
     * 사용자 주문 한 건에 포함된 취소 가능 주문상품별로
     * CANCEL_REQUEST를 생성한다.
     * 이 단계에서는 포트원 환불을 호출하지 않는다.
     * =========================================================
     */
    @Override
    @Transactional
    public void requestOrderCancel(
            Long memberNo,
            Long orderNo,
            String reason) {

        if (memberNo == null || memberNo <= 0) {
            throw new IllegalArgumentException("로그인 회원 정보가 올바르지 않습니다.");
        }

        if (orderNo == null || orderNo <= 0) {
            throw new IllegalArgumentException("주문 번호가 올바르지 않습니다.");
        }

        String normalizedReason = normalizeReason(reason, "주문 취소 요청");

        List<OrderItemVO> itemList = orderCancelRefundDAO.selectCancelableItemsByOrder(
                memberNo,
                orderNo);

        if (itemList == null || itemList.isEmpty()) {
            throw new IllegalArgumentException(
                    "취소 요청할 수 있는 주문상품이 없습니다. 배송이 시작된 상품은 취소할 수 없습니다.");
        }

        int insertedCount = 0;

        for (OrderItemVO item : itemList) {

            if (orderCancelRefundDAO.countWaitingCancelByOrderItemNo(item.getOrderItemNo()) > 0) {
                continue;
            }

            OrderCancelRefundVO cancelRequestVO = new OrderCancelRefundVO();
            cancelRequestVO.setOrderItemNo(item.getOrderItemNo());
            cancelRequestVO.setMemberNo(memberNo);
            cancelRequestVO.setReason(normalizedReason);
            cancelRequestVO.setStatus(WAITING);

            if (orderCancelRefundDAO.insertCancelRequest(cancelRequestVO) != 1) {
                throw new IllegalStateException("취소 요청 저장에 실패했습니다.");
            }

            if (orderCancelRefundDAO.updateOrderItemCancelRequested(item.getOrderItemNo()) != 1) {
                throw new IllegalStateException("주문상품 상태 변경에 실패했습니다.");
            }

            insertedCount++;
        }

        if (insertedCount == 0) {
            throw new IllegalArgumentException("이미 취소 요청이 접수된 주문입니다.");
        }

        orderCancelRefundDAO.updateOrderStatusByItems(orderNo);
    }

    @Override
    public List<OrderCancelRefundVO> getBusinessCancelList(
            Long memberNo,
            String status) {

        BusinessVO business = getBusiness(memberNo);

        String normalizedStatus = status == null || status.isBlank()
                || "ALL".equalsIgnoreCase(status)
                        ? null
                        : status.trim().toUpperCase();

        return orderCancelRefundDAO.selectCancelListByBusiness(
                business.getBusinessNo(),
                normalizedStatus);
    }

    /*
     * =========================================================
     * [사업자 취소 승인 기능 추가]
     *
     * 사업자 소유 주문상품인지 검증한 뒤 포트원 부분 취소를
     * 먼저 실행한다. 환불 성공 후 주문상품, 재고, 결제,
     * 취소 요청 상태를 함께 변경한다.
     * =========================================================
     */
    @Override
    @Transactional
    public void approveCancel(
            Long memberNo,
            Long cancelNo) {

        BusinessVO business = getBusiness(memberNo);
        OrderCancelRefundVO request = getWaitingRequest(cancelNo, business.getBusinessNo());

        PaymentVO payment = paymentDAO.selectPaymentByOrderNo(request.getOrderNo());

        if (payment == null) {
            throw new IllegalArgumentException("주문 결제내역을 찾을 수 없습니다.");
        }

        long cancelAmount = request.getCancelAmount() == null
                ? 0L
                : request.getCancelAmount();

        if (cancelAmount <= 0) {
            throw new IllegalArgumentException("환불 금액이 올바르지 않습니다.");
        }

        PaymentVO canceledPayment = paymentService.cancelPaidPaymentPartially(
                payment,
                cancelAmount,
                request.getReason());

        if (orderCancelRefundDAO.cancelOrderItem(request.getOrderItemNo()) != 1) {
            throw new IllegalStateException("주문상품 취소 상태 변경에 실패했습니다.");
        }

        if (orderCancelRefundDAO.restoreProductStock(
                request.getProductNo(),
                request.getQuantity()) != 1) {
            throw new IllegalStateException("상품 재고 복구에 실패했습니다.");
        }

        if (orderCancelRefundDAO.approveCancelRequest(cancelNo) != 1) {
            throw new IllegalStateException("취소 요청 승인 처리에 실패했습니다.");
        }

        if (paymentDAO.updatePaymentPartialCanceled(canceledPayment) != 1) {
            throw new IllegalStateException("결제 취소 정보 저장에 실패했습니다.");
        }

        orderCancelRefundDAO.updateOrderStatusByItems(request.getOrderNo());
    }

    @Override
    @Transactional
    public void rejectCancel(
            Long memberNo,
            Long cancelNo,
            String rejectReason) {

        BusinessVO business = getBusiness(memberNo);
        OrderCancelRefundVO request = getWaitingRequest(cancelNo, business.getBusinessNo());
        String normalizedReason = normalizeReason(rejectReason, "사업자 사유로 취소 요청 반려");

        if (orderCancelRefundDAO.rejectCancelRequest(cancelNo, normalizedReason) != 1) {
            throw new IllegalStateException("취소 요청 반려 처리에 실패했습니다.");
        }

        if (orderCancelRefundDAO.restoreOrderItemStatus(request.getOrderItemNo()) != 1) {
            throw new IllegalStateException("주문상품 상태 복구에 실패했습니다.");
        }

        orderCancelRefundDAO.updateOrderStatusByItems(request.getOrderNo());
    }

    private BusinessVO getBusiness(Long memberNo) {

        if (memberNo == null || memberNo <= 0) {
            throw new IllegalArgumentException("로그인 회원 정보를 확인할 수 없습니다.");
        }

        BusinessVO business = businessDAO.selectBusinessByMemberNo(memberNo);

        if (business == null) {
            throw new IllegalArgumentException("로그인 회원과 연결된 사업자 정보가 없습니다.");
        }

        return business;
    }

    private OrderCancelRefundVO getWaitingRequest(
            Long cancelNo,
            Long businessNo) {

        if (cancelNo == null || cancelNo <= 0) {
            throw new IllegalArgumentException("취소 요청 번호가 올바르지 않습니다.");
        }

        OrderCancelRefundVO request = orderCancelRefundDAO.selectCancelRequestForBusiness(
                cancelNo,
                businessNo);

        if (request == null) {
            throw new IllegalArgumentException("취소 요청을 찾을 수 없거나 처리 권한이 없습니다.");
        }

        if (!WAITING.equals(request.getStatus())) {
            throw new IllegalArgumentException("이미 처리된 취소 요청입니다.");
        }

        return request;
    }

    private String normalizeReason(
            String reason,
            String defaultReason) {

        String normalized = reason == null ? "" : reason.trim();

        if (normalized.isEmpty()) {
            normalized = defaultReason;
        }

        if (normalized.length() > 500) {
            throw new IllegalArgumentException("사유는 500자 이하로 입력해주세요.");
        }

        return normalized;
    }
}
