package com.project.oditji.order.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.admin.service.AdminService;

import com.project.oditji.cart.dao.CartDAO;
import com.project.oditji.order.dao.OrderDAO;
import com.project.oditji.order.vo.DeliveryVO;
import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.order.vo.OrderPaymentPrepareVO;
import com.project.oditji.order.vo.OrderSheetItemVO;
import com.project.oditji.order.vo.OrderVO;
import com.project.oditji.payment.dao.PaymentDAO;
import com.project.oditji.payment.service.PaymentService;
import com.project.oditji.payment.vo.PaymentVO;
import com.project.oditji.notification.service.NotificationService;

/**
 * 주문 비즈니스 로직을 처리하는 서비스 구현 클래스.
 * 장바구니 주문 준비, 바로 구매, 결제 검증, 주문 완료, 결제 취소 및 조회 기능을 담당한다.
 */
@Service
public class OrderServiceImpl implements OrderService {

        private final OrderDAO orderDAO;
        private final CartDAO cartDAO;
        private final PaymentDAO paymentDAO;
        private final PaymentService paymentService;
        private final NotificationService notificationService;
        /* [사업자 자동 등급 관리 추가] 결제 완료 후 누적 매출 등급 갱신에 사용한다. */
        private final AdminService adminService;

        /**
         * 의존성 주입을 위한 생성자.
         */
        public OrderServiceImpl(
                        OrderDAO orderDAO,
                        CartDAO cartDAO,
                        PaymentDAO paymentDAO,
                        PaymentService paymentService,
                        NotificationService notificationService,
                        AdminService adminService) {

                this.orderDAO = orderDAO;
                this.cartDAO = cartDAO;
                this.paymentDAO = paymentDAO;
                this.paymentService = paymentService;
                this.notificationService = notificationService;
                this.adminService = adminService;
        }

        /**
         * 장바구니에 담긴 상품들을 대상으로 주문서 작성 데이터를 준비한다.
         * 
         * @param memberNo    로그인 회원 번호
         * @param cartItemNos 선택한 장바구니 항목 번호 목록
         * @return 주문서 작성용 상품 목록
         */
        @Override
        @Transactional(readOnly = true)
        public List<OrderSheetItemVO> prepareCheckoutFromCart(
                        Long memberNo,
                        List<Long> cartItemNos) {

                /*
                 * 회원 번호 및 장바구니 선택 항목 식별자 정제 및 유효성 검증
                 */
                validateMemberNo(memberNo);

                List<Long> normalizedCartItemNos = normalizeCartItemNos(
                                cartItemNos);

                if (normalizedCartItemNos.isEmpty()) {
                        throw new IllegalArgumentException(
                                        "주문할 상품을 선택해주세요.");
                }

                /*
                 * DB에서 선택한 장바구니 상품 상세 정보를 조회한다.
                 */
                List<OrderSheetItemVO> sheetItems = orderDAO.selectCartItemsForOrder(
                                memberNo,
                                normalizedCartItemNos);

                /*
                 * 요청한 장바구니 개수와 실제로 조회된 항목 개수가 일치하는지 확인한다.
                 */
                if (sheetItems == null
                                || sheetItems.size() != normalizedCartItemNos.size()) {

                        throw new IllegalArgumentException(
                                        "선택한 장바구니 상품을 찾을 수 없습니다. "
                                                        + "새로고침 후 다시 시도해주세요.");
                }

                /*
                 * 각 상품의 판매 상태 및 재고 상태를 검증한다.
                 */
                for (OrderSheetItemVO item : sheetItems) {
                        validateAvailable(item);
                }

                return sheetItems;
        }

