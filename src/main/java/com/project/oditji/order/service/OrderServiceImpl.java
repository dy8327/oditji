package com.project.oditji.order.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.cart.dao.CartDAO;
import com.project.oditji.order.dao.OrderDAO;
import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.order.vo.OrderPaymentPrepareVO;
import com.project.oditji.order.vo.OrderSheetItemVO;
import com.project.oditji.order.vo.OrderVO;
import com.project.oditji.payment.dao.PaymentDAO;
import com.project.oditji.payment.service.PaymentService;
import com.project.oditji.payment.vo.PaymentVO;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;
    private final PaymentDAO paymentDAO;
    private final PaymentService paymentService;

    public OrderServiceImpl(
            OrderDAO orderDAO,
            CartDAO cartDAO,
            PaymentDAO paymentDAO,
            PaymentService paymentService) {

        this.orderDAO = orderDAO;
        this.cartDAO = cartDAO;
        this.paymentDAO = paymentDAO;
        this.paymentService = paymentService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSheetItemVO> prepareCheckoutFromCart(
            Long memberNo,
            List<Long> cartItemNos) {

        validateMemberNo(memberNo);

        List<Long> normalizedCartItemNos = normalizeCartItemNos(cartItemNos);

        if (normalizedCartItemNos.isEmpty()) {
            throw new IllegalArgumentException(
                    "주문할 상품을 선택해주세요.");
        }

        List<OrderSheetItemVO> sheetItems = orderDAO.selectCartItemsForOrder(
                memberNo,
                normalizedCartItemNos);

        if (sheetItems == null
                || sheetItems.size() != normalizedCartItemNos.size()) {

            throw new IllegalArgumentException(
                    "선택한 장바구니 상품을 찾을 수 없습니다. "
                            + "새로고침 후 다시 시도해주세요.");
        }

        for (OrderSheetItemVO item : sheetItems) {
            validateAvailable(item);
        }

        return sheetItems;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSheetItemVO> prepareDirectOrder(
            Long memberNo,
            Integer productNo,
            Integer quantity) {

        validateMemberNo(memberNo);
        validateProductNo(productNo);
        validateQuantity(quantity);

        OrderSheetItemVO item = orderDAO.selectProductForOrder(productNo);

        if (item == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 상품입니다.");
        }

        item.setQuantity(quantity);
        item.setCartItemNo(null);

        validateAvailable(item);

        List<OrderSheetItemVO> sheetItems = new ArrayList<OrderSheetItemVO>();

        sheetItems.add(item);

        return sheetItems;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderPaymentPrepareVO preparePayment(
            Long memberNo,
            List<OrderSheetItemVO> sheetItems,
            String receiverName,
            String receiverPhone,
            String address) {

        validateMemberNo(memberNo);
        validateDeliveryInformation(
                receiverName,
                receiverPhone,
                address);

        if (sheetItems == null || sheetItems.isEmpty()) {
            throw new IllegalArgumentException(
                    "주문할 상품이 없습니다.");
        }

        /*
         * 세션에 있던 가격을 그대로 사용하지 않고
         * 결제창 호출 직전에 DB에서 상품을 다시 조회한다.
         */
        List<OrderSheetItemVO> confirmedItems = new ArrayList<OrderSheetItemVO>();

        long totalAmount = 0L;

        for (OrderSheetItemVO requestedItem : sheetItems) {

            if (requestedItem == null) {
                throw new IllegalArgumentException(
                        "올바르지 않은 주문 상품이 포함되어 있습니다.");
            }

            OrderSheetItemVO currentItem = orderDAO.selectProductForOrder(
                    requestedItem.getProductNo());

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

        String paymentId = createPaymentId(memberNo);

        if (paymentId.length() > 40) {
            throw new IllegalStateException(
                    "결제 ID는 40자 이하여야 합니다.");
        }

        String orderName = createOrderName(confirmedItems);

        OrderPaymentPrepareVO prepareVO = new OrderPaymentPrepareVO();

        prepareVO.setPaymentId(paymentId);
        prepareVO.setOrderName(orderName);
        prepareVO.setTotalAmount(totalAmount);
        prepareVO.setReceiverName(receiverName.trim());
        prepareVO.setReceiverPhone(receiverPhone.trim());
        prepareVO.setAddress(address.trim());
        prepareVO.setItems(confirmedItems);

        return prepareVO;
    }

    @Override
    @Transactional
    public Long completePaidOrder(
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

        if (!paymentId.equals(
                paymentPrepareVO.getPaymentId())) {

            throw new IllegalArgumentException(
                    "결제 준비 정보와 결제 ID가 일치하지 않습니다.");
        }

        List<OrderSheetItemVO> preparedItems = paymentPrepareVO.getItems();

        if (preparedItems == null
                || preparedItems.isEmpty()) {

            throw new IllegalArgumentException(
                    "결제 상품 정보가 만료되었습니다.");
        }

        /*
         * PAYMENT_ID 중복 저장 및 중복 주문 생성 방지
         */
        PaymentVO existingPayment = paymentDAO.selectPaymentByPaymentId(paymentId);

        if (existingPayment != null) {
            throw new IllegalArgumentException(
                    "이미 처리된 결제입니다.");
        }

        /*
         * 결제 완료 시점에는 상품 가격을 다시 계산하지 않는다.
         * 사용자는 결제 준비 단계에서 확정된 금액으로 이미 결제했기 때문이다.
         *
         * 단, 상품 판매 상태와 재고는 다시 확인한다.
         */
        for (OrderSheetItemVO preparedItem : preparedItems) {

            OrderSheetItemVO currentItem = orderDAO.selectProductForOrder(
                    preparedItem.getProductNo());

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

        /*
         * 포트원 서버 API로 결제 상태 및 실제 결제 금액 검증
         */
        PaymentVO verifiedPayment = paymentService.verifyPaidPayment(
                paymentId,
                paymentPrepareVO.getTotalAmount(),
                paymentPrepareVO.getOrderName());

        OrderVO order = new OrderVO();

        order.setMemberNo(memberNo);
        order.setTotalAmount(
                paymentPrepareVO.getTotalAmount());
        order.setOrderStatus("PAID");
        order.setReceiverName(
                paymentPrepareVO.getReceiverName());
        order.setReceiverPhone(
                paymentPrepareVO.getReceiverPhone());
        order.setAddress(
                paymentPrepareVO.getAddress());

        int orderInsertResult = orderDAO.insertOrder(order);

        if (orderInsertResult <= 0
                || order.getOrderNo() == null) {

            throw new IllegalStateException(
                    "주문 생성에 실패했습니다.");
        }

        List<Long> usedCartItemNos = new ArrayList<Long>();

        for (OrderSheetItemVO item : preparedItems) {

            OrderItemVO orderItem = new OrderItemVO();

            orderItem.setOrderNo(
                    order.getOrderNo());

            orderItem.setProductNo(
                    item.getProductNo());

            orderItem.setBusinessNo(
                    item.getBusinessNo());

            orderItem.setProductPrice(
                    item.getDiscountPrice());

            orderItem.setQuantity(
                    item.getQuantity());

            orderItem.setStatus("PAID");

            int orderItemInsertResult = orderDAO.insertOrderItem(orderItem);

            if (orderItemInsertResult <= 0) {
                throw new IllegalStateException(
                        "주문 상세 생성에 실패했습니다.");
            }

            int stockUpdateResult = orderDAO.decreaseProductStock(
                    item.getProductNo(),
                    item.getQuantity());

            if (stockUpdateResult <= 0) {
                throw new IllegalArgumentException(
                        "'"
                                + item.getProductName()
                                + "' 상품의 재고가 부족합니다.");
            }

            if (item.getCartItemNo() != null) {
                usedCartItemNos.add(
                        item.getCartItemNo());
            }
        }

        /*
         * 생성한 주문 번호를 실제 결제내역과 연결한다.
         */
        verifiedPayment.setOrderNo(
                order.getOrderNo());

        verifiedPayment.setOrderName(
                paymentPrepareVO.getOrderName());

        int paymentInsertResult = paymentDAO.insertPayment(
                verifiedPayment);

        if (paymentInsertResult <= 0
                || verifiedPayment.getPaymentNo() == null) {

            throw new IllegalStateException(
                    "결제내역 저장에 실패했습니다.");
        }

        if (!usedCartItemNos.isEmpty()) {

            cartDAO.deleteSelectedCartItems(
                    memberNo,
                    usedCartItemNos);
        }

        return order.getOrderNo();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderVO> getOrderList(
            Long memberNo) {

        validateMemberNo(memberNo);

        List<OrderVO> orderList = orderDAO.selectOrderListByMember(memberNo);

        if (orderList == null) {
            orderList = new ArrayList<OrderVO>();
        }

        List<OrderItemVO> orderItemList = orderDAO.selectOrderItemListByMember(memberNo);

        if (orderItemList == null) {
            orderItemList = new ArrayList<OrderItemVO>();
        }

        Map<Long, List<OrderItemVO>> itemsByOrderNo = new LinkedHashMap<Long, List<OrderItemVO>>();

        for (OrderItemVO orderItem : orderItemList) {

            List<OrderItemVO> groupedItems = itemsByOrderNo.get(
                    orderItem.getOrderNo());

            if (groupedItems == null) {

                groupedItems = new ArrayList<OrderItemVO>();

                itemsByOrderNo.put(
                        orderItem.getOrderNo(),
                        groupedItems);
            }

            groupedItems.add(orderItem);
        }

        for (OrderVO order : orderList) {

            List<OrderItemVO> groupedItems = itemsByOrderNo.get(
                    order.getOrderNo());

            order.setItems(
                    groupedItems == null
                            ? new ArrayList<OrderItemVO>()
                            : groupedItems);
        }

        return orderList;
    }

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

        OrderVO order = orderDAO.selectOrderByMember(
                memberNo,
                orderNo);

        if (order == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 주문입니다.");
        }

        List<OrderItemVO> orderItemList = orderDAO.selectOrderItemListByOrderNo(
                orderNo);

        order.setItems(orderItemList);

        return order;
    }

    private String createPaymentId(
            Long memberNo) {

        /*
         * KG이니시스 paymentId 제한:
         * 최대 40자
         *
         * UUID의 하이픈을 제거하면 32자이며,
         * "ODT_" 접두사를 붙여도 총 36자이므로
         * 회원 번호 자릿수와 관계없이 제한을 넘지 않는다.
         *
         * 생성 예:
         * ODT_19aa014eed2b4ab791ac09ea1b668447
         */
        String uuid = UUID.randomUUID()
                .toString()
                .replace("-", "");

        return "ODT_" + uuid;
    }

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

        /*
         * PAYMENT.ORDER_NAME VARCHAR2(200)
         * 포트원 결제창 주문명도 과도하게 길지 않도록 제한한다.
         */
        if (orderName.length() > 100) {
            orderName = orderName.substring(0, 100);
        }

        return orderName;
    }

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

            if (!normalizedList.contains(cartItemNo)) {
                normalizedList.add(cartItemNo);
            }
        }

        return normalizedList;
    }

    private void validateAvailable(
            OrderSheetItemVO item) {

        if (item == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 상품입니다.");
        }

        if (!"APPROVED".equals(item.getStatus())) {
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

    private void validateMemberNo(
            Long memberNo) {

        if (memberNo == null || memberNo <= 0) {
            throw new IllegalArgumentException(
                    "로그인 회원 정보가 올바르지 않습니다.");
        }
    }

    private void validateProductNo(
            Integer productNo) {

        if (productNo == null || productNo <= 0) {
            throw new IllegalArgumentException(
                    "상품 번호가 올바르지 않습니다.");
        }
    }

    private void validateQuantity(
            Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "상품 수량은 1개 이상이어야 합니다.");
        }
    }
}