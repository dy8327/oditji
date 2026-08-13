package com.project.oditji.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.EventFormVO;
import com.project.oditji.business.vo.EventManageVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.refund.service.OrderCancelRefundService;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.servlet.http.HttpSession;

/** 사업자 컨트롤러 예외 처리의 WARN/ERROR 로그 비활성 분기를 묶어서 보완합니다. */
class BusinessControllerLoggingGuardCoverageTest {

    private BusinessService businessService;
    private OrderCancelRefundService orderCancelRefundService;
    private BusinessController controller;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        businessService = mock(BusinessService.class);
        orderCancelRefundService = mock(OrderCancelRefundService.class);
        controller = new BusinessController(
                businessService,
                orderCancelRefundService);

        session = mock(HttpSession.class);
        when(session.getAttribute("loginMemberNo")).thenReturn(1L);

        BusinessVO business = new BusinessVO();
        business.setBusinessNo(10L);
        business.setStatus("APPROVED");
        when(businessService.getBusinessByMemberNo(1L)).thenReturn(business);
    }

    @Test
    void productFailurePathsShouldCoverDisabledErrorLogGuards() {
        GoodsManageVO registerForm = new GoodsManageVO();
        GoodsManageVO updateForm = new GoodsManageVO();
        updateForm.setProductNo(20L);

        when(businessService.registerProduct(
                any(GoodsManageVO.class),
                isNull(),
                isNull()))
                .thenThrow(new RuntimeException("register boom"));
        doThrow(new RuntimeException("update boom"))
                .when(businessService)
                .updateProduct(
                        any(GoodsManageVO.class),
                        isNull(),
                        isNull(),
                        org.mockito.ArgumentMatchers.eq(false),
                        isNull());
        doThrow(new RuntimeException("delete boom"))
                .when(businessService)
                .requestProductDelete(30L, 10L, "reason");

        withLoggingDisabled(() -> {
            assertEquals(
                    "redirect:/business/product/register",
                    controller.productRegisterProcess(
                            registerForm,
                            null,
                            null,
                            session,
                            new RedirectAttributesModelMap()));
            assertEquals(
                    "redirect:/business/product/list",
                    controller.productUpdateProcess(
                            updateForm,
                            null,
                            null,
                            false,
                            null,
                            session,
                            new RedirectAttributesModelMap()));
            assertEquals(
                    "redirect:/business/product/list",
                    controller.productDeleteProcess(
                            30L,
                            "reason",
                            session,
                            new RedirectAttributesModelMap()));
        });
    }

    @Test
    void eventFailurePathsShouldCoverDisabledWarnAndErrorLogGuards() {
        EventFormVO form = new EventFormVO();
        form.setEventTitle("이벤트");
        form.setDiscountRateList(List.of(10));

        when(businessService.registerEvent(
                any(EventManageVO.class),
                isNull()))
                .thenThrow(new IllegalArgumentException("invalid event"))
                .thenThrow(new RuntimeException("event boom"));
        doThrow(new RuntimeException("update event boom"))
                .when(businessService)
                .updateApprovedEvent(any(EventManageVO.class), isNull());

        withLoggingDisabled(() -> {
            assertEquals(
                    "redirect:/business/event/register",
                    controller.eventRegisterProcess(
                            form,
                            null,
                            session,
                            new RedirectAttributesModelMap()));
            assertEquals(
                    "redirect:/business/event/register",
                    controller.eventRegisterProcess(
                            form,
                            null,
                            session,
                            new RedirectAttributesModelMap()));
            assertEquals(
                    "redirect:/business/event/list",
                    controller.eventUpdateProcess(
                            40L,
                            form,
                            null,
                            session,
                            new RedirectAttributesModelMap()));
        });
    }

    @Test
    void deliveryAndCancelFailurePathsShouldCoverDisabledErrorLogGuards() {
        doThrow(new RuntimeException("delivery boom"))
                .when(businessService)
                .updateBusinessDelivery(
                        10L,
                        50L,
                        "courier",
                        "tracking",
                        "SHIPPING");
        doThrow(new RuntimeException("approve boom"))
                .when(orderCancelRefundService)
                .approveCancel(1L, 60L);
        doThrow(new RuntimeException("reject boom"))
                .when(orderCancelRefundService)
                .rejectCancel(1L, 61L, "reject reason");

        withLoggingDisabled(() -> {
            assertEquals(
                    "redirect:/business/delivery/list",
                    controller.updateDelivery(
                            50L,
                            "courier",
                            "tracking",
                            "SHIPPING",
                            null,
                            null,
                            1,
                            session,
                            new RedirectAttributesModelMap()));
            assertEquals(
                    "redirect:/business/cancel/list",
                    controller.approveCancel(
                            60L,
                            null,
                            1,
                            session,
                            new RedirectAttributesModelMap()));
            assertEquals(
                    "redirect:/business/cancel/list",
                    controller.rejectCancel(
                            61L,
                            "reject reason",
                            null,
                            1,
                            session,
                            new RedirectAttributesModelMap()));
        });
    }

    private void withLoggingDisabled(Runnable action) {
        Logger logger = (Logger) LoggerFactory.getLogger(BusinessController.class);
        Level originalLevel = logger.getLevel();

        try {
            logger.setLevel(Level.OFF);
            action.run();
        } finally {
            logger.setLevel(originalLevel);
        }
    }
}
