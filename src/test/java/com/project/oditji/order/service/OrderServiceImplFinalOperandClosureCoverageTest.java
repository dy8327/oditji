package com.project.oditji.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

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
import com.project.oditji.order.vo.OrderPaymentPrepareVO;
import com.project.oditji.order.vo.OrderSheetItemVO;
import com.project.oditji.payment.dao.PaymentDAO;
import com.project.oditji.payment.service.PaymentService;

/** 주문 서비스의 null/비양수/옵션 short-circuit 잔여 조건을 보완합니다. */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplFinalOperandClosureCoverageTest {

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
    void completionValidationShouldCoverNullPaymentIdAndNullPreparedItems() {
        OrderPaymentPrepareVO validItems = new OrderPaymentPrepareVO();
        validItems.setPaymentId("pay-null-id");
        validItems.setItems(List.of(availableItem(10, null, "GOODS")));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.completePaidOrder(1L, validItems, null));

        OrderPaymentPrepareVO nullItems = new OrderPaymentPrepareVO();
        nullItems.setPaymentId("pay-null-items");
        ReflectionTestUtils.setField(nullItems, "items", null);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.completePaidOrder(1L, nullItems, "pay-null-items"));
    }

    @Test
    void orderAndDeliveryNumberGuardsShouldCoverOppositeOrOperands() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getOrderDetail(1L, 0L));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getDeliveryDetail(1L, null));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.cancelPaidOrder(1L, null, "사유"));
    }

    @Test
    void directOrderShouldAllowClothesWhenOptionIsActuallySelected() {
        OrderSheetItemVO item = availableItem(20, 200L, "CLOTHES");
        when(orderDAO.selectProductForOrder(20, 200L)).thenReturn(item);

        List<OrderSheetItemVO> result = service.prepareDirectOrder(
                1L,
                20,
                200L,
                2);

        assertEquals(1, result.size());
        assertSame(item, result.get(0));
        assertEquals(2, item.getQuantity().intValue());
    }

    private OrderSheetItemVO availableItem(
            int productNo,
            Long optionNo,
            String productType) {

        OrderSheetItemVO item = new OrderSheetItemVO();
        item.setProductNo(productNo);
        item.setOptionNo(optionNo);
        item.setProductName("테스트 상품");
        item.setProductType(productType);
        item.setStatus("APPROVED");
        item.setStock(10);
        item.setQuantity(1);
        item.setPrice(1000);
        item.setDiscountRate(0);
        return item;
    }
}
