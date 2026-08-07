package com.project.oditji.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.DeliveryManageVO;
import com.project.oditji.business.vo.EventFormVO;
import com.project.oditji.business.vo.EventManageVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.business.vo.SettlementManageVO;
import com.project.oditji.order.vo.OrderVO;
import com.project.oditji.refund.service.OrderCancelRefundService;

/** 화면에 남아 있던 BusinessController의 상품·이벤트·배송·취소 분기를 보완합니다. */
@ExtendWith(MockitoExtension.class)
class BusinessControllerAdditionalCoverageTest {

        @Mock
        private BusinessService businessService;

        @Mock
        private OrderCancelRefundService orderCancelRefundService;

        private BusinessController controller;

        @BeforeEach
        void setUp() {
                controller = new BusinessController(
                                businessService,
                                orderCancelRefundService);
        }

        @Test
        void productUpdateShouldCoverAccessApprovalSuccessAndFailures() {
                GoodsManageVO form = new GoodsManageVO();
                form.setProductNo(100L);
                form.setActorNo(0L);

                RedirectAttributesModelMap guestRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/member/login",
                                controller.productUpdateProcess(
                                                form,
                                                null,
                                                null,
                                                new MockHttpSession(),
                                                guestRedirect));

                when(businessService.getBusinessByMemberNo(10L)).thenReturn(null);
                assertEquals(
                                "redirect:/",
                                controller.productUpdateProcess(
                                                form,
                                                null,
                                                null,
                                                session(10L),
                                                new RedirectAttributesModelMap()));

                when(businessService.getBusinessByMemberNo(11L))
                                .thenReturn(business(20L, "WAITING"));
                assertEquals(
                                "redirect:/business/main",
                                controller.productUpdateProcess(
                                                form,
                                                null,
                                                null,
                                                session(11L),
                                                new RedirectAttributesModelMap()));