        /**
         * 단일 상품 바로 구매 시 주문서 작성 데이터를 준비한다.
         * 
         * @param memberNo  로그인 회원 번호
         * @param productNo 상품 번호
         * @param quantity  주문 수량
         * @return 주문서 작성용 상품 목록
         */
        @Override
        @Transactional(readOnly = true)
        public List<OrderSheetItemVO> prepareDirectOrder(
                        Long memberNo,
                        Integer productNo,
                        Long optionNo,
                        Integer quantity) {

                /*
                 * 입력 매개변수 유효성 검증
                 */
                validateMemberNo(memberNo);
                validateProductNo(productNo);
                validateQuantity(quantity);

                /*
                 * 주문 대상 상품 기본 정보 조회
                 */
                OrderSheetItemVO item = orderDAO.selectProductForOrder(
                                productNo, optionNo);

                if (item == null) {
                        throw new IllegalArgumentException(
                                        "존재하지 않는 상품이거나 선택한 옵션이 올바르지 않습니다.");
                }
                // [상품 옵션 기능 추가] 의상/신발은 옵션 없는 직접 구매를 허용하지 않습니다.
                if (("CLOTHES".equals(item.getProductType()) || "SHOES".equals(item.getProductType()))
                                && item.getOptionNo() == null) {
                        throw new IllegalArgumentException("색상과 사이즈 옵션을 선택해주세요.");
                }

                /*
                 * 요청 수량 설정 및 장바구니 식별자 제거 (바로 구매건 구분)
                 */
                item.setQuantity(quantity);
                item.setCartItemNo(null);

                /*
                 * 상품 판매 상태 및 재고 유효성 검증
                 */
                validateAvailable(item);

                List<OrderSheetItemVO> sheetItems = new ArrayList<OrderSheetItemVO>();

                sheetItems.add(item);

                return sheetItems;
        }

        /**
         * PG 결제 창 호출 전 결제 사전 검증 데이터를 생성하고 데이터의 정합성을 확인한다.
         * 
         * @param memberNo      로그인 회원 번호
         * @param sheetItems    주문할 상품 데이터 목록
         * @param receiverName  수령인 이름
         * @param receiverPhone 수령인 연락처
         * @param address       배송지 주소
         * @return PG 결제 요청에 필요한 사전 검증 객체
         */
        @Override
        @Transactional(readOnly = true)
        public OrderPaymentPrepareVO preparePayment(
                        Long memberNo,
                        List<OrderSheetItemVO> sheetItems,
                        String receiverName,
                        String receiverPhone,
                        String address) {

                /*
                 * 기본 요청 데이터 유효성 검증
                 */
                validateMemberNo(memberNo);

                validateDeliveryInformation(
                                receiverName,
                                receiverPhone,
                                address);

                if (sheetItems == null
                                || sheetItems.isEmpty()) {

                        throw new IllegalArgumentException(
                                        "주문할 상품이 없습니다.");
                }

                List<OrderSheetItemVO> confirmedItems = new ArrayList<OrderSheetItemVO>();

                long totalAmount = 0L;

                /*
                 * 클라이언트에서 전달받은 상품 정보를 DB의 최신 데이터 기준으로 재검증하고
                 * 최종 결제 금액 계산을 수행한다.
                 */
                for (OrderSheetItemVO requestedItem : sheetItems) {

                        if (requestedItem == null) {
                                throw new IllegalArgumentException(
                                                "올바르지 않은 주문 상품이 포함되어 있습니다.");
                        }

                        OrderSheetItemVO currentItem = orderDAO.selectProductForOrder(
                                        requestedItem.getProductNo(), requestedItem.getOptionNo());

                        if (currentItem == null) {
                                throw new IllegalArgumentException(
                                                "존재하지 않는 상품이 포함되어 있습니다.");
                        }

                        currentItem.setQuantity(
                                        requestedItem.getQuantity());

                        currentItem.setCartItemNo(
                                        requestedItem.getCartItemNo());

                        validateAvailable(currentItem);

                        totalAmount += currentItem.getItemTotalPrice();

                        confirmedItems.add(currentItem);
                }

                if (totalAmount <= 0) {
                        throw new IllegalArgumentException(
                                        "결제 금액이 올바르지 않습니다.");
                }

                /*
                 * 결제 고유 식별자 및 대표 주문명 생성
                 */
                String paymentId = createPaymentId();

                if (paymentId.length() > 40) {
                        throw new IllegalStateException(
                                        "결제 ID는 40자 이하여야 합니다.");
                }

                String orderName = createOrderName(
                                confirmedItems);

                /*
                 * 결제 준비 데이터 VO 구성
                 */
                OrderPaymentPrepareVO prepareVO = new OrderPaymentPrepareVO();

                prepareVO.setPaymentId(paymentId);
                prepareVO.setOrderName(orderName);
                prepareVO.setTotalAmount(totalAmount);

                prepareVO.setReceiverName(
                                receiverName.trim());

                prepareVO.setReceiverPhone(
                                receiverPhone.trim());

                prepareVO.setAddress(
                                address.trim());

                prepareVO.setItems(
                                confirmedItems);

                return prepareVO;
        }

