package com.project.oditji.refund.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.payment.dao.PaymentDAO;
import com.project.oditji.payment.service.PaymentService;
import com.project.oditji.payment.vo.PaymentVO;
import com.project.oditji.refund.dao.OrderCancelRefundDAO;
import com.project.oditji.refund.vo.OrderCancelRefundVO;
import com.project.oditji.notification.service.NotificationService;

@Service
public class OrderCancelRefundServiceImpl implements OrderCancelRefundService {

    private static final String WAITING = "WAITING";
    private static final String FULL = "FULL";
    private static final String PARTIAL = "PARTIAL";
    private static final String CANCEL = "CANCEL";
    private static final String ORDER_NUMBER_PREFIX = "주문번호 ";
    private static final String ORDER_LIST_URL = "/order/list";

    private final OrderCancelRefundDAO orderCancelRefundDAO;
    private final BusinessDAO businessDAO;
    private final PaymentDAO paymentDAO;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    /*
     * =========================================================
     * [포트원 테스트 채널 간편결제 부분 취소 제한 설정]
     * =========================================================
     */
    @Value("${portone.payment.test-mode:true}")
    private boolean portOneTestMode;

    public OrderCancelRefundServiceImpl(
            OrderCancelRefundDAO orderCancelRefundDAO,
            BusinessDAO businessDAO,
            PaymentDAO paymentDAO,
            PaymentService paymentService,
            NotificationService notificationService) {

        this.orderCancelRefundDAO = orderCancelRefundDAO;
        this.businessDAO = businessDAO;
        this.paymentDAO = paymentDAO;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
    }

    /*
     * =========================================================
     * [추가] 사용자 취소/환불 내역 조건 조회
     * 조회 유형은 CANCEL/REFUND, 처리 상태는 WAITING/APPROVED/REJECTED만 허용한다.
     * 종료일은 Mapper에서 다음 날 미만으로 비교하여 선택 날짜 전체를 포함한다.
     * =========================================================
     */
    @Override
    public List<OrderCancelRefundVO> getMemberCancelRefundHistory(
            Long memberNo, String historyType, String status, LocalDate startDate, LocalDate endDate) {

        validateMemberNo(memberNo);

        String normalizedType = normalizeHistoryFilter(historyType, SetType.HISTORY_TYPE);
        String normalizedStatus = normalizeHistoryFilter(status, SetType.STATUS);

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("조회 시작일은 종료일보다 늦을 수 없습니다.");
        }

