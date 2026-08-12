package com.project.oditji.order.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.common.vo.PageVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.order.service.OrderService;
import com.project.oditji.order.vo.OrderVO;
import com.project.oditji.refund.service.OrderCancelRefundService;
import com.project.oditji.refund.vo.OrderCancelRefundVO;

import jakarta.servlet.http.HttpSession;

/** 주문 목록의 이전/다음 페이지가 모두 존재하는 페이지 블록 분기를 검증합니다. */
class OrderControllerPaginationBranchCoverageTest {

    @Test
    void orderListShouldExposePrevAndNextWhenCurrentBlockIsInMiddle() {
        OrderService orderService = mock(OrderService.class);
        OrderCancelRefundService refundService =
                mock(OrderCancelRefundService.class);
        OrderController controller =
                new OrderController(orderService, refundService);

        HttpSession session = mock(HttpSession.class);
        MemberVO loginMember = new MemberVO();
        loginMember.setMemberNo(77L);
        when(session.getAttribute("loginMember")).thenReturn(loginMember);

        /* 40건 / 3건 = 14페이지. 6페이지는 6~10 블록이라 prev/next가 모두 true입니다. */
        when(orderService.getOrderCount(77L)).thenReturn(40);
        when(orderService.getOrderList(77L, 16, 18))
                .thenReturn(List.of(new OrderVO()));
        when(refundService.getMemberCancelRefundHistory(
                eq(77L),
                eq("ALL"),
                eq("ALL"),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull()))
                .thenReturn(List.<OrderCancelRefundVO>of());

        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals(
                "order/orderList",
                controller.orderList(
                        6,
                        1,
                        "ALL",
                        "ALL",
                        null,
                        null,
                        "order",
                        session,
                        model));

        PageVO pageVO = (PageVO) model.get("pageVO");
        assertEquals(6, pageVO.getCurrentPage());
        assertEquals(6, pageVO.getStartPage());
        assertEquals(10, pageVO.getEndPage());
        assertTrue(pageVO.isPrev());
        assertTrue(pageVO.isNext());
    }
}