        /**
         * PG 결제 승인 후, 결제내역 검증 및 주문/주문상세 생성, 재고 차감, 장바구니 비우기 트랜잭션을 처리한다.
         * 
         * @param memberNo         로그인 회원 번호
         * @param paymentPrepareVO 기존 결제 준비 객체 (세션/캐시 저장분)
         * @param paymentId        결제 고유 식별자
         * @return 생성된 주문 번호(PK)
         */
        @Override
        @Transactional
        public Long completePaidOrder(
                        Long memberNo,
                        OrderPaymentPrepareVO paymentPrepareVO,
                        String paymentId) {

                validatePaymentCompletionRequest(
                                memberNo,
                                paymentPrepareVO,
                                paymentId);

                List<OrderSheetItemVO> preparedItems = paymentPrepareVO.getItems();

                validatePaymentNotProcessed(paymentId);
                validatePreparedItemsAvailable(preparedItems);

                PaymentVO verifiedPayment = paymentService.verifyPaidPayment(
                                paymentId,
                                paymentPrepareVO.getTotalAmount(),
                                paymentPrepareVO.getOrderName());

                OrderVO order = createPaidOrder(
                                memberNo,
                                paymentPrepareVO);

                List<Long> usedCartItemNos = new ArrayList<Long>();
                List<Long> createdOrderItemNos = new ArrayList<Long>();

                saveOrderItems(
                                order.getOrderNo(),
                                preparedItems,
                                usedCartItemNos,
                                createdOrderItemNos);

                savePayment(
                                order.getOrderNo(),
                                paymentPrepareVO,
                                verifiedPayment);

                adminService.updateBusinessGradesBySales();
                createWaitingSettlements(createdOrderItemNos);
                deleteUsedCartItems(memberNo, usedCartItemNos);
                notifyOrderBusinesses(order.getOrderNo());

                return order.getOrderNo();
        }

        private void validatePaymentCompletionRequest(
                        Long memberNo,
                        OrderPaymentPrepareVO paymentPrepareVO,
                        String paymentId) {

                validateMemberNo(memberNo);

                if (paymentPrepareVO == null) {
                        throw new IllegalArgumentException(
                                        "결제 준비 정보가 만료되었습니다. "
                                                        + "주문서를 다시 작성해주세요.");
                }

                if (paymentId == null || paymentId.isBlank()) {
                        throw new IllegalArgumentException(
                                        "결제 ID가 없습니다.");
                }

                if (!paymentId.equals(paymentPrepareVO.getPaymentId())) {
                        throw new IllegalArgumentException(
                                        "결제 준비 정보와 결제 ID가 일치하지 않습니다.");
                }

                List<OrderSheetItemVO> preparedItems = paymentPrepareVO.getItems();

                if (preparedItems == null || preparedItems.isEmpty()) {
                        throw new IllegalArgumentException(
                                        "결제 상품 정보가 만료되었습니다.");
                }
        }

        private void validatePaymentNotProcessed(String paymentId) {

                PaymentVO existingPayment = paymentDAO.selectPaymentByPaymentId(
                                paymentId);

                if (existingPayment != null) {
                        throw new IllegalArgumentException(
                                        "이미 처리된 결제입니다.");
                }
        }