        return orderCancelRefundDAO.selectMemberCancelRefundHistory(
                memberNo, normalizedType, normalizedStatus,
                startDate,
                endDate);
    }

    private enum SetType {
        HISTORY_TYPE, STATUS
    }

    private String normalizeHistoryFilter(String value, SetType type) {
        if (value == null || value.isBlank() || "ALL".equalsIgnoreCase(value)) {
            return null;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        boolean valid = type == SetType.HISTORY_TYPE
                ? (CANCEL.equals(normalized) || "REFUND".equals(normalized))
                : (WAITING.equals(normalized) || "APPROVED".equals(normalized) || "REJECTED".equals(normalized));

        if (!valid) {
            throw new IllegalArgumentException("올바르지 않은 취소/환불 조회 조건입니다.");
        }
        return normalized;
    }

    /*
     * =========================================================
     * [주문 전체 취소 요청 기능 수정]
     *
     * 사용자 화면에서는 주문 전체 취소를 한 건으로 요청한다.
     * DB에는 주문상품별 행을 저장하되 동일한 CANCEL_GROUP_NO와
     * FULL 유형으로 묶어 하나의 전체 취소 요청으로 관리한다.
     *
     * 사업자 승인 단계에서는 환불하지 않고 모든 사업자의 승인이
     * 완료된 마지막 시점에만 포트원 전액 환불을 한 번 호출한다.
     * =========================================================
     */
    @Override
    @Transactional
    public void requestOrderCancel(Long memberNo, Long orderNo, String reason) {

        validateMemberNo(memberNo);

        if (orderNo == null || orderNo <= 0) {
            throw new IllegalArgumentException("주문 번호가 올바르지 않습니다.");
        }

        String normalizedReason = normalizeReason(reason, "주문 전체 취소 요청");
        List<OrderItemVO> itemList = orderCancelRefundDAO.selectCancelableItemsByOrder(memberNo, orderNo);

        if (itemList == null || itemList.isEmpty()) {
            throw new IllegalArgumentException(
                    "전체 취소 또는 전체 환불을 요청할 수 있는 주문상품이 없습니다.");
        }

        for (OrderItemVO item : itemList) {
            if (orderCancelRefundDAO.countWaitingCancelByOrderItemNo(item.getOrderItemNo()) > 0) {
                throw new IllegalArgumentException("이미 취소 요청이 접수된 상품이 포함되어 있습니다.");
            }
        }

        Long cancelGroupNo = orderCancelRefundDAO.selectNextCancelGroupNo();

        for (OrderItemVO item : itemList) {
            OrderCancelRefundVO requestVO = createRequestVO(
                    memberNo,
                    orderNo,
                    item,
                    FULL,
                    cancelGroupNo,
                    normalizedReason);

            if (orderCancelRefundDAO.insertCancelRequest(requestVO) != 1) {
                throw new IllegalStateException("전체 취소 요청 저장에 실패했습니다.");
            }

            if (orderCancelRefundDAO.updateOrderItemCancelRequested(item.getOrderItemNo()) != 1) {
                throw new IllegalStateException("주문상품 상태 변경에 실패했습니다.");
            }
        }

        orderCancelRefundDAO.updateOrderStatusByItems(orderNo);

        notificationService.createForCancelGroupBusinesses(
                cancelGroupNo,
                "REFUND_REQUESTED",
                "전체 취소/환불 요청",
                ORDER_NUMBER_PREFIX + orderNo + "의 전체 취소/환불 요청이 접수되었습니다.",
                "/business/cancel/list",
                CANCEL,
                cancelGroupNo);
    }

    /*
     * =========================================================
     * [추가] 선택 상품 일괄 취소/환불 요청
     * 하나라도 실패하면 전체 요청을 롤백하여 일부 상품만 접수되는 문제를 방지한다.
     * =========================================================
     */
    @Override
    @Transactional
    public void requestOrderItemsCancel(Long memberNo, List<Long> orderItemNos, String reason) {
        validateMemberNo(memberNo);

        if (orderItemNos == null || orderItemNos.isEmpty()) {
            throw new IllegalArgumentException("선택한 주문상품이 없습니다.");
        }

        List<Long> distinctItemNos = orderItemNos.stream()
                .filter(itemNo -> itemNo != null && itemNo > 0)
                .distinct()
                .toList();

        if (distinctItemNos.size() != orderItemNos.size()) {
            throw new IllegalArgumentException("선택한 주문상품 정보가 올바르지 않습니다.");
        }

        for (Long orderItemNo : distinctItemNos) {
            requestOrderItemCancelInternal(memberNo, orderItemNo, reason);
        }
    }

    /*
     * =========================================================
     * [상품별 부분 취소 요청 기능 추가]
     *
     * 사용자가 선택한 ORDER_ITEM 한 건만 PARTIAL 유형으로 요청한다.
     * 해당 상품의 사업자가 승인하면 해당 금액만 부분 환불한다.
     * =========================================================
     */
    @Override
    @Transactional
    public void requestOrderItemCancel(Long memberNo, Long orderItemNo, String reason) {

        validateMemberNo(memberNo);
        requestOrderItemCancelInternal(memberNo, orderItemNo, reason);
    }

    private void requestOrderItemCancelInternal(Long memberNo, Long orderItemNo, String reason) {

        if (orderItemNo == null || orderItemNo <= 0) {
            throw new IllegalArgumentException("주문상품 번호가 올바르지 않습니다.");
        }

        if (orderCancelRefundDAO.countWaitingCancelByOrderItemNo(orderItemNo) > 0) {
            throw new IllegalArgumentException("이미 취소 요청이 접수된 상품입니다.");
        }

        OrderItemVO item = orderCancelRefundDAO.selectCancelableItem(memberNo, orderItemNo);

        if (item == null) {
            throw new IllegalArgumentException(
                    "주문 확인중 상품만 취소할 수 있고, 배송 완료 상품만 환불할 수 있습니다.");
        }

        /*
         * =========================================================
         * [간편결제 부분 취소 요청 서버 차단 추가]
         *
         * 화면에서 버튼을 안내하더라도 요청 URL을 직접 호출할 수 있으므로
         * Service 계층에서 결제수단을 다시 확인한다.
         * 테스트 채널의 간편결제 주문은 전체 취소만 허용한다.
         * =========================================================
         */
        PaymentVO payment = getPayment(item.getOrderNo());

        if (portOneTestMode && isEasyPay(payment)) {
            throw new IllegalArgumentException(
                    "테스트 채널의 간편결제 주문은 상품 부분 취소를 지원하지 않습니다. "
                            + "주문 전체 취소를 이용해주세요.");
        }

        String normalizedReason = normalizeReason(reason, "상품 부분 취소 요청");
        Long cancelGroupNo = orderCancelRefundDAO.selectNextCancelGroupNo();

        OrderCancelRefundVO requestVO = createRequestVO(
                memberNo,
                item.getOrderNo(),
                item,
                PARTIAL,
                cancelGroupNo,
                normalizedReason);

        if (orderCancelRefundDAO.insertCancelRequest(requestVO) != 1) {
            throw new IllegalStateException("부분 취소 요청 저장에 실패했습니다.");
        }

        if (orderCancelRefundDAO.updateOrderItemCancelRequested(orderItemNo) != 1) {
            throw new IllegalStateException("주문상품 상태 변경에 실패했습니다.");
        }

        orderCancelRefundDAO.updateOrderStatusByItems(item.getOrderNo());

        notificationService.createForCancelGroupBusinesses(
                cancelGroupNo,
                "REFUND_REQUESTED",
                "상품 취소/환불 요청",
                ORDER_NUMBER_PREFIX + item.getOrderNo()
                        + "의 상품 취소/환불 요청이 접수되었습니다.",
                "/business/cancel/list",
                CANCEL,
                cancelGroupNo);
    }

    @Override
    public List<OrderCancelRefundVO> getBusinessCancelList(Long memberNo, String status) {

        BusinessVO business = getBusiness(memberNo);
        String normalizedStatus = status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)
                ? null
                : status.trim().toUpperCase();

        return orderCancelRefundDAO.selectCancelListByBusiness(
                business.getBusinessNo(),
                normalizedStatus);
    }

    /*
     * =========================================================
     * [전체/부분 취소 승인 처리 분리]
     *
     * FULL
     * - 현재 사업자 소유 상품들을 한 번에 승인한다.
     * - 모든 사업자가 승인한 경우에만 포트원 전액 환불,
     * 전체 재고 복구, 주문 전체 취소를 처리한다.
     *
     * PARTIAL
     * - 해당 상품 금액만 즉시 포트원 부분 환불한다.
     * =========================================================
     */
    @Override
    @Transactional
    public void approveCancel(Long memberNo, Long cancelNo) {

        BusinessVO business = getBusiness(memberNo);
        OrderCancelRefundVO request = getWaitingRequest(cancelNo, business.getBusinessNo());

        if (FULL.equals(request.getCancelType())) {
            approveFullCancel(request, business.getBusinessNo());
            return;
        }

        approvePartialCancel(request);
    }

    private void approveFullCancel(OrderCancelRefundVO request, Long businessNo) {

        int updatedCount = orderCancelRefundDAO.approveFullGroupForBusiness(
                request.getCancelGroupNo(),
                businessNo);

        if (updatedCount <= 0) {
            throw new IllegalStateException("전체 취소 승인 처리에 실패했습니다.");
        }

        if (orderCancelRefundDAO.countRejectedByGroup(request.getCancelGroupNo()) > 0) {
            throw new IllegalArgumentException("이미 반려된 전체 취소 요청입니다.");
        }

        if (orderCancelRefundDAO.countWaitingByGroup(request.getCancelGroupNo()) > 0) {
            return;
        }

        PaymentVO payment = getPayment(request.getOrderNo());
        PaymentVO canceledPayment = paymentService.cancelPaidPayment(
                payment,
                request.getReason());

        List<OrderCancelRefundVO> groupRequests = orderCancelRefundDAO.selectRequestsByGroup(
                request.getCancelGroupNo());

        for (OrderCancelRefundVO groupItem : groupRequests) {
            if (orderCancelRefundDAO.restoreProductStock(
                    groupItem.getProductNo(),
                    groupItem.getQuantity()) != 1) {
                throw new IllegalStateException("상품 재고 복구에 실패했습니다.");
            }
        }

        int canceledItemCount = orderCancelRefundDAO.cancelOrderItemsByGroup(
                request.getCancelGroupNo());

        if (canceledItemCount != groupRequests.size()) {
            throw new IllegalStateException("전체 주문상품 취소 상태 변경에 실패했습니다.");
        }

        /*
         * =========================================================
         * [전체 취소 완료 상품 정산 제외 추가]
         * 실제 결제 전액 취소와 주문상품 취소가 완료된 뒤
         * 그룹에 포함된 WAITING 정산을 REJECTED로 변경한다.
         * =========================================================
         */
        orderCancelRefundDAO.rejectSettlementsByCancelGroupNo(
                request.getCancelGroupNo());

        canceledPayment.setCanceledAmount(canceledPayment.getPaymentAmount());

        if (paymentDAO.updatePaymentCanceled(canceledPayment) != 1) {
            throw new IllegalStateException("결제 전액 취소 정보 저장에 실패했습니다.");
        }

        orderCancelRefundDAO.updateOrderStatusByItems(request.getOrderNo());

        notificationService.createForMember(
                request.getMemberNo(),
                "REFUND_COMPLETED",
                "환불이 완료되었습니다.",
                ORDER_NUMBER_PREFIX + request.getOrderNo()
                        + "의 전체 취소 및 결제 환불이 완료되었습니다.",
                ORDER_LIST_URL,
                CANCEL,
                request.getCancelGroupNo());
    }

    private void approvePartialCancel(OrderCancelRefundVO request) {

        PaymentVO payment = getPayment(request.getOrderNo());
        long cancelAmount = request.getRefundAmount() == null
                ? 0L
                : request.getRefundAmount();

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

        /*
         * =========================================================
         * [부분 취소 완료 상품 정산 제외 추가]
         * 실제 부분 환불과 주문상품 취소가 완료된 뒤
         * 해당 WAITING 정산을 REJECTED로 변경한다.
         * =========================================================
         */
        orderCancelRefundDAO.rejectSettlementByOrderItemNo(
                request.getOrderItemNo());

        if (orderCancelRefundDAO.restoreProductStock(
                request.getProductNo(),
                request.getQuantity()) != 1) {
            throw new IllegalStateException("상품 재고 복구에 실패했습니다.");
        }

        if (orderCancelRefundDAO.approveCancelRequest(request.getCancelNo()) != 1) {
            throw new IllegalStateException("부분 취소 요청 승인 처리에 실패했습니다.");
        }

        if (paymentDAO.updatePaymentPartialCanceled(canceledPayment) != 1) {
            throw new IllegalStateException("결제 부분 취소 정보 저장에 실패했습니다.");
        }

        orderCancelRefundDAO.updateOrderStatusByItems(request.getOrderNo());

        notificationService.createForMember(
                request.getMemberNo(),
                "REFUND_COMPLETED",
                "환불이 완료되었습니다.",
                buildProductMessage(
                        request,
                        "의 취소 및 결제 환불이 완료되었습니다."),
                ORDER_LIST_URL,
                CANCEL,
                request.getCancelNo());
    }

    /*
     * =========================================================
     * [전체 취소 반려 처리 수정]
     *
     * 전체 취소 요청은 어느 한 사업자라도 반려하면 그룹 전체를
     * 반려 처리하고 모든 주문상품 상태를 결제 완료 상태로 복구한다.
     * 실제 환불과 재고 복구는 실행하지 않는다.
     * =========================================================
     */
    @Override
    @Transactional
    public void rejectCancel(Long memberNo, Long cancelNo, String rejectReason) {

        BusinessVO business = getBusiness(memberNo);
        OrderCancelRefundVO request = getWaitingRequest(cancelNo, business.getBusinessNo());
        String normalizedReason = normalizeReason(rejectReason, "사업자 사유로 취소 요청 반려");

        if (FULL.equals(request.getCancelType())) {
            if (orderCancelRefundDAO.rejectFullGroup(
                    request.getCancelGroupNo(),
                    normalizedReason) <= 0) {
                throw new IllegalStateException("전체 취소 요청 반려 처리에 실패했습니다.");
            }

            orderCancelRefundDAO.restoreOrderItemsByGroup(request.getCancelGroupNo());
        } else {
            if (orderCancelRefundDAO.rejectCancelRequest(
                    request.getCancelNo(),
                    normalizedReason) != 1) {
                throw new IllegalStateException("부분 취소 요청 반려 처리에 실패했습니다.");
            }

            if (orderCancelRefundDAO.restoreOrderItemStatus(request.getOrderItemNo()) != 1) {
                throw new IllegalStateException("주문상품 상태 복구에 실패했습니다.");
            }
        }

        orderCancelRefundDAO.updateOrderStatusByItems(request.getOrderNo());

        notificationService.createForMember(
                request.getMemberNo(),
                "REFUND_REJECTED",
                "환불 요청이 반려되었습니다.",
                buildRefundRejectedMessage(request, normalizedReason),
                ORDER_LIST_URL,
                CANCEL,
                FULL.equals(request.getCancelType())
                        ? request.getCancelGroupNo()
                        : request.getCancelNo());
    }

    /**
     * 상품명이 있으면 상품명을 사용하고, 없으면 주문번호를 사용해
     * 일반 사용자 알림 문구를 생성한다.
     */
    private String buildProductMessage(
            OrderCancelRefundVO request,
            String suffix) {

        if (request.getProductName() != null
                && !request.getProductName().isBlank()) {
            return "‘" + request.getProductName().trim() + "’" + suffix;
        }

        return ORDER_NUMBER_PREFIX + request.getOrderNo() + suffix;
    }

    /**
     * 전체 요청과 상품별 요청을 구분해 반려 사유가 포함된 문구를 생성한다.
     */
    private String buildRefundRejectedMessage(
            OrderCancelRefundVO request,
            String rejectReason) {

        String targetMessage;

        if (FULL.equals(request.getCancelType())) {
            targetMessage = ORDER_NUMBER_PREFIX + request.getOrderNo()
                    + "의 전체 취소/환불 요청이 반려되었습니다.";
        } else {
            targetMessage = buildProductMessage(
                    request,
                    "의 취소/환불 요청이 반려되었습니다.");
        }

        return targetMessage + " 반려 사유: " + rejectReason;
    }

    private OrderCancelRefundVO createRequestVO(
            Long memberNo,
            Long orderNo,
            OrderItemVO item,
            String cancelType,
            Long cancelGroupNo,
            String reason) {

        OrderCancelRefundVO requestVO = new OrderCancelRefundVO();
        requestVO.setOrderNo(orderNo);
        requestVO.setOrderItemNo(item.getOrderItemNo());
        requestVO.setMemberNo(memberNo);
        requestVO.setCancelType(cancelType);
        requestVO.setCancelGroupNo(cancelGroupNo);
        requestVO.setRefundAmount(item.getItemTotalPrice());
        requestVO.setReason(reason);
        requestVO.setStatus(WAITING);
        return requestVO;
    }

    private PaymentVO getPayment(Long orderNo) {
        PaymentVO payment = paymentDAO.selectPaymentByOrderNo(orderNo);

        if (payment == null) {
            throw new IllegalArgumentException("주문 결제내역을 찾을 수 없습니다.");
        }

        return payment;
    }

    private BusinessVO getBusiness(Long memberNo) {
        validateMemberNo(memberNo);

        BusinessVO business = businessDAO.selectBusinessByMemberNo(memberNo);

        if (business == null) {
            throw new IllegalArgumentException("로그인 회원과 연결된 사업자 정보가 없습니다.");
        }

        return business;
    }

    private OrderCancelRefundVO getWaitingRequest(Long cancelNo, Long businessNo) {

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

    /*
     * =========================================================
     * [간편결제 여부 판별 추가]
     *
     * 포트원 결제 조회 시 PAYMENT.PAY_METHOD에 저장된 값을 기준으로
     * 일반 카드(CARD)와 간편결제(EASY_PAY 등)를 구분한다.
     * =========================================================
     */
    private boolean isEasyPay(PaymentVO payment) {

        if (payment == null) {
            return false;
        }

        String payMethod = payment.getPayMethod();

        if (payMethod == null || payMethod.isBlank()) {
            return false;
        }

        String normalized = payMethod.trim()
                .toUpperCase()
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "");

        return normalized.contains("EASYPAY")
                || normalized.contains("KAKAOPAY")
                || normalized.contains("NAVERPAY")
                || normalized.contains("TOSSPAY")
                || normalized.contains("PAYCO")
                || normalized.contains("SAMSUNGPAY")
                || normalized.contains("SSGPAY")
                || normalized.contains("LPAY");
    }

    private void validateMemberNo(Long memberNo) {
        if (memberNo == null || memberNo <= 0) {
            throw new IllegalArgumentException("로그인 회원 정보가 올바르지 않습니다.");
        }
    }

    private String normalizeReason(String reason, String defaultReason) {
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