package com.project.oditji.order.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.admin.service.AdminService;
import com.project.oditji.cart.dao.CartDAO;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.order.dao.OrderDAO;
import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.order.vo.OrderPaymentPrepareVO;
import com.project.oditji.order.vo.OrderSheetItemVO;
import com.project.oditji.order.vo.OrderVO;
import com.project.oditji.payment.dao.PaymentDAO;
import com.project.oditji.payment.service.PaymentService;
import com.project.oditji.payment.vo.PaymentVO;

/**
 * 기존 주문 테스트 이후 남은 저장 실패와 결제 취소 상태 분기를 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplAdditionalRemainingCoverageTest {

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

    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(
                orderDAO,
                cartDAO,
                paymentDAO,
                paymentService,
                notificationService,
                adminService);
    }

    @Test
    void checkoutShouldRejectNullDaoResult() {
        when(orderDAO.selectCartItemsForOrder(
                1L,
                List.of(1L)))
                .thenReturn(null);

        List<Long> cartItemNoList =
                List.of(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.prepareCheckoutFromCart(
                        1L,
                        cartItemNoList));
    }

    @Test
    void createPaidOrderShouldRejectZeroInsertAndMissingGeneratedNumber() {
        OrderPaymentPrepareVO prepare = prepare();

        when(orderDAO.insertOrder(any(OrderVO.class)))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> invokePrivate(
                        "createPaidOrder",
                        1L,
                        prepare));

        when(orderDAO.insertOrder(any(OrderVO.class)))
                .thenReturn(1);

        assertThrows(
                IllegalStateException.class,
                () -> invokePrivate(
                        "createPaidOrder",
                        1L,
                        prepare));
    }

    @Test
    void saveOrderItemsShouldRejectFailedDetailInsert() {
        OrderSheetItemVO item = item(
                null,
                null);

        when(orderDAO.insertOrderItem(any(OrderItemVO.class)))
                .thenReturn(0);

        List<OrderSheetItemVO> itemList =
                List.of(item);
        List<Long> orderItemNoList =
                new ArrayList<Long>();
        List<Long> usedCartItemNoList =
                new ArrayList<Long>();

        assertThrows(
                IllegalStateException.class,
                () -> invokePrivate(
                        "saveOrderItems",
                        10L,
                        itemList,
                        orderItemNoList,
                        usedCartItemNoList));
    }

    @Test
    void optionStockFailureShouldNotDecreaseProductStock() {
        OrderSheetItemVO item = item(
                50L,
                null);

        when(orderDAO.decreaseProductOptionStock(
                50L,
                1))
                .thenReturn(0);

        assertThrows(
                IllegalArgumentException.class,
                () -> invokePrivate(
                        "decreaseProductStock",
                        item));

        verify(orderDAO, never())
                .decreaseProductStock(
                        item.getProductNo(),
                        item.getQuantity());
    }

    @Test
    void paymentSaveShouldRejectZeroInsertAndMissingGeneratedPaymentNumber() {
        OrderPaymentPrepareVO prepare = prepare();

        PaymentVO first = new PaymentVO();
        when(paymentDAO.insertPayment(first))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> invokePrivate(
                        "savePayment",
                        10L,
                        prepare,
                        first));

        PaymentVO second = new PaymentVO();
        when(paymentDAO.insertPayment(second))
                .thenReturn(1);

        assertThrows(
                IllegalStateException.class,
                () -> invokePrivate(
                        "savePayment",
                        10L,
                        prepare,
                        second));
    }

    @Test
    void waitingSettlementShouldRejectFailedInsert() {
        when(orderDAO.insertWaitingSettlement(100L))
                .thenReturn(0);

        List<Long> orderItemNoList =
                List.of(100L);

        assertThrows(
                IllegalStateException.class,
                () -> invokePrivate(
                        "createWaitingSettlements",
                        orderItemNoList));
    }

    @Test
    void deleteUsedCartItemsShouldSkipEmptyList() {
        invokePrivate(
                "deleteUsedCartItems",
                1L,
                List.of());

        verify(cartDAO, never())
                .deleteSelectedCartItems(
                        any(),
                        any());
    }

    @Test
    void orderDetailShouldPreserveNullItemListFromDao() {
        OrderVO order = new OrderVO();
        order.setOrderNo(70L);

        when(orderDAO.selectOrderByMember(
                1L,
                70L))
                .thenReturn(order);
        when(orderDAO.selectOrderItemListByOrderNo(70L))
                .thenReturn(null);

        OrderVO result =
                service.getOrderDetail(
                        1L,
                        70L);

        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void cancelShouldRejectCanceledAndNonPaidPaymentStatuses() {
        OrderVO canceledPaymentOrder =
                order(80L);
        PaymentVO canceledPayment =
                payment("CANCELED");

        when(orderDAO.selectOrderByMember(
                1L,
                80L))
                .thenReturn(canceledPaymentOrder);
        when(paymentDAO.selectPaymentByOrderNo(80L))
                .thenReturn(canceledPayment);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.cancelPaidOrder(
                        1L,
                        80L,
                        "사유"));

        OrderVO readyPaymentOrder =
                order(81L);
        PaymentVO readyPayment =
                payment("READY");

        when(orderDAO.selectOrderByMember(
                1L,
                81L))
                .thenReturn(readyPaymentOrder);
        when(paymentDAO.selectPaymentByOrderNo(81L))
                .thenReturn(readyPayment);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.cancelPaidOrder(
                        1L,
                        81L,
                        "사유"));
    }

    @Test
    void cancelShouldCoverRemainingDatabaseUpdateFailures() {
        OrderVO firstOrder = order(90L);
        PaymentVO firstPayment = payment("PAID");

        when(orderDAO.selectOrderByMember(1L, 90L))
                .thenReturn(firstOrder);
        when(paymentDAO.selectPaymentByOrderNo(90L))
                .thenReturn(firstPayment);
        when(paymentService.cancelPaidPayment(
                firstPayment,
                "사유"))
                .thenReturn(firstPayment);
        when(orderDAO.restoreProductStockByOrderNo(90L))
                .thenReturn(1);
        when(orderDAO.updateOrderItemsCanceled(90L))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.cancelPaidOrder(
                        1L,
                        90L,
                        "사유"));

        OrderVO secondOrder = order(91L);
        PaymentVO secondPayment = payment("PAID");

        when(orderDAO.selectOrderByMember(1L, 91L))
                .thenReturn(secondOrder);
        when(paymentDAO.selectPaymentByOrderNo(91L))
                .thenReturn(secondPayment);
        when(paymentService.cancelPaidPayment(
                secondPayment,
                "사유"))
                .thenReturn(secondPayment);
        when(orderDAO.restoreProductStockByOrderNo(91L))
                .thenReturn(1);
        when(orderDAO.updateOrderItemsCanceled(91L))
                .thenReturn(1);
        when(orderDAO.updateOrderCanceled(1L, 91L))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.cancelPaidOrder(
                        1L,
                        91L,
                        "사유"));

        OrderVO thirdOrder = order(92L);
        PaymentVO thirdPayment = payment("PAID");

        when(orderDAO.selectOrderByMember(1L, 92L))
                .thenReturn(thirdOrder);
        when(paymentDAO.selectPaymentByOrderNo(92L))
                .thenReturn(thirdPayment);
        when(paymentService.cancelPaidPayment(
                thirdPayment,
                "사유"))
                .thenReturn(thirdPayment);
        when(orderDAO.restoreProductStockByOrderNo(92L))
                .thenReturn(1);
        when(orderDAO.updateOrderItemsCanceled(92L))
                .thenReturn(1);
        when(orderDAO.updateOrderCanceled(1L, 92L))
                .thenReturn(1);
        when(paymentDAO.updatePaymentCanceled(thirdPayment))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.cancelPaidOrder(
                        1L,
                        92L,
                        "사유"));
    }

    private void invokePrivate(
            String methodName,
            Object... arguments) {

        ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
    }

    private OrderSheetItemVO item(
            Long optionNo,
            Long cartItemNo) {

        OrderSheetItemVO item =
                new OrderSheetItemVO();
        item.setProductNo(10);
        item.setOptionNo(optionNo);
        item.setBusinessNo(5);
        item.setProductName("상품");
        item.setProductType("GOODS");
        item.setStatus("APPROVED");
        item.setStock(10);
        item.setQuantity(1);
        item.setPrice(1000);
        item.setDiscountRate(0);
        item.setCartItemNo(cartItemNo);
        return item;
    }

    private OrderPaymentPrepareVO prepare() {
        OrderPaymentPrepareVO prepare =
                new OrderPaymentPrepareVO();
        prepare.setPaymentId("pay");
        prepare.setOrderName("주문");
        prepare.setTotalAmount(1000L);
        prepare.setReceiverName("홍길동");
        prepare.setReceiverPhone("010");
        prepare.setAddress("서울");
        return prepare;
    }

    private OrderVO order(Long orderNo) {
        OrderVO order = new OrderVO();
        order.setOrderNo(orderNo);
        order.setOrderStatus("PAID");
        return order;
    }

    private PaymentVO payment(
            String status) {

        PaymentVO payment =
                new PaymentVO();
        payment.setPaymentStatus(status);
        return payment;
    }
}