        private void validatePreparedItemsAvailable(
                        List<OrderSheetItemVO> preparedItems) {

                for (OrderSheetItemVO preparedItem : preparedItems) {

                        OrderSheetItemVO currentItem = orderDAO.selectProductForOrder(
                                        preparedItem.getProductNo(), preparedItem.getOptionNo());

                        if (currentItem == null) {
                                throw new IllegalArgumentException(
                                                "'"
                                                                + preparedItem.getProductName()
                                                                + "' 상품을 찾을 수 없습니다.");
                        }

                        currentItem.setQuantity(
                                        preparedItem.getQuantity());

                        validateAvailable(currentItem);
                }
        }

        private OrderVO createPaidOrder(
                        Long memberNo,
                        OrderPaymentPrepareVO paymentPrepareVO) {

                OrderVO order = new OrderVO();

                order.setMemberNo(memberNo);
                order.setTotalAmount(paymentPrepareVO.getTotalAmount());
                order.setOrderStatus("PAID");
                order.setReceiverName(paymentPrepareVO.getReceiverName());
                order.setReceiverPhone(paymentPrepareVO.getReceiverPhone());
                order.setAddress(paymentPrepareVO.getAddress());

                int orderInsertResult = orderDAO.insertOrder(order);

                if (orderInsertResult <= 0 || order.getOrderNo() == null) {
                        throw new IllegalStateException(
                                        "주문 생성에 실패했습니다.");
                }

                return order;
        }

        private void saveOrderItems(
                        Long orderNo,
                        List<OrderSheetItemVO> preparedItems,
                        List<Long> usedCartItemNos,
                        List<Long> createdOrderItemNos) {

                for (OrderSheetItemVO item : preparedItems) {

                        OrderItemVO orderItem = new OrderItemVO();

                        orderItem.setOrderNo(orderNo);
                        orderItem.setProductNo(item.getProductNo());
                        orderItem.setOptionNo(item.getOptionNo());
                        orderItem.setBusinessNo(item.getBusinessNo());
                        orderItem.setProductPrice(item.getDiscountPrice());
                        orderItem.setQuantity(item.getQuantity());
                        orderItem.setStatus("PAID");

                        int orderItemInsertResult = orderDAO.insertOrderItem(
                                        orderItem);

                        if (orderItemInsertResult <= 0) {
                                throw new IllegalStateException(
                                                "주문 상세 생성에 실패했습니다.");
                        }

                        createdOrderItemNos.add(orderItem.getOrderItemNo());
                        decreaseProductStock(item);
                        addUsedCartItemNo(item, usedCartItemNos);
                }
        }

        private void decreaseProductStock(OrderSheetItemVO item) {

                int stockUpdateResult;

                if (item.getOptionNo() != null) {
                        stockUpdateResult = orderDAO.decreaseProductOptionStock(
                                        item.getOptionNo(),
                                        item.getQuantity());

                        if (stockUpdateResult > 0) {
                                orderDAO.decreaseProductStock(
                                                item.getProductNo(),
                                                item.getQuantity());
                        }
                } else {
                        stockUpdateResult = orderDAO.decreaseProductStock(
                                        item.getProductNo(),
                                        item.getQuantity());
                }

                if (stockUpdateResult <= 0) {
                        throw new IllegalArgumentException(
                                        "'"
                                                        + item.getProductName()
                                                        + "' 상품의 재고가 부족합니다.");
                }
        }

        private void addUsedCartItemNo(
                        OrderSheetItemVO item,
                        List<Long> usedCartItemNos) {

                if (item.getCartItemNo() != null) {
                        usedCartItemNos.add(item.getCartItemNo());
                }
        }