                when(businessService.getBusinessByMemberNo(12L))
                                .thenReturn(business(21L, "APPROVED"));
                RedirectAttributesModelMap successRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/product/list",
                                controller.productUpdateProcess(
                                                form,
                                                null,
                                                null,
                                                session(12L),
                                                successRedirect));
                assertEquals(21L, form.getBusinessNo());
                assertNull(form.getActorNo());
                assertTrue(successRedirect.getFlashAttributes()
                                .get("successMessage")
                                .toString()
                                .contains("상품 수정 요청"));

                doThrow(new IllegalArgumentException("수정 검증 실패"))
                                .when(businessService)
                                .updateProduct(any(GoodsManageVO.class), any(), any());
                RedirectAttributesModelMap domainRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/product/list",
                                controller.productUpdateProcess(
                                                form,
                                                null,
                                                null,
                                                session(12L),
                                                domainRedirect));
                assertEquals(
                                "수정 검증 실패",
                                domainRedirect.getFlashAttributes().get("errorMessage"));
        }

        @Test
        void productDeleteShouldCoverApprovalSuccessAndExceptionResponses() {
                when(businessService.getBusinessByMemberNo(20L))
                                .thenReturn(business(30L, "WAITING"));
                assertEquals(
                                "redirect:/business/main",
                                controller.productDeleteProcess(
                                                1L,
                                                "사유",
                                                session(20L),
                                                new RedirectAttributesModelMap()));
                verify(businessService, never())
                                .requestProductDelete(anyLong(), anyLong(), any());

                when(businessService.getBusinessByMemberNo(21L))
                                .thenReturn(business(31L, "APPROVED"));
                RedirectAttributesModelMap successRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/product/list",
                                controller.productDeleteProcess(
                                                2L,
                                                " 판매 종료 ",
                                                session(21L),
                                                successRedirect));
                verify(businessService).requestProductDelete(2L, 31L, " 판매 종료 ");

                doThrow(new IllegalStateException("이미 삭제 요청 중입니다."))
                                .when(businessService)
                                .requestProductDelete(3L, 31L, "중복");
                RedirectAttributesModelMap failureRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/product/list",
                                controller.productDeleteProcess(
                                                3L,
                                                "중복",
                                                session(21L),
                                                failureRedirect));
                assertEquals(
                                "이미 삭제 요청 중입니다.",
                                failureRedirect.getFlashAttributes().get("errorMessage"));
        }

        @Test
        void eventListAndRegisterScreenShouldLoadBusinessOwnedData() {
                BusinessVO business = business(40L, "APPROVED");
                when(businessService.getBusinessByMemberNo(30L)).thenReturn(business);

                EventManageVO approved = new EventManageVO();
                approved.setEventNo(100L);
                approved.setStatus("APPROVED");
                EventManageVO waiting = new EventManageVO();
                waiting.setEventNo(101L);
                waiting.setStatus("WAITING");
                EventManageVO detail = new EventManageVO();
                detail.setDescription("상세 설명");
                detail.setConnectedProducts(List.of());
                List<GoodsManageVO> products = List.of(new GoodsManageVO());

                when(businessService.getEventListCountByBusinessNo(40L, " 여름 "))
                                .thenReturn(2);
                when(businessService.getEventListByBusinessNo(40L, " 여름 ", 1, 10))
                                .thenReturn(List.of(approved, waiting));
                when(businessService.getApprovedEventForBusiness(100L, 40L))
                                .thenReturn(detail);
                when(businessService.getApprovedProductListByBusinessNo(40L))
                                .thenReturn(products);

                ExtendedModelMap listModel = new ExtendedModelMap();
                assertEquals(
                                "business/event/eventList",
                                controller.eventList(
                                                " 여름 ",
                                                1,
                                                session(30L),
                                                listModel,
                                                new RedirectAttributesModelMap()));
                assertEquals("상세 설명", approved.getDescription());
                assertEquals(products, listModel.get("productList"));
                verify(businessService, never())
                                .getApprovedEventForBusiness(101L, 40L);

                ExtendedModelMap registerModel = new ExtendedModelMap();
                assertEquals(
                                "business/event/eventRegister",
                                controller.eventRegister(
                                                session(30L),
                                                registerModel,
                                                new RedirectAttributesModelMap()));
                assertSame(business, registerModel.get("business"));
                assertEquals(products, registerModel.get("productList"));
        }

        @Test
        void eventRegisterShouldValidateDiscountAndMapFormForService() {
                when(businessService.getBusinessByMemberNo(31L))
                                .thenReturn(business(41L, "APPROVED"));

                EventFormVO invalid = eventForm(List.of(1L), List.of(101));
                RedirectAttributesModelMap invalidRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/event/register",
                                controller.eventRegisterProcess(
                                                invalid,
                                                null,
                                                session(31L),
                                                invalidRedirect));
                verify(businessService, never()).registerEvent(any(), any());

                EventFormVO valid = eventForm(List.of(1L, 2L), List.of(10, 20));
                when(businessService.registerEvent(any(EventManageVO.class), any()))
                                .thenAnswer(invocation -> {
                                        EventManageVO event = invocation.getArgument(0);
                                        assertEquals(41L, event.getBusinessNo());
                                        assertEquals("이벤트명", event.getTitle());
                                        assertEquals("WAITING", event.getStatus());
                                        assertEquals(List.of(1L, 2L), event.getProductNoList());
                                        return 500L;
                                });
                RedirectAttributesModelMap successRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/event/list",
                                controller.eventRegisterProcess(
                                                valid,
                                                null,
                                                session(31L),
                                                successRedirect));
                assertEquals(
                                500L,
                                successRedirect.getFlashAttributes().get("registeredEventNo"));

                doThrow(new IllegalArgumentException("이벤트 검증 실패"))
                                .when(businessService)
                                .registerEvent(any(EventManageVO.class), any());
                RedirectAttributesModelMap failureRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/event/register",
                                controller.eventRegisterProcess(
                                                valid,
                                                null,
                                                session(31L),
                                                failureRedirect));
                assertEquals(
                                "이벤트 검증 실패",
                                failureRedirect.getFlashAttributes().get("errorMessage"));
        }

        @Test
        void eventUpdateAndExtendShouldUseCommonResultHandling() {
                when(businessService.getBusinessByMemberNo(32L))
                                .thenReturn(business(42L, "APPROVED"));

                EventFormVO invalid = eventForm(
                                List.of(1L),
                                java.util.Arrays.asList((Integer) null));
                assertEquals(
                                "redirect:/business/event/list",
                                controller.eventUpdateProcess(
                                                700L,
                                                invalid,
                                                null,
                                                session(32L),
                                                new RedirectAttributesModelMap()));
                verify(businessService, never()).updateApprovedEvent(any(), any());

                EventFormVO valid = eventForm(List.of(1L), List.of(30));
                RedirectAttributesModelMap updateRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/event/list",
                                controller.eventUpdateProcess(
                                                700L,
                                                valid,
                                                null,
                                                session(32L),
                                                updateRedirect));
                assertTrue(updateRedirect.getFlashAttributes()
                                .get("successMessage")
                                .toString()
                                .contains("이벤트 수정 요청"));

                LocalDate extendedEndDate = LocalDate.of(2026, Month.SEPTEMBER, 30);
                doThrow(new IllegalStateException("연장 불가"))
                                .when(businessService)
                                .extendApprovedEvent(
                                                700L,
                                                42L,
                                                extendedEndDate,
                                                "기간 연장");
                RedirectAttributesModelMap extendRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/event/list",
                                controller.eventExtendProcess(
                                                700L,
                                                extendedEndDate,
                                                "기간 연장",
                                                session(32L),
                                                extendRedirect));
                assertEquals(
                                "연장 불가",
                                extendRedirect.getFlashAttributes().get("errorMessage"));
        }

        @Test
        void settlementSalesChatAndOrderShouldPopulateModels() {
                BusinessVO business = business(50L, "APPROVED");
                when(businessService.getBusinessByMemberNo(40L)).thenReturn(business);
                SettlementManageVO summary = new SettlementManageVO();
                SettlementManageVO account = new SettlementManageVO();
                when(businessService.getMonthlySettlementSummary(50L)).thenReturn(summary);
                when(businessService.getSettlementAccount(50L)).thenReturn(account);

                ExtendedModelMap settlementModel = new ExtendedModelMap();
                assertEquals(
                                "business/settlement/settlementMain",
                                controller.settlement(
                                                session(40L),
                                                settlementModel,
                                                new RedirectAttributesModelMap()));
                assertSame(summary, settlementModel.get("settlementSummary"));
                assertSame(account, settlementModel.get("settlementAccount"));

                RedirectAttributesModelMap requestRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/settlement/main",
                                controller.requestSettlement(session(40L), requestRedirect));
                verify(businessService).requestSettlementConfirmation(50L);

                LocalDate start = LocalDate.of(2026, Month.AUGUST, 1);
                LocalDate end = LocalDate.of(2026, Month.AUGUST, 6);
                SettlementManageVO salesStatus = new SettlementManageVO();
                List<SettlementManageVO> history = List.of(new SettlementManageVO());
                when(businessService.getBusinessSalesStatus(50L, start, end))
                                .thenReturn(salesStatus);
                when(businessService.getBusinessSalesHistoryCount(50L, start, end))
                                .thenReturn(1);
                when(businessService.getBusinessSalesHistory(50L, start, end, 1, 10))
                                .thenReturn(history);
                ExtendedModelMap salesModel = new ExtendedModelMap();
                assertEquals(
                                "business/settlement/salesStatus",
                                controller.sales(
                                                start,
                                                end,
                                                1,
                                                session(40L),
                                                salesModel,
                                                new RedirectAttributesModelMap()));
                assertSame(salesStatus, salesModel.get("salesStatus"));
                assertEquals(history, salesModel.get("salesHistory"));

                ExtendedModelMap chatModel = new ExtendedModelMap();
                assertEquals("business/community/businessChat", controller.chat(chatModel));
                assertEquals("chat", chatModel.get("activeMenu"));

                List<OrderVO> orders = List.of(new OrderVO());
                when(businessService.getBusinessOrderListCount(50L)).thenReturn(1);
                when(businessService.getBusinessOrderList(50L, 1, 10)).thenReturn(orders);
                ExtendedModelMap orderModel = new ExtendedModelMap();
                assertEquals(
                                "business/order/orderList",
                                controller.orderList(
                                                1,
                                                session(40L),
                                                orderModel,
                                                new RedirectAttributesModelMap()));
                assertEquals(orders, orderModel.get("orderList"));
        }

        @Test
        void deliveryShouldRetainStatusKeywordAndPageAfterUpdate() {
                when(businessService.getBusinessByMemberNo(50L))
                                .thenReturn(business(60L, "APPROVED"));
                List<DeliveryManageVO> deliveries = List.of(new DeliveryManageVO());
                when(businessService.getBusinessDeliveryListCount(60L, "SHIPPING", "상품"))
                                .thenReturn(1);
                when(businessService.getBusinessDeliveryList(60L, "SHIPPING", "상품", 1, 5))
                                .thenReturn(deliveries);

                ExtendedModelMap model = new ExtendedModelMap();
                assertEquals(
                                "business/order/deliveryList",
                                controller.deliveryList(
                                                "SHIPPING",
                                                "상품",
                                                1,
                                                session(50L),
                                                model,
                                                new RedirectAttributesModelMap()));
                assertEquals(deliveries, model.get("deliveryList"));
                assertEquals("SHIPPING", model.get("selectedStatus"));

                RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/delivery/list",
                                controller.updateDelivery(
                                                100L,
                                                "CJ대한통운",
                                                "123456",
                                                "DELIVERED",
                                                "SHIPPING",
                                                "상품",
                                                3,
                                                session(50L),
                                                redirect));
                verify(businessService).updateBusinessDelivery(
                                60L,
                                100L,
                                "CJ대한통운",
                                "123456",
                                "DELIVERED");
                assertEquals("SHIPPING", redirect.get("status"));
                assertEquals("상품", redirect.get("keyword"));
                assertEquals("3", redirect.get("page"));
        }

        @Test
        void cancelListApproveAndRejectShouldRetainFilterAndHandleErrors() {
                when(orderCancelRefundService.getBusinessCancelListCount(70L, "WAITING"))
                                .thenReturn(1);
                when(orderCancelRefundService.getBusinessCancelList(70L, "WAITING", 1, 5))
                                .thenReturn(List.of());
                ExtendedModelMap model = new ExtendedModelMap();
                assertEquals(
                                "business/order/cancelList",
                                controller.cancelList(
                                                "WAITING",
                                                1,
                                                session(70L),
                                                model,
                                                new RedirectAttributesModelMap()));

                RedirectAttributesModelMap approveRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/cancel/list",
                                controller.approveCancel(
                                                900L,
                                                "WAITING",
                                                2,
                                                session(70L),
                                                approveRedirect));
                verify(orderCancelRefundService).approveCancel(70L, 900L);
                assertEquals("WAITING", approveRedirect.get("status"));
                assertEquals("2", approveRedirect.get("page"));

                doThrow(new IllegalArgumentException("반려 사유 오류"))
                                .when(orderCancelRefundService)
                                .rejectCancel(70L, 901L, " ");
                RedirectAttributesModelMap rejectRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/cancel/list",
                                controller.rejectCancel(
                                                901L,
                                                " ",
                                                "WAITING",
                                                4,
                                                session(70L),
                                                rejectRedirect));
                assertEquals(
                                "반려 사유 오류",
                                rejectRedirect.getFlashAttributes().get("errorMessage"));
                assertEquals("WAITING", rejectRedirect.get("status"));
                assertEquals("4", rejectRedirect.get("page"));
        }

        private EventFormVO eventForm(
                        List<Long> productNos,
                        List<Integer> discountRates) {
                EventFormVO form = new EventFormVO();
                form.setEventTitle("이벤트명");
                form.setDescription("설명");
                form.setStartDate(LocalDate.of(2026, Month.AUGUST, 10));
                form.setEndDate(LocalDate.of(2026, Month.AUGUST, 31));
                form.setProductNoList(productNos);
                form.setDiscountRateList(discountRates);
                return form;
        }

        private BusinessVO business(Long businessNo, String status) {
                BusinessVO business = new BusinessVO();
                business.setBusinessNo(businessNo);
                business.setStatus(status);
                return business;
        }

        private MockHttpSession session(long memberNo) {
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("loginMemberNo", memberNo);
                return session;
        }
}