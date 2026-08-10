package com.project.oditji.order.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.order.service.OrderService;
import com.project.oditji.order.vo.OrderPaymentCancelRequestVO;
import com.project.oditji.order.vo.OrderPaymentCompleteRequestVO;
import com.project.oditji.order.vo.OrderSubmitRequestVO;
import com.project.oditji.refund.service.OrderCancelRefundService;

import jakarta.servlet.http.HttpSession;

/**
 * OrderController의 null/empty OR 조건의 반대 피연산자들을 보완합니다.
 */
class OrderControllerAdditionalConditionCoverageTest {

    private final Map<String, Object> sessionValues =
            new HashMap<String, Object>();

    private HttpSession session;
    private OrderController controller;

    @BeforeEach
    void setUp() {
        session = mock(HttpSession.class);

        lenient().when(session.getAttribute(anyString()))
                .thenAnswer(invocation ->
                        sessionValues.get(
                                invocation.getArgument(0)));

        controller = new OrderController(
                mock(OrderService.class),
                mock(OrderCancelRefundService.class));
    }

    @Test
    void emptyOrderSheetShouldCoverSecondSheetValidationOperand() {
        login(1L);
        sessionValues.put("orderSheet", List.of());

        assertEquals(
                "redirect:/cart",
                controller.orderSheet(
                        session,
                        new ExtendedModelMap()));

        OrderSubmitRequestVO request =
                new OrderSubmitRequestVO(
                        "수령인",
                        "010",
                        "서울");

        assertEquals(
                "주문서 정보가 만료되었습니다. 다시 주문해주세요.",
                controller.preparePayment(
                        request,
                        session)
                        .get("message"));
    }

    @Test
    void completePaymentShouldCoverNullAndBlankPaymentIdOperands() {
        login(2L);

        OrderPaymentCompleteRequestVO nullId =
                new OrderPaymentCompleteRequestVO();
        nullId.setPaymentId(null);

        assertEquals(
                "결제 ID가 없습니다.",
                controller.completePayment(
                        nullId,
                        session)
                        .get("message"));

        OrderPaymentCompleteRequestVO blankId =
                new OrderPaymentCompleteRequestVO();
        blankId.setPaymentId("   ");

        assertEquals(
                "결제 ID가 없습니다.",
                controller.completePayment(
                        blankId,
                        session)
                        .get("message"));
    }

    @Test
    void bulkCancelShouldCoverNullAndEmptyOrderItemListsSeparately() {
        login(3L);

        OrderPaymentCancelRequestVO nullList =
                new OrderPaymentCancelRequestVO();
        nullList.setOrderItemNos(null);

        assertEquals(
                "선택한 주문상품이 없습니다.",
                controller.cancelOrderItems(
                        nullList,
                        session)
                        .get("message"));

        OrderPaymentCancelRequestVO emptyList =
                new OrderPaymentCancelRequestVO();
        emptyList.setOrderItemNos(List.of());

        assertEquals(
                "선택한 주문상품이 없습니다.",
                controller.cancelOrderItems(
                        emptyList,
                        session)
                        .get("message"));
    }

    @Test
    void singleCancelShouldCoverNonNullRequestWithNullItemNumber() {
        login(4L);

        OrderPaymentCancelRequestVO request =
                new OrderPaymentCancelRequestVO();
        request.setOrderItemNo(null);

        assertEquals(
                "부분 취소 요청 정보가 없습니다.",
                controller.cancelOrderItem(
                        request,
                        session)
                        .get("message"));
    }

    private void login(Long memberNo) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        sessionValues.put("loginMember", member);
    }
}