        private void savePayment(
                        Long orderNo,
                        OrderPaymentPrepareVO paymentPrepareVO,
                        PaymentVO verifiedPayment) {

                verifiedPayment.setOrderNo(orderNo);
                verifiedPayment.setOrderName(paymentPrepareVO.getOrderName());

                int paymentInsertResult = paymentDAO.insertPayment(
                                verifiedPayment);

                if (paymentInsertResult <= 0 || verifiedPayment.getPaymentNo() == null) {
                        throw new IllegalStateException(
                                        "결제내역 저장에 실패했습니다.");
                }
        }

        private void createWaitingSettlements(
                        List<Long> createdOrderItemNos) {

                for (Long orderItemNo : createdOrderItemNos) {

                        int settlementInsertResult = orderDAO.insertWaitingSettlement(
                                        orderItemNo);

                        if (settlementInsertResult <= 0) {
                                throw new IllegalStateException(
                                                "정산 예정 데이터 생성에 실패했습니다.");
                        }
                }
        }

        private void deleteUsedCartItems(
                        Long memberNo,
                        List<Long> usedCartItemNos) {

                if (!usedCartItemNos.isEmpty()) {
                        cartDAO.deleteSelectedCartItems(
                                        memberNo,
                                        usedCartItemNos);
                }
        }

        private void notifyOrderBusinesses(Long orderNo) {

                notificationService.createForOrderBusinesses(
                                orderNo,
                                "NEW_ORDER",
                                "새로운 주문 접수",
                                "새로운 결제 완료 주문이 접수되었습니다.",
                                "/business/order/detail?orderNo=" + orderNo,
                                "ORDER",
                                orderNo);
        }

        /**
         * 사용자가 요청한 결제 완료 주문 건을 환불 처리하고 재고를 복구한다.
         * 
         * @param memberNo 로그인 회원 번호
         * @param orderNo  취소할 주문 번호
         * @param reason   취소 사유
         */
        @Override
        @Transactional
        public void cancelPaidOrder(
                        Long memberNo,
                        Long orderNo,
                        String reason) {

                validateMemberNo(memberNo);

                if (orderNo == null || orderNo <= 0) {
                        throw new IllegalArgumentException(
                                        "주문 번호가 올바르지 않습니다.");
                }

                String normalizedReason = normalizeCancelReason(reason);

                /*
                 * 로그인 회원 소유 주문인지 먼저 확인한다.
                 */
                OrderVO order = orderDAO.selectOrderByMember(
                                memberNo,
                                orderNo);

                if (order == null) {
                        throw new IllegalArgumentException(
                                        "존재하지 않거나 취소 권한이 없는 주문입니다.");
                }

                if ("CANCELED".equals(
                                order.getOrderStatus())) {

                        throw new IllegalArgumentException(
                                        "이미 취소된 주문입니다.");
                }

                if (!"PAID".equals(
                                order.getOrderStatus())) {

                        throw new IllegalArgumentException(
                                        "결제 완료 상태의 주문만 취소할 수 있습니다. "
                                                        + "현재 주문 상태: "
                                                        + order.getOrderStatus());
                }

                /*
                 * 배송 중 또는 배송 완료 상품이 하나라도 있으면
                 * 사용자 즉시 결제 취소를 허용하지 않는다.
                 */
                int startedDeliveryCount = orderDAO.countStartedDeliveryByOrderNo(
                                orderNo);

                if (startedDeliveryCount > 0) {
                        throw new IllegalArgumentException(
                                        "배송이 시작된 주문은 즉시 결제 취소할 수 없습니다.");
                }

                /*
                 * 주문 연동 결제내역 검증
                 */
                PaymentVO payment = paymentDAO.selectPaymentByOrderNo(
                                orderNo);

                if (payment == null) {
                        throw new IllegalArgumentException(
                                        "주문에 연결된 결제내역을 찾을 수 없습니다.");
                }

                if ("CANCELED".equals(
                                payment.getPaymentStatus())) {

                        throw new IllegalArgumentException(
                                        "이미 취소된 결제입니다.");
                }

                if (!"PAID".equals(
                                payment.getPaymentStatus())) {

                        throw new IllegalArgumentException(
                                        "결제 완료 상태의 결제만 취소할 수 있습니다.");
                }

                /*
                 * 포트원에서 실제 전액 취소를 먼저 처리한다.
                 */
                PaymentVO canceledPayment = paymentService.cancelPaidPayment(
                                payment,
                                normalizedReason);

                /*
                 * 상태 변경 전에 PAID 주문상품 기준으로 재고를 복구한다.
                 */
                // [상품 옵션 기능 추가] 전체 재고와 옵션 조합 재고를 함께 복구합니다.
                orderDAO.restoreProductOptionStockByOrderNo(orderNo);
                int restoredProductCount = orderDAO.restoreProductStockByOrderNo(
                                orderNo);

                if (restoredProductCount <= 0) {
                        throw new IllegalStateException(
                                        "취소 상품의 재고를 복구하지 못했습니다.");
                }

                /*
                 * 주문 상품 상태 업데이트 (CANCELED)
                 */
                int orderItemUpdateResult = orderDAO.updateOrderItemsCanceled(
                                orderNo);

                if (orderItemUpdateResult <= 0) {
                        throw new IllegalStateException(
                                        "주문상품 취소 상태 변경에 실패했습니다.");
                }

                /*
                 * =========================================================
                 * [사용자 즉시 전액 취소 정산 제외 추가]
                 * 실제 결제 취소와 주문상품 취소가 성공한 주문의
                 * WAITING 정산을 REJECTED로 변경한다.
                 * =========================================================
                 */
                orderDAO.rejectSettlementsByOrderNo(orderNo);

                /*
                 * 주문 메인 상태 업데이트 (CANCELED)
                 */
                int orderUpdateResult = orderDAO.updateOrderCanceled(
                                memberNo,
                                orderNo);

                if (orderUpdateResult <= 0) {
                        throw new IllegalStateException(
                                        "주문 취소 상태 변경에 실패했습니다.");
                }

                /*
                 * 결제 테이블 상태 및 취소 정보 업데이트 (CANCELED)
                 */
                int paymentUpdateResult = paymentDAO.updatePaymentCanceled(
                                canceledPayment);

                if (paymentUpdateResult <= 0) {
                        throw new IllegalStateException(
                                        "결제 취소 상태 저장에 실패했습니다.");
                }
        }

