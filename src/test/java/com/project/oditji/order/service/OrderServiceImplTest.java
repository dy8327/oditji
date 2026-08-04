package com.project.oditji.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.admin.service.AdminService;
import com.project.oditji.cart.dao.CartDAO;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.order.dao.OrderDAO;
import com.project.oditji.order.vo.DeliveryVO;
import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.order.vo.OrderSheetItemVO;
import com.project.oditji.order.vo.OrderVO;
import com.project.oditji.payment.dao.PaymentDAO;
import com.project.oditji.payment.service.PaymentService;

/**
 * 장바구니 주문 준비, 바로 구매, 주문 조회 및 공통 검증 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderDAO orderDAO;

    @Mock
    private CartDAO cartDAO;

    @Mock
    private PaymentDAO paymentDAO;

    @Mock
    private PaymentService paymentService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AdminService adminService;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(
                orderDAO,
                cartDAO,
                paymentDAO,
                paymentService,
                notificationService,
                adminService);
    }

    @Test
    void prepareCheckoutFromCartShouldRejectInvalidMemberAndEmptySelection() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.prepareCheckoutFromCart(null, List.of(1L)));
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.prepareCheckoutFromCart(1L, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.prepareCheckoutFromCart(
                        1L,
                        java.util.Arrays.asList(null, 0L, -1L)));

        verify(orderDAO, never()).selectCartItemsForOrder(eq(1L), anyList());
    }

    @Test
    void prepareCheckoutFromCartShouldNormalizeDuplicatesAndReturnAvailableItems() {
        OrderSheetItemVO first = createAvailableItem(10, "상품1", 5, 1);
        OrderSheetItemVO second = createAvailableItem(20, "상품2", 3, 2);

        when(orderDAO.selectCartItemsForOrder(eq(1L), anyList()))
                .thenReturn(List.of(first, second));

        List<OrderSheetItemVO> result = orderService.prepareCheckoutFromCart(
                1L,
                List.of(5L, 5L, 6L));

        assertEquals(List.of(first, second), result);

        @SuppressWarnings({ "unchecked", "rawtypes" })
        ArgumentCaptor<List<Long>> captor =
                (ArgumentCaptor) ArgumentCaptor.forClass(List.class);
        verify(orderDAO).selectCartItemsForOrder(eq(1L), captor.capture());
        assertEquals(List.of(5L, 6L), captor.getValue());
    }

    @Test
    void prepareCheckoutFromCartShouldRejectMissingOrUnavailableItem() {
        when(orderDAO.selectCartItemsForOrder(1L, List.of(1L, 2L)))
                .thenReturn(List.of(createAvailableItem(10, "상품", 5, 1)));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.prepareCheckoutFromCart(1L, List.of(1L, 2L)));

        OrderSheetItemVO soldOut = createAvailableItem(10, "품절상품", 0, 1);
        when(orderDAO.selectCartItemsForOrder(1L, List.of(3L)))
                .thenReturn(List.of(soldOut));

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.prepareCheckoutFromCart(1L, List.of(3L)));
    }

    @Test
    void prepareDirectOrderShouldValidateParametersAndExistence() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.prepareDirectOrder(0L, 1, null, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.prepareDirectOrder(1L, null, null, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.prepareDirectOrder(1L, 1, null, 0));

        when(orderDAO.selectProductForOrder(10, null)).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.prepareDirectOrder(1L, 10, null, 1));
    }

    @Test
    void prepareDirectOrderShouldRequireOptionForClothesAndShoes() {
        OrderSheetItemVO clothes = createAvailableItem(10, "의상", 5, 1);
        clothes.setProductType("CLOTHES");
        clothes.setOptionNo(null);
        when(orderDAO.selectProductForOrder(10, null)).thenReturn(clothes);

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.prepareDirectOrder(1L, 10, null, 1));

        OrderSheetItemVO shoes = createAvailableItem(11, "신발", 5, 1);
        shoes.setProductType("SHOES");
        shoes.setOptionNo(null);
        when(orderDAO.selectProductForOrder(11, null)).thenReturn(shoes);

        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.prepareDirectOrder(1L, 11, null, 1));
    }

    @Test
    void prepareDirectOrderShouldSetQuantityAndClearCartItemNumber() {
        OrderSheetItemVO item = createAvailableItem(12, "굿즈", 10, 1);
        item.setCartItemNo(99L);
        item.setProductType("GOODS");
        when(orderDAO.selectProductForOrder(12, null)).thenReturn(item);

        List<OrderSheetItemVO> result = orderService.prepareDirectOrder(
                1L,
                12,
                null,
                3);

        assertEquals(1, result.size());
        assertSame(item, result.get(0));
        assertEquals(3, item.getQuantity().intValue());
        assertNull(item.getCartItemNo());
    }

    @Test
    void getOrderListShouldValidateRangeAndConvertNullToEmptyList() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.getOrderList(null, 1, 10));
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.getOrderList(1L, 0, 10));
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.getOrderList(1L, 10, 9));

        when(orderDAO.selectOrderListByMember(1L, 1, 10)).thenReturn(null);
        assertTrue(orderService.getOrderList(1L, 1, 10).isEmpty());
    }

    @Test
    void getOrderListShouldAttachItemsAndReplaceNullItemList() {
        OrderVO first = new OrderVO();
        first.setOrderNo(100L);
        OrderVO second = new OrderVO();
        second.setOrderNo(200L);
        OrderItemVO item = new OrderItemVO();

        when(orderDAO.selectOrderListByMember(1L, 1, 10))
                .thenReturn(List.of(first, second));
        when(orderDAO.selectOrderItemListByOrderNo(100L))
                .thenReturn(List.of(item));
        when(orderDAO.selectOrderItemListByOrderNo(200L))
                .thenReturn(null);

        List<OrderVO> result = orderService.getOrderList(1L, 1, 10);

        assertEquals(2, result.size());
        assertEquals(List.of(item), first.getItems());
        assertTrue(second.getItems().isEmpty());
    }

    @Test
    void getOrderCountShouldValidateAndDelegate() {
        when(orderDAO.countOrderListByMember(1L)).thenReturn(7);

        assertEquals(7, orderService.getOrderCount(1L));
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.getOrderCount(0L));
    }

    @Test
    void getOrderDetailShouldValidateOwnershipAndAttachItems() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.getOrderDetail(1L, null));

        when(orderDAO.selectOrderByMember(1L, 10L)).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.getOrderDetail(1L, 10L));

        OrderVO order = new OrderVO();
        order.setOrderNo(11L);
        List<OrderItemVO> items = List.of(new OrderItemVO());
        when(orderDAO.selectOrderByMember(1L, 11L)).thenReturn(order);
        when(orderDAO.selectOrderItemListByOrderNo(11L)).thenReturn(items);

        assertSame(order, orderService.getOrderDetail(1L, 11L));
        assertSame(items, order.getItems());
    }

    @Test
    void getDeliveryDetailShouldValidateAndReturnOwnedDelivery() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.getDeliveryDetail(1L, 0L));

        when(orderDAO.selectDeliveryDetailByMember(1L, 10L)).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> orderService.getDeliveryDetail(1L, 10L));

        DeliveryVO delivery = new DeliveryVO();
        when(orderDAO.selectDeliveryDetailByMember(1L, 11L)).thenReturn(delivery);
        assertSame(delivery, orderService.getDeliveryDetail(1L, 11L));
    }

    @Test
    void createPaymentIdShouldUseExpectedPrefixAndLength() {
        String first = invokePrivate(
                "createPaymentId",
                new Class<?>[0]);
        String second = invokePrivate(
                "createPaymentId",
                new Class<?>[0]);

        assertTrue(first.startsWith("ODT_"));
        assertEquals(36, first.length());
        assertFalse(first.equals(second));
    }

    @Test
    void createOrderNameShouldHandleEmptyMultipleBlankAndLongNames() {
        assertEquals(
                "ODITJI 상품 주문",
                invokePrivate(
                        "createOrderName",
                        new Class<?>[] { List.class },
                        (Object) null));

        OrderSheetItemVO blank = new OrderSheetItemVO();
        blank.setProductName(" ");
        assertEquals(
                "ODITJI 상품",
                invokePrivate(
                        "createOrderName",
                        new Class<?>[] { List.class },
                        List.of(blank)));

        OrderSheetItemVO first = new OrderSheetItemVO();
        first.setProductName("상품A");
        assertEquals(
                "상품A 외 1건",
                invokePrivate(
                        "createOrderName",
                        new Class<?>[] { List.class },
                        List.of(first, new OrderSheetItemVO())));

        first.setProductName("가".repeat(120));
        String truncated = invokePrivate(
                "createOrderName",
                new Class<?>[] { List.class },
                List.of(first));
        assertEquals(100, truncated.length());
    }

    @Test
    void normalizeCancelReasonShouldUseDefaultTrimAndLengthLimit() {
        assertEquals(
                "사용자 요청에 의한 결제 취소",
                invokePrivate(
                        "normalizeCancelReason",
                        new Class<?>[] { String.class },
                        (Object) null));
        assertEquals(
                "단순 변심",
                invokePrivate(
                        "normalizeCancelReason",
                        new Class<?>[] { String.class },
                        " 단순 변심 "));
        assertThrows(
                IllegalArgumentException.class,
                () -> invokePrivate(
                        "normalizeCancelReason",
                        new Class<?>[] { String.class },
                        "가".repeat(501)));
    }

    @Test
    void deliveryInformationValidationShouldRejectBlankAndLongValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> invokePrivate(
                        "validateDeliveryInformation",
                        new Class<?>[] { String.class, String.class, String.class },
                        " ", "010", "주소"));
        assertThrows(
                IllegalArgumentException.class,
                () -> invokePrivate(
                        "validateDeliveryInformation",
                        new Class<?>[] { String.class, String.class, String.class },
                        "가".repeat(51), "010", "주소"));
        assertThrows(
                IllegalArgumentException.class,
                () -> invokePrivate(
                        "validateDeliveryInformation",
                        new Class<?>[] { String.class, String.class, String.class },
                        "홍길동", " ", "주소"));
        assertThrows(
                IllegalArgumentException.class,
                () -> invokePrivate(
                        "validateDeliveryInformation",
                        new Class<?>[] { String.class, String.class, String.class },
                        "홍길동", "0".repeat(21), "주소"));
        assertThrows(
                IllegalArgumentException.class,
                () -> invokePrivate(
                        "validateDeliveryInformation",
                        new Class<?>[] { String.class, String.class, String.class },
                        "홍길동", "010", " "));
        assertThrows(
                IllegalArgumentException.class,
                () -> invokePrivate(
                        "validateDeliveryInformation",
                        new Class<?>[] { String.class, String.class, String.class },
                        "홍길동", "010", "가".repeat(301)));

        invokePrivate(
                "validateDeliveryInformation",
                new Class<?>[] { String.class, String.class, String.class },
                "홍길동", "010-1234-5678", "서울시");
    }

    @Test
    void normalizeCartItemNosShouldRemoveInvalidAndDuplicateNumbers() {
        assertTrue(((List<?>) invokePrivate(
                "normalizeCartItemNos",
                new Class<?>[] { List.class },
                (Object) null)).isEmpty());

        List<Long> result = invokePrivate(
                "normalizeCartItemNos",
                new Class<?>[] { List.class },
                java.util.Arrays.asList(null, -1L, 0L, 1L, 1L, 2L));

        assertEquals(List.of(1L, 2L), result);
    }

    @Test
    void validateAvailableShouldCoverEveryFailureCondition() {
        assertThrows(
                IllegalArgumentException.class,
                () -> invokePrivate(
                        "validateAvailable",
                        new Class<?>[] { OrderSheetItemVO.class },
                        (Object) null));

        OrderSheetItemVO waiting = createAvailableItem(1, "대기", 5, 1);
        waiting.setStatus("WAITING");
        assertThrows(
                IllegalArgumentException.class,
                () -> invokePrivate(
                        "validateAvailable",
                        new Class<?>[] { OrderSheetItemVO.class },
                        waiting));

        OrderSheetItemVO noStock = createAvailableItem(1, "품절", 0, 1);
        assertThrows(
                IllegalArgumentException.class,
                () -> invokePrivate(
                        "validateAvailable",
                        new Class<?>[] { OrderSheetItemVO.class },
                        noStock));

        OrderSheetItemVO noQuantity = createAvailableItem(1, "수량", 5, 0);
        assertThrows(
                IllegalArgumentException.class,
                () -> invokePrivate(
                        "validateAvailable",
                        new Class<?>[] { OrderSheetItemVO.class },
                        noQuantity));

        OrderSheetItemVO overStock = createAvailableItem(1, "재고초과", 2, 3);
        assertThrows(
                IllegalArgumentException.class,
                () -> invokePrivate(
                        "validateAvailable",
                        new Class<?>[] { OrderSheetItemVO.class },
                        overStock));

        invokePrivate(
                "validateAvailable",
                new Class<?>[] { OrderSheetItemVO.class },
                createAvailableItem(1, "정상", 5, 2));
    }

    private OrderSheetItemVO createAvailableItem(
            int productNo,
            String productName,
            int stock,
            int quantity) {

        OrderSheetItemVO item = new OrderSheetItemVO();
        item.setProductNo(productNo);
        item.setProductName(productName);
        item.setStatus("APPROVED");
        item.setStock(stock);
        item.setQuantity(quantity);
        item.setPrice(1000);
        item.setDiscountRate(0);
        return item;
    }

    @SuppressWarnings("unchecked")
    private <T> T invokePrivate(
            String methodName,
            Class<?>[] parameterTypes,
            Object... arguments) {

        try {
            Method method = OrderServiceImpl.class.getDeclaredMethod(
                    methodName,
                    parameterTypes);
            method.setAccessible(true);
            return (T) method.invoke(orderService, arguments);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException(cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
