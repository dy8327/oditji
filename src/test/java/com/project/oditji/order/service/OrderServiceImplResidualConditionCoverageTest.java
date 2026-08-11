package com.project.oditji.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.admin.service.AdminService;
import com.project.oditji.cart.dao.CartDAO;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.order.dao.OrderDAO;
import com.project.oditji.order.vo.OrderSheetItemVO;
import com.project.oditji.payment.dao.PaymentDAO;
import com.project.oditji.payment.service.PaymentService;

/** 주문 서비스의 잔여 단락 조건과 입력 검증 helper를 보완합니다. */
class OrderServiceImplResidualConditionCoverageTest {

    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(
                mock(OrderDAO.class),
                mock(CartDAO.class),
                mock(PaymentDAO.class),
                mock(PaymentService.class),
                mock(NotificationService.class),
                mock(AdminService.class));
    }

    @Test
    void createOrderNameShouldCoverNullEmptyBlankMultipleAndLengthLimit() {
        assertEquals("ODITJI 상품 주문", invokeString("createOrderName", (Object) null));
        assertEquals("ODITJI 상품 주문", invokeString("createOrderName", List.of()));

        OrderSheetItemVO blank = item("   ", 10, 1, "APPROVED");
        assertEquals("ODITJI 상품", invokeString("createOrderName", List.of(blank)));

        OrderSheetItemVO first = item("상품A", 10, 1, "APPROVED");
        OrderSheetItemVO second = item("상품B", 10, 1, "APPROVED");
        assertEquals("상품A 외 1건", invokeString("createOrderName", List.of(first, second)));

        String longName = "가".repeat(120);
        OrderSheetItemVO longItem = item(longName, 10, 1, "APPROVED");
        String result = invokeString("createOrderName", List.of(longItem));
        assertEquals(100, result.length());
    }

    @Test
    void normalizeCancelReasonShouldCoverNullBlankNormalAndTooLong() {
        assertEquals("사용자 요청에 의한 결제 취소", invokeString("normalizeCancelReason", (Object) null));
        assertEquals("사용자 요청에 의한 결제 취소", invokeString("normalizeCancelReason", "   "));
        assertEquals("단순 변심", invokeString("normalizeCancelReason", "  단순 변심  "));

        String tooLong = "가".repeat(501);
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "normalizeCancelReason", tooLong));
    }

    @Test
    void deliveryValidationShouldCoverEveryNullAndBlankShortCircuit() {
        String validName = "홍길동";
        String validPhone = "010-1111-2222";
        String validAddress = "서울시 테스트구";
        String longName = "가".repeat(51);
        String longPhone = "1".repeat(21);
        String longAddress = "가".repeat(301);

        assertInvalidDelivery(null, validPhone, validAddress);
        assertInvalidDelivery("   ", validPhone, validAddress);
        assertInvalidDelivery(longName, validPhone, validAddress);
        assertInvalidDelivery(validName, null, validAddress);
        assertInvalidDelivery(validName, "   ", validAddress);
        assertInvalidDelivery(validName, longPhone, validAddress);
        assertInvalidDelivery(validName, validPhone, null);
        assertInvalidDelivery(validName, validPhone, "   ");
        assertInvalidDelivery(validName, validPhone, longAddress);

        ReflectionTestUtils.invokeMethod(
                service,
                "validateDeliveryInformation",
                validName,
                validPhone,
                validAddress);
    }

    @Test
    void normalizeCartItemNosShouldIgnoreInvalidAndDuplicateValues() {
        List<Long> nullResult = ReflectionTestUtils.invokeMethod(
                service,
                "normalizeCartItemNos",
                (Object) null);
        assertTrue(nullResult.isEmpty());

        List<Long> source = new java.util.ArrayList<Long>();
        source.add(null);
        source.add(0L);
        source.add(-1L);
        source.add(10L);
        source.add(10L);
        source.add(11L);

        List<Long> result = ReflectionTestUtils.invokeMethod(
                service,
                "normalizeCartItemNos",
                source);
        assertEquals(List.of(10L, 11L), result);
    }

    @Test
    void validateAvailableShouldCoverNullStatusStockQuantityAndOverStock() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "validateAvailable", (Object) null));

        OrderSheetItemVO waiting = item("대기", 10, 1, "WAITING");
        assertInvalidAvailable(waiting);

        OrderSheetItemVO nullStock = item("재고없음", null, 1, "APPROVED");
        assertInvalidAvailable(nullStock);

        OrderSheetItemVO zeroStock = item("재고0", 0, 1, "APPROVED");
        assertInvalidAvailable(zeroStock);

        OrderSheetItemVO nullQuantity = item("수량없음", 10, null, "APPROVED");
        assertInvalidAvailable(nullQuantity);

        OrderSheetItemVO zeroQuantity = item("수량0", 10, 0, "APPROVED");
        assertInvalidAvailable(zeroQuantity);

        OrderSheetItemVO overStock = item("초과", 2, 3, "APPROVED");
        assertInvalidAvailable(overStock);

        OrderSheetItemVO available = item("정상", 2, 2, "APPROVED");
        ReflectionTestUtils.invokeMethod(service, "validateAvailable", available);
        assertFalse(available.isSoldOut());
    }

    @Test
    void scalarValidatorsShouldCoverNullZeroAndPositiveValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "validateMemberNo", (Object) null));
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "validateMemberNo", 0L));
        ReflectionTestUtils.invokeMethod(service, "validateMemberNo", 1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "validateProductNo", (Object) null));
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "validateProductNo", 0));
        ReflectionTestUtils.invokeMethod(service, "validateProductNo", 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "validateQuantity", (Object) null));
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "validateQuantity", 0));
        ReflectionTestUtils.invokeMethod(service, "validateQuantity", 1);
    }

    private void assertInvalidDelivery(String name, String phone, String address) {
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateDeliveryInformation",
                        name,
                        phone,
                        address));
    }

    private void assertInvalidAvailable(OrderSheetItemVO item) {
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "validateAvailable", item));
    }

    private String invokeString(String methodName, Object argument) {
        return ReflectionTestUtils.invokeMethod(service, methodName, argument);
    }

    private OrderSheetItemVO item(String productName, Integer stock, Integer quantity, String status) {
        OrderSheetItemVO item = new OrderSheetItemVO();
        item.setProductName(productName);
        item.setStock(stock);
        item.setQuantity(quantity);
        item.setStatus(status);
        return item;
    }
}