        /**
         * 로그인한 회원의 전체 주문 목록을 조회한다 (주문 상품 정보 매핑 포함).
         * 
         * @param memberNo 로그인 회원 번호
         * @return 주문 목록
         */
        @Override
        @Transactional(readOnly = true)
        public List<OrderVO> getOrderList(
                        Long memberNo,
                        int startRow,
                        int endRow) {

                validateMemberNo(memberNo);

                if (startRow <= 0 || endRow < startRow) {
                        throw new IllegalArgumentException(
                                        "주문내역 페이지 범위가 올바르지 않습니다.");
                }

                List<OrderVO> orderList = orderDAO.selectOrderListByMember(
                                memberNo,
                                startRow,
                                endRow);

                if (orderList == null) {
                        orderList = new ArrayList<OrderVO>();
                }

                for (OrderVO order : orderList) {

                        List<OrderItemVO> itemList = orderDAO.selectOrderItemListByOrderNo(
                                        order.getOrderNo());

                        order.setItems(
                                        itemList == null
                                                        ? new ArrayList<OrderItemVO>()
                                                        : itemList);
                }

                return orderList;
        }

        @Override
        @Transactional(readOnly = true)
        public int getOrderCount(
                        Long memberNo) {

                validateMemberNo(memberNo);

                return orderDAO.countOrderListByMember(
                                memberNo);
        }

