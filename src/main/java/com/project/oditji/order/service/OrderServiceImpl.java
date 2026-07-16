package com.project.oditji.order.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.cart.dao.CartDAO;
import com.project.oditji.order.dao.OrderDAO;
import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.order.vo.OrderSheetItemVO;
import com.project.oditji.order.vo.OrderVO;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;

    public OrderServiceImpl(
            OrderDAO orderDAO,
            CartDAO cartDAO) {

        this.orderDAO = orderDAO;
        this.cartDAO = cartDAO;
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
                    "주문할 상품을 선택해주세요."
            );
        }

        List<OrderSheetItemVO> sheetItems =
                orderDAO.selectCartItemsForOrder(
                        memberNo,
                        normalizedCartItemNos
                );

        if (sheetItems == null
                || sheetItems.size() != normalizedCartItemNos.size()) {

            throw new IllegalArgumentException(
                    "선택한 장바구니 상품을 찾을 수 없습니다. 새로고침 후 다시 시도해주세요."
            );
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

        OrderSheetItemVO item =
                orderDAO.selectProductForOrder(productNo);

        if (item == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 상품입니다."
            );
        }

        item.setQuantity(quantity);
        item.setCartItemNo(null);

        validateAvailable(item);

        List<OrderSheetItemVO> sheetItems =
                new ArrayList<OrderSheetItemVO>();

        sheetItems.add(item);

        return sheetItems;
    }

    @Override
    @Transactional
    public Long submitOrder(
            Long memberNo,
            List<OrderSheetItemVO> sheetItems,
            String receiverName,
            String receiverPhone,
            String address) {

        validateMemberNo(memberNo);

        if (sheetItems == null || sheetItems.isEmpty()) {
            throw new IllegalArgumentException(
                    "주문할 상품이 없습니다. 다시 시도해주세요."
            );
        }

        if (receiverName == null || receiverName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "받는 사람을 입력해주세요."
            );
        }

        if (receiverPhone == null || receiverPhone.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "연락처를 입력해주세요."
            );
        }

        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "배송지 주소를 입력해주세요."
            );
        }

        /*
         * 주문 확정 시점의 최신 상품 정보로 다시 검증한다.
         * 세션에 담겨 있던 가격/재고는 참고용일 뿐,
         * 실제 결제 금액과 재고 차감은 이 시점의 DB 값을 기준으로 한다.
         */
        List<OrderSheetItemVO> confirmedItems =
                new ArrayList<OrderSheetItemVO>();

        long totalAmount = 0L;

        for (OrderSheetItemVO requested : sheetItems) {

            OrderSheetItemVO current =
                    orderDAO.selectProductForOrder(requested.getProductNo());

            if (current == null) {
                throw new IllegalArgumentException(
                        "존재하지 않는 상품이 포함되어 있습니다."
                );
            }

            current.setQuantity(requested.getQuantity());
            current.setCartItemNo(requested.getCartItemNo());

            validateAvailable(current);

            totalAmount += current.getItemTotalPrice();

            confirmedItems.add(current);
        }

        OrderVO order = new OrderVO();

        order.setMemberNo(memberNo);
        order.setTotalAmount(totalAmount);
        order.setOrderStatus("ORDERED");
        order.setReceiverName(receiverName.trim());
        order.setReceiverPhone(receiverPhone.trim());
        order.setAddress(address.trim());

        int orderInsertResult = orderDAO.insertOrder(order);

        if (orderInsertResult <= 0 || order.getOrderNo() == null) {
            throw new IllegalStateException(
                    "주문 생성에 실패했습니다."
            );
        }

        List<Long> usedCartItemNos = new ArrayList<Long>();

        for (OrderSheetItemVO item : confirmedItems) {

            OrderItemVO orderItem = new OrderItemVO();

            orderItem.setOrderNo(order.getOrderNo());
            orderItem.setProductNo(item.getProductNo());
            orderItem.setBusinessNo(item.getBusinessNo());
            orderItem.setProductPrice(item.getDiscountPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setStatus("ORDERED");

            int orderItemInsertResult = orderDAO.insertOrderItem(orderItem);

            if (orderItemInsertResult <= 0) {
                throw new IllegalStateException(
                        "주문 상세 생성에 실패했습니다."
                );
            }

            int stockUpdateResult =
                    orderDAO.decreaseProductStock(
                            item.getProductNo(),
                            item.getQuantity()
                    );

            if (stockUpdateResult <= 0) {

                throw new IllegalArgumentException(
                        "'"
                                + item.getProductName()
                                + "' 상품의 재고가 부족합니다."
                );
            }

            if (item.getCartItemNo() != null) {
                usedCartItemNos.add(item.getCartItemNo());
            }
        }

        if (!usedCartItemNos.isEmpty()) {

            cartDAO.deleteSelectedCartItems(
                    memberNo,
                    usedCartItemNos
            );
        }

        return order.getOrderNo();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderVO> getOrderList(
            Long memberNo) {

        validateMemberNo(memberNo);

        List<OrderVO> orderList =
                orderDAO.selectOrderListByMember(memberNo);

        if (orderList == null) {
            orderList = new ArrayList<OrderVO>();
        }

        List<OrderItemVO> orderItemList =
                orderDAO.selectOrderItemListByMember(memberNo);

        if (orderItemList == null) {
            orderItemList = new ArrayList<OrderItemVO>();
        }

        Map<Long, List<OrderItemVO>> itemsByOrderNo =
                new LinkedHashMap<Long, List<OrderItemVO>>();

        for (OrderItemVO orderItem : orderItemList) {

            List<OrderItemVO> groupedItems =
                    itemsByOrderNo.get(orderItem.getOrderNo());

            if (groupedItems == null) {
                groupedItems = new ArrayList<OrderItemVO>();
                itemsByOrderNo.put(orderItem.getOrderNo(), groupedItems);
            }

            groupedItems.add(orderItem);
        }

        for (OrderVO order : orderList) {

            List<OrderItemVO> groupedItems =
                    itemsByOrderNo.get(order.getOrderNo());

            order.setItems(
                    groupedItems == null
                            ? new ArrayList<OrderItemVO>()
                            : groupedItems
            );
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
                    "주문 번호가 올바르지 않습니다."
            );
        }

        OrderVO order =
                orderDAO.selectOrderByMember(memberNo, orderNo);

        if (order == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 주문입니다."
            );
        }

        List<OrderItemVO> orderItemList =
                orderDAO.selectOrderItemListByOrderNo(orderNo);

        order.setItems(orderItemList);

        return order;
    }

    private List<Long> normalizeCartItemNos(
            List<Long> cartItemNos) {

        List<Long> normalizedList = new ArrayList<Long>();

        if (cartItemNos == null) {
            return normalizedList;
        }

        for (Long cartItemNo : cartItemNos) {

            if (cartItemNo == null || cartItemNo <= 0) {
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

        if (!"APPROVED".equals(item.getStatus())) {
            throw new IllegalArgumentException(
                    "'" + item.getProductName() + "'은(는) 현재 판매 중인 상품이 아닙니다."
            );
        }

        int stock = item.getStock() == null ? 0 : item.getStock();

        if (stock <= 0) {
            throw new IllegalArgumentException(
                    "'" + item.getProductName() + "'은(는) 품절된 상품입니다."
            );
        }

        int quantity = item.getQuantity() == null ? 0 : item.getQuantity();

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "상품 수량은 1개 이상이어야 합니다."
            );
        }

        if (quantity > stock) {
            throw new IllegalArgumentException(
                    "'"
                            + item.getProductName()
                            + "'의 현재 재고는 "
                            + stock
                            + "개입니다."
            );
        }
    }

    private void validateMemberNo(
            Long memberNo) {

        if (memberNo == null || memberNo <= 0) {
            throw new IllegalArgumentException(
                    "로그인 회원 정보가 올바르지 않습니다."
            );
        }
    }

    private void validateProductNo(
            Integer productNo) {

        if (productNo == null || productNo <= 0) {
            throw new IllegalArgumentException(
                    "상품 번호가 올바르지 않습니다."
            );
        }
    }

    private void validateQuantity(
            Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "상품 수량은 1개 이상이어야 합니다."
            );
        }
    }
}