        /**
         * 특정 주문의 상세 정보를 조회한다.
         * 
         * @param memberNo 로그인 회원 번호
         * @param orderNo  조회할 주문 번호
         * @return 주문 상세 객체
         */
        @Override
        @Transactional(readOnly = true)
        public OrderVO getOrderDetail(
                        Long memberNo,
                        Long orderNo) {

                validateMemberNo(memberNo);

                if (orderNo == null || orderNo <= 0) {
                        throw new IllegalArgumentException(
                                        "주문 번호가 올바르지 않습니다.");
                }

                /*
                 * 회원의 주문 기본 데이터 조회
                 */
                OrderVO order = orderDAO.selectOrderByMember(
                                memberNo,
                                orderNo);

                if (order == null) {
                        throw new IllegalArgumentException(
                                        "존재하지 않는 주문입니다.");
                }

                /*
                 * 해당 주문의 상품 목록 조회 후 설정
                 */
                List<OrderItemVO> orderItemList = orderDAO.selectOrderItemListByOrderNo(
                                orderNo);

                order.setItems(orderItemList);

                return order;
        }

        /**
         * [배송 조회 화면 추가]
         * 로그인 회원 소유의 주문상품 배송 정보를 조회한다.
         *
         * @param memberNo    로그인 회원 번호
         * @param orderItemNo 조회할 주문상품 번호
         * @return 배송 조회 객체
         */
        @Override
        @Transactional(readOnly = true)
        public DeliveryVO getDeliveryDetail(
                        Long memberNo,
                        Long orderItemNo) {

                validateMemberNo(memberNo);

                if (orderItemNo == null || orderItemNo <= 0) {
                        throw new IllegalArgumentException(
                                        "주문 상품 번호가 올바르지 않습니다.");
                }

                DeliveryVO delivery = orderDAO.selectDeliveryDetailByMember(
                                memberNo,
                                orderItemNo);

                if (delivery == null) {
                        throw new IllegalArgumentException(
                                        "존재하지 않는 주문상품이거나 조회 권한이 없습니다.");
                }

                return delivery;
        }

        /**
         * PG사에 전달할 난수 기반 결제 고유 식별자(Payment ID)를 생성한다.
         * 
         * @return 결제 ID (예: ODT_32자리UUID)
         */
        private String createPaymentId() {

                String uuid = UUID.randomUUID()
                                .toString()
                                .replace("-", "");

                /*
                 * KG이니시스 결제 ID 최대 40자 제한.
                 * ODT_ 4자 + UUID 32자 = 총 36자.
                 */
                return "ODT_" + uuid;
        }

        /**
         * PG사 결제창 및 대표 결제 내역에 표시될 주문명을 생성한다.
         * 
         * @param items 주문서 항목 목록
         * @return 요약된 주문명 (예: "상품A 외 2건")
         */
        private String createOrderName(
                        List<OrderSheetItemVO> items) {

                if (items == null || items.isEmpty()) {
                        return "ODITJI 상품 주문";
                }

                String firstProductName = items.get(0).getProductName();

                if (firstProductName == null
                                || firstProductName.isBlank()) {

                        firstProductName = "ODITJI 상품";
                }

                String orderName;

                if (items.size() == 1) {
                        orderName = firstProductName;

                } else {

                        orderName = firstProductName
                                        + " 외 "
                                        + (items.size() - 1)
                                        + "건";
                }

                if (orderName.length() > 100) {
                        orderName = orderName.substring(
                                        0,
                                        100);
                }

                return orderName;
        }

        /**
         * 취소 사유 파라미터를 검증하고 유효한 기본값으로 정제한다.
         * 
         * @param reason 사용자 입력 취소 사유
         * @return 정제된 취소 사유
         */
        private String normalizeCancelReason(
                        String reason) {

                String normalizedReason = reason == null
                                ? ""
                                : reason.trim();

                if (normalizedReason.isEmpty()) {
                        normalizedReason = "사용자 요청에 의한 결제 취소";
                }

                if (normalizedReason.length() > 500) {
                        throw new IllegalArgumentException(
                                        "취소 사유는 500자 이하로 입력해주세요.");
                }

                return normalizedReason;
        }

        /**
         * 배송 정보(수령인명, 연락처, 주소)의 유효성을 검증한다.
         */
        private void validateDeliveryInformation(
                        String receiverName,
                        String receiverPhone,
                        String address) {

                if (receiverName == null
                                || receiverName.trim().isEmpty()) {

                        throw new IllegalArgumentException(
                                        "받는 사람을 입력해주세요.");
                }

                if (receiverName.trim().length() > 50) {
                        throw new IllegalArgumentException(
                                        "받는 사람은 50자 이하로 입력해주세요.");
                }

                if (receiverPhone == null
                                || receiverPhone.trim().isEmpty()) {

                        throw new IllegalArgumentException(
                                        "연락처를 입력해주세요.");
                }

                if (receiverPhone.trim().length() > 20) {
                        throw new IllegalArgumentException(
                                        "연락처는 20자 이하로 입력해주세요.");
                }

                if (address == null
                                || address.trim().isEmpty()) {

                        throw new IllegalArgumentException(
                                        "배송지 주소를 입력해주세요.");
                }

                if (address.trim().length() > 300) {
                        throw new IllegalArgumentException(
                                        "배송지 주소는 300자 이하로 입력해주세요.");
                }
        }

        /**
         * 장바구니 PK 목록 중 중복 항목 및 잘못된 번호를 정제(필터링)한다.
         */
        private List<Long> normalizeCartItemNos(
                        List<Long> cartItemNos) {

                List<Long> normalizedList = new ArrayList<Long>();

                if (cartItemNos == null) {
                        return normalizedList;
                }

                for (Long cartItemNo : cartItemNos) {

                        if (cartItemNo == null
                                        || cartItemNo <= 0) {

                                continue;
                        }

                        if (!normalizedList.contains(
                                        cartItemNo)) {

                                normalizedList.add(
                                                cartItemNo);
                        }
                }

                return normalizedList;
        }

        /**
         * 주문 가능 상태(승인 여부, 재고 수량 등)를 검증한다.
         */
        private void validateAvailable(
                        OrderSheetItemVO item) {

                if (item == null) {
                        throw new IllegalArgumentException(
                                        "존재하지 않는 상품입니다.");
                }

                if (!"APPROVED".equals(
                                item.getStatus())) {

                        throw new IllegalArgumentException(
                                        "'"
                                                        + item.getProductName()
                                                        + "'은(는) 현재 판매 중인 상품이 아닙니다.");
                }

                int stock = item.getStock() == null
                                ? 0
                                : item.getStock();

                if (stock <= 0) {
                        throw new IllegalArgumentException(
                                        "'"
                                                        + item.getProductName()
                                                        + "'은(는) 품절된 상품입니다.");
                }

                int quantity = item.getQuantity() == null
                                ? 0
                                : item.getQuantity();

                if (quantity <= 0) {
                        throw new IllegalArgumentException(
                                        "상품 수량은 1개 이상이어야 합니다.");
                }

                if (quantity > stock) {
                        throw new IllegalArgumentException(
                                        "'"
                                                        + item.getProductName()
                                                        + "'의 현재 재고는 "
                                                        + stock
                                                        + "개입니다.");
                }
        }

        /**
         * 회원 번호 유효성을 검증한다.
         */
        private void validateMemberNo(
                        Long memberNo) {

                if (memberNo == null || memberNo <= 0) {
                        throw new IllegalArgumentException(
                                        "로그인 회원 정보가 올바르지 않습니다.");
                }
        }

        /**
         * 상품 번호 유효성을 검증한다.
         */
        private void validateProductNo(
                        Integer productNo) {

                if (productNo == null || productNo <= 0) {
                        throw new IllegalArgumentException(
                                        "상품 번호가 올바르지 않습니다.");
                }
        }

        /**
         * 주문 수량 유효성을 검증한다.
         */
        private void validateQuantity(
                        Integer quantity) {

                if (quantity == null || quantity <= 0) {
                        throw new IllegalArgumentException(
                                        "상품 수량은 1개 이상이어야 합니다.");
                }
        }
}