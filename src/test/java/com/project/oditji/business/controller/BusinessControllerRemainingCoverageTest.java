package com.project.oditji.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
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
import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.EventFormVO;
import com.project.oditji.business.vo.EventManageVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.refund.service.OrderCancelRefundService;

/**
 * BusinessController에서 기존 테스트가 지나가지 못한 예외/반대 조건 분기를 보완합니다.
 *
 * 최신 기준: oditji0810_1113AM.zip
 */
@ExtendWith(MockitoExtension.class)
class BusinessControllerRemainingCoverageTest {

        @Mock
        private BusinessService businessService;

        @Mock
        private OrderCancelRefundService orderCancelRefundService;

        private BusinessController controller;

        @BeforeEach
        void setUp() {
                controller = new BusinessController(businessService, orderCancelRefundService);
        }

        @Test
        void productRegisterProcessShouldCoverDeniedUnapprovedPositiveActorAndUnexpectedFailure() {
                GoodsManageVO guestForm = new GoodsManageVO();
                RedirectAttributesModelMap guestRedirect = new RedirectAttributesModelMap();

                assertEquals(
                                "redirect:/member/login",
                                controller.productRegisterProcess(
                                                guestForm,
                                                null,
                                                null,
                                                new MockHttpSession(),
                                                guestRedirect));

                when(businessService.getBusinessByMemberNo(101L)).thenReturn(null);
                assertEquals(
                                "redirect:/",
                                controller.productRegisterProcess(
                                                new GoodsManageVO(),
                                                null,
                                                null,
                                                session(101L),
                                                new RedirectAttributesModelMap()));

                when(businessService.getBusinessByMemberNo(102L))
                                .thenReturn(business(202L, "WAITING"));
                assertEquals(
                                "redirect:/business/main",
                                controller.productRegisterProcess(
                                                new GoodsManageVO(),
                                                null,
                                                null,
                                                session(102L),
                                                new RedirectAttributesModelMap()));

                BusinessVO approved = business(203L, "APPROVED");
                when(businessService.getBusinessByMemberNo(103L)).thenReturn(approved);

                GoodsManageVO positiveActorForm = new GoodsManageVO();
                positiveActorForm.setActorNo(7L);
                when(businessService.registerProduct(positiveActorForm, null, null)).thenReturn(303L);
                RedirectAttributesModelMap successRedirect = new RedirectAttributesModelMap();

                assertEquals(
                                "redirect:/business/product/list",
                                controller.productRegisterProcess(
                                                positiveActorForm,
                                                null,
                                                null,
                                                session(103L),
                                                successRedirect));
                assertEquals(7L, positiveActorForm.getActorNo());
                assertEquals(203L, positiveActorForm.getBusinessNo());

                GoodsManageVO nullActorForm = new GoodsManageVO();
                when(businessService.registerProduct(nullActorForm, null, null))
                                .thenThrow(new RuntimeException("unexpected"));
                RedirectAttributesModelMap failureRedirect = new RedirectAttributesModelMap();

                assertEquals(
                                "redirect:/business/product/register",
                                controller.productRegisterProcess(
                                                nullActorForm,
                                                null,
                                                null,
                                                session(103L),
                                                failureRedirect));
                assertNull(nullActorForm.getActorNo());
                assertEquals(
                                "상품 등록 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                                failureRedirect.getFlashAttributes().get("errorMessage"));
                assertSame(nullActorForm, failureRedirect.getFlashAttributes().get("productForm"));
        }

        @Test
        void productUpdateShouldCoverPositiveActorAndUnexpectedFailure() {
                when(businessService.getBusinessByMemberNo(110L))
                                .thenReturn(business(210L, "APPROVED"));

                GoodsManageVO positiveActorForm = new GoodsManageVO();
                positiveActorForm.setProductNo(1L);
                positiveActorForm.setActorNo(9L);
                RedirectAttributesModelMap successRedirect = new RedirectAttributesModelMap();

                assertEquals(
                                "redirect:/business/product/list",
                                controller.productUpdateProcess(
                                                positiveActorForm,
                                                null,
                                                null,
                                                true,
                                                new String[] { "/old/detail.jpg" },
                                                session(110L),
                                                successRedirect));
                assertEquals(9L, positiveActorForm.getActorNo());
                verify(businessService).updateProduct(
                                positiveActorForm,
                                null,
                                null,
                                true,
                                new String[] { "/old/detail.jpg" });

                GoodsManageVO nullActorForm = new GoodsManageVO();
                nullActorForm.setProductNo(2L);
                doThrow(new RuntimeException("unexpected"))
                                .when(businessService)
                                .updateProduct(
                                                eq(nullActorForm),
                                                isNull(),
                                                isNull(),
                                                eq(false),
                                                isNull());
                RedirectAttributesModelMap failureRedirect = new RedirectAttributesModelMap();

                assertEquals(
                                "redirect:/business/product/list",
                                controller.productUpdateProcess(
                                                nullActorForm,
                                                null,
                                                null,
                                                false,
                                                null,
                                                session(110L),
                                                failureRedirect));
                assertEquals(
                                "상품 수정 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                                failureRedirect.getFlashAttributes().get("errorMessage"));
        }

        @Test
        void productDeleteShouldCoverDeniedAccessAndUnexpectedFailure() {
                assertEquals(
                                "redirect:/member/login",
                                controller.productDeleteProcess(
                                                1L,
                                                "사유",
                                                new MockHttpSession(),
                                                new RedirectAttributesModelMap()));

                when(businessService.getBusinessByMemberNo(120L)).thenReturn(null);
                assertEquals(
                                "redirect:/",
                                controller.productDeleteProcess(
                                                2L,
                                                "사유",
                                                session(120L),
                                                new RedirectAttributesModelMap()));

                when(businessService.getBusinessByMemberNo(121L))
                                .thenReturn(business(221L, "APPROVED"));
                doThrow(new RuntimeException("unexpected"))
                                .when(businessService)
                                .requestProductDelete(3L, 221L, "삭제");
                RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

                assertEquals(
                                "redirect:/business/product/list",
                                controller.productDeleteProcess(
                                                3L,
                                                "삭제",
                                                session(121L),
                                                redirect));
                assertEquals(
                                "상품 삭제 요청 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                                redirect.getFlashAttributes().get("errorMessage"));
        }

        @Test
        void eventScreensShouldCoverDeniedAccessAndUnapprovedRegistration() {
                assertEquals(
                                "redirect:/member/login",
                                controller.eventList(
                                                null,
                                                1,
                                                new MockHttpSession(),
                                                new ExtendedModelMap(),
                                                new RedirectAttributesModelMap()));

                when(businessService.getBusinessByMemberNo(130L)).thenReturn(null);
                assertEquals(
                                "redirect:/",
                                controller.eventRegister(
                                                session(130L),
                                                new ExtendedModelMap(),
                                                new RedirectAttributesModelMap()));

                when(businessService.getBusinessByMemberNo(131L))
                                .thenReturn(business(231L, "REJECTED"));
                RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/main",
                                controller.eventRegister(
                                                session(131L),
                                                new ExtendedModelMap(),
                                                redirect));
                assertEquals(
                                "승인된 사업자만 이벤트 등록을 요청할 수 있습니다.",
                                redirect.getFlashAttributes().get("errorMessage"));
        }

        @Test
        void eventRegisterShouldCoverDeniedUnapprovedNullNegativeAndUnexpectedBranches() {
                EventFormVO form = eventForm(null);

                assertEquals(
                                "redirect:/member/login",
                                controller.eventRegisterProcess(
                                                form,
                                                null,
                                                new MockHttpSession(),
                                                new RedirectAttributesModelMap()));

                when(businessService.getBusinessByMemberNo(140L))
                                .thenReturn(business(240L, "WAITING"));
                assertEquals(
                                "redirect:/business/main",
                                controller.eventRegisterProcess(
                                                form,
                                                null,
                                                session(140L),
                                                new RedirectAttributesModelMap()));

                when(businessService.getBusinessByMemberNo(141L))
                                .thenReturn(business(241L, "APPROVED"));
                EventFormVO negativeRateForm = eventForm(List.of(-1));
                RedirectAttributesModelMap invalidRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/event/register",
                                controller.eventRegisterProcess(
                                                negativeRateForm,
                                                null,
                                                session(141L),
                                                invalidRedirect));
                assertEquals(
                                "이벤트 할인율은 0~100 사이로 입력해주세요.",
                                invalidRedirect.getFlashAttributes().get("errorMessage"));

                EventFormVO nullRatesForm = eventForm(null);
                when(businessService.registerEvent(any(EventManageVO.class), isNull()))
                                .thenThrow(new RuntimeException("unexpected"));
                RedirectAttributesModelMap failureRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/event/register",
                                controller.eventRegisterProcess(
                                                nullRatesForm,
                                                null,
                                                session(141L),
                                                failureRedirect));
                assertEquals(
                                "이벤트 등록 요청 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                                failureRedirect.getFlashAttributes().get("errorMessage"));
        }

        @Test
        void eventUpdateAndExtendShouldCoverDeniedAndUnexpectedBranches() {
                EventFormVO validForm = eventForm(List.of(10));

                assertEquals(
                                "redirect:/member/login",
                                controller.eventUpdateProcess(
                                                500L,
                                                validForm,
                                                null,
                                                new MockHttpSession(),
                                                new RedirectAttributesModelMap()));

                when(businessService.getBusinessByMemberNo(150L))
                                .thenReturn(business(250L, "APPROVED"));
                doThrow(new RuntimeException("unexpected"))
                                .when(businessService)
                                .updateApprovedEvent(any(EventManageVO.class), isNull());
                RedirectAttributesModelMap updateRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/event/list",
                                controller.eventUpdateProcess(
                                                501L,
                                                validForm,
                                                null,
                                                session(150L),
                                                updateRedirect));
                assertEquals(
                                "이벤트 수정 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                                updateRedirect.getFlashAttributes().get("errorMessage"));

                when(businessService.getBusinessByMemberNo(151L)).thenReturn(null);
                assertEquals(
                                "redirect:/",
                                controller.eventExtendProcess(
                                                502L,
                                                LocalDate.of(2026, Month.SEPTEMBER, 30),
                                                "연장",
                                                session(151L),
                                                new RedirectAttributesModelMap()));

                when(businessService.getBusinessByMemberNo(152L))
                                .thenReturn(business(252L, "APPROVED"));
                LocalDate extendDate = LocalDate.of(2026, Month.OCTOBER, 31);
                doThrow(new RuntimeException("unexpected"))
                                .when(businessService)
                                .extendApprovedEvent(502L, 252L, extendDate, "예외 연장");
                RedirectAttributesModelMap extendRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/event/list",
                                controller.eventExtendProcess(
                                                502L,
                                                extendDate,
                                                "예외 연장",
                                                session(152L),
                                                extendRedirect));
                assertEquals(
                                "이벤트 연장 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                                extendRedirect.getFlashAttributes().get("errorMessage"));
        }

        @Test
        void settlementActionsShouldCoverMissingBusinessDomainAndAccountBranches() {
                when(businessService.getBusinessByMemberNo(160L)).thenReturn(null);
                assertEquals(
                                "redirect:/member/login",
                                controller.settlement(
                                                session(160L),
                                                new ExtendedModelMap(),
                                                new RedirectAttributesModelMap()));

                assertEquals(
                                "redirect:/member/login",
                                controller.requestSettlement(
                                                new MockHttpSession(),
                                                new RedirectAttributesModelMap()));

                when(businessService.getBusinessByMemberNo(161L))
                                .thenReturn(business(261L, "APPROVED"));
                doThrow(new IllegalStateException("정산할 금액이 없습니다."))
                                .when(businessService)
                                .requestSettlementConfirmation(261L);
                RedirectAttributesModelMap requestRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/settlement/main",
                                controller.requestSettlement(session(161L), requestRedirect));
                assertEquals(
                                "정산할 금액이 없습니다.",
                                requestRedirect.getFlashAttributes().get("errorMessage"));

                assertEquals(
                                "redirect:/member/login",
                                controller.updateSettlementAccount(
                                                "은행",
                                                "123",
                                                "예금주",
                                                new MockHttpSession(),
                                                new RedirectAttributesModelMap()));

                when(businessService.getBusinessByMemberNo(162L))
                                .thenReturn(business(262L, "APPROVED"));
                RedirectAttributesModelMap successRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/settlement/account",
                                controller.updateSettlementAccount(
                                                "국민은행",
                                                "111-222",
                                                "사업자",
                                                session(162L),
                                                successRedirect));
                assertEquals(
                                "정산 계좌 정보가 저장되었습니다.",
                                successRedirect.getFlashAttributes().get("successMessage"));

                doThrow(new IllegalArgumentException("계좌 오류"))
                                .when(businessService)
                                .updateSettlementAccount(262L, "", "", "");
                RedirectAttributesModelMap failureRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/settlement/account",
                                controller.updateSettlementAccount(
                                                "",
                                                "",
                                                "",
                                                session(162L),
                                                failureRedirect));
                assertEquals(
                                "계좌 오류",
                                failureRedirect.getFlashAttributes().get("errorMessage"));
        }

        @Test
        void salesShouldCoverDefaultPartialDateDeniedAndValidationBranches() {
                when(businessService.getBusinessByMemberNo(170L))
                                .thenReturn(business(270L, "APPROVED"));

                LocalDate today = LocalDate.now(com.project.oditji.common.util.DateTimeUtil.KOREA_ZONE);
                LocalDate monthStart = today.withDayOfMonth(1);
                when(businessService.getBusinessSalesHistoryCount(eq(270L), any(LocalDate.class), any(LocalDate.class)))
                                .thenReturn(0);
                ExtendedModelMap defaultModel = new ExtendedModelMap();
                assertEquals(
                                "business/settlement/salesStatus",
                                controller.sales(
                                                null,
                                                null,
                                                1,
                                                session(170L),
                                                defaultModel,
                                                new RedirectAttributesModelMap()));
                assertEquals(monthStart, defaultModel.get("startDate"));
                assertEquals(today, defaultModel.get("endDate"));

                LocalDate explicitStart = LocalDate.of(2026, Month.JULY, 1);
                ExtendedModelMap partialModel = new ExtendedModelMap();
                assertEquals(
                                "business/settlement/salesStatus",
                                controller.sales(
                                                explicitStart,
                                                null,
                                                1,
                                                session(170L),
                                                partialModel,
                                                new RedirectAttributesModelMap()));
                assertEquals(explicitStart, partialModel.get("startDate"));
                assertEquals(today, partialModel.get("endDate"));

                assertEquals(
                                "redirect:/member/login",
                                controller.sales(
                                                null,
                                                null,
                                                1,
                                                new MockHttpSession(),
                                                new ExtendedModelMap(),
                                                new RedirectAttributesModelMap()));

                LocalDate invalidStart = LocalDate.of(2026, Month.AUGUST, 20);
                LocalDate invalidEnd = LocalDate.of(2026, Month.AUGUST, 1);
                when(businessService.getBusinessSalesStatus(270L, invalidStart, invalidEnd))
                                .thenThrow(new IllegalArgumentException("조회 기간 오류"));
                RedirectAttributesModelMap invalidRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/settlement/sales",
                                controller.sales(
                                                invalidStart,
                                                invalidEnd,
                                                1,
                                                session(170L),
                                                new ExtendedModelMap(),
                                                invalidRedirect));
                assertEquals(
                                "조회 기간 오류",
                                invalidRedirect.getFlashAttributes().get("errorMessage"));
        }

        @Test
        void orderAndDeliveryListsShouldCoverDeniedAndValidationBranches() {
                assertEquals(
                                "redirect:/member/login",
                                controller.orderList(
                                                1,
                                                new MockHttpSession(),
                                                new ExtendedModelMap(),
                                                new RedirectAttributesModelMap()));

                when(businessService.getBusinessByMemberNo(180L)).thenReturn(null);
                assertEquals(
                                "redirect:/",
                                controller.deliveryList(
                                                null,
                                                null,
                                                1,
                                                session(180L),
                                                new ExtendedModelMap(),
                                                new RedirectAttributesModelMap()));

                when(businessService.getBusinessByMemberNo(181L))
                                .thenReturn(business(281L, "APPROVED"));
                when(businessService.getBusinessDeliveryListCount(281L, "INVALID", null))
                                .thenThrow(new IllegalArgumentException("배송 상태 오류"));
                RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/delivery/list",
                                controller.deliveryList(
                                                "INVALID",
                                                null,
                                                1,
                                                session(181L),
                                                new ExtendedModelMap(),
                                                redirect));
                assertEquals(
                                "배송 상태 오류",
                                redirect.getFlashAttributes().get("errorMessage"));
        }

        @Test
        void updateDeliveryShouldCoverDeniedDomainUnexpectedAndEmptyReturnFilters() {
                assertEquals(
                                "redirect:/member/login",
                                controller.updateDelivery(
                                                1L,
                                                null,
                                                null,
                                                "PREPARING",
                                                null,
                                                null,
                                                1,
                                                new MockHttpSession(),
                                                new RedirectAttributesModelMap()));

                when(businessService.getBusinessByMemberNo(190L)).thenReturn(null);
                assertEquals(
                                "redirect:/",
                                controller.updateDelivery(
                                                2L,
                                                null,
                                                null,
                                                "PREPARING",
                                                null,
                                                null,
                                                1,
                                                session(190L),
                                                new RedirectAttributesModelMap()));

                when(businessService.getBusinessByMemberNo(191L))
                                .thenReturn(business(291L, "APPROVED"));
                doThrow(new IllegalArgumentException("운송장 오류"))
                                .when(businessService)
                                .updateBusinessDelivery(291L, 3L, null, null, "SHIPPING");
                RedirectAttributesModelMap domainRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/delivery/list",
                                controller.updateDelivery(
                                                3L,
                                                null,
                                                null,
                                                "SHIPPING",
                                                " ",
                                                " ",
                                                1,
                                                session(191L),
                                                domainRedirect));
                assertEquals(
                                "운송장 오류",
                                domainRedirect.getFlashAttributes().get("errorMessage"));
                assertNull(domainRedirect.get("status"));
                assertNull(domainRedirect.get("keyword"));
                assertNull(domainRedirect.get("page"));

                doThrow(new RuntimeException("unexpected"))
                                .when(businessService)
                                .updateBusinessDelivery(291L, 4L, "택배", "123", "DELIVERED");
                RedirectAttributesModelMap unexpectedRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/delivery/list",
                                controller.updateDelivery(
                                                4L,
                                                "택배",
                                                "123",
                                                "DELIVERED",
                                                null,
                                                null,
                                                1,
                                                session(191L),
                                                unexpectedRedirect));
                assertEquals(
                                "배송 정보 저장 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                                unexpectedRedirect.getFlashAttributes().get("errorMessage"));
        }

        @Test
        void cancelListShouldCoverAnonymousAndIllegalArgumentBranches() {
                RedirectAttributesModelMap guestRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/member/login",
                                controller.cancelList(
                                                null,
                                                1,
                                                new MockHttpSession(),
                                                new ExtendedModelMap(),
                                                guestRedirect));
                assertEquals(
                                "로그인 회원 정보를 확인할 수 없습니다.",
                                guestRedirect.getFlashAttributes().get("errorMessage"));

                when(orderCancelRefundService.getBusinessCancelListCount(200L, "INVALID"))
                                .thenThrow(new IllegalArgumentException("취소 상태 오류"));
                RedirectAttributesModelMap invalidRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/main",
                                controller.cancelList(
                                                "INVALID",
                                                1,
                                                session(200L),
                                                new ExtendedModelMap(),
                                                invalidRedirect));
                assertEquals(
                                "취소 상태 오류",
                                invalidRedirect.getFlashAttributes().get("errorMessage"));
        }

        @Test
        void approveCancelShouldCoverDomainUnexpectedAndEmptyReturnConditions() {
                doThrow(new IllegalStateException("승인 불가"))
                                .when(orderCancelRefundService)
                                .approveCancel(210L, 1L);
                RedirectAttributesModelMap domainRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/cancel/list",
                                controller.approveCancel(
                                                1L,
                                                " ",
                                                1,
                                                session(210L),
                                                domainRedirect));
                assertEquals(
                                "승인 불가",
                                domainRedirect.getFlashAttributes().get("errorMessage"));
                assertNull(domainRedirect.get("status"));
                assertNull(domainRedirect.get("page"));

                doThrow(new RuntimeException("unexpected"))
                                .when(orderCancelRefundService)
                                .approveCancel(211L, 2L);
                RedirectAttributesModelMap unexpectedRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/cancel/list",
                                controller.approveCancel(
                                                2L,
                                                null,
                                                1,
                                                session(211L),
                                                unexpectedRedirect));
                assertEquals(
                                "취소 요청 승인 처리 중 오류가 발생했습니다.",
                                unexpectedRedirect.getFlashAttributes().get("errorMessage"));
        }

        @Test
        void rejectCancelShouldCoverSuccessUnexpectedAndEmptyReturnConditions() {
                RedirectAttributesModelMap successRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/cancel/list",
                                controller.rejectCancel(
                                                3L,
                                                "반려 사유",
                                                null,
                                                1,
                                                session(220L),
                                                successRedirect));
                verify(orderCancelRefundService).rejectCancel(220L, 3L, "반려 사유");
                assertEquals(
                                "취소 요청을 반려했습니다.",
                                successRedirect.getFlashAttributes().get("successMessage"));
                assertNull(successRedirect.get("status"));
                assertNull(successRedirect.get("page"));

                doThrow(new RuntimeException("unexpected"))
                                .when(orderCancelRefundService)
                                .rejectCancel(221L, 4L, "예외");
                RedirectAttributesModelMap unexpectedRedirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/business/cancel/list",
                                controller.rejectCancel(
                                                4L,
                                                "예외",
                                                "",
                                                1,
                                                session(221L),
                                                unexpectedRedirect));
                assertEquals(
                                "취소 요청 반려 처리 중 오류가 발생했습니다.",
                                unexpectedRedirect.getFlashAttributes().get("errorMessage"));
                assertNull(unexpectedRedirect.get("status"));
        }

        @Test
        void contentApisShouldCoverNonNullCollections() {
                List<ContentSearchVO> contents = List.of(new ContentSearchVO());
                List<ActorSearchVO> actors = List.of(new ActorSearchVO());
                when(businessService.getContentList("ok")).thenReturn(contents);
                when(businessService.getActorPreview(900L, "TV")).thenReturn(actors);

                assertSame(contents, controller.contentListApi("ok"));
                assertSame(actors, controller.contentActorPreviewApi(900L, "TV"));
        }

        @Test
        void unsupportedSessionValuesAndZeroMemberObjectShouldRemainAnonymous() {
                MockHttpSession unsupportedSession = new MockHttpSession();
                Object unsupportedLoginMemberNo = new Object();
                Object unsupportedMemberNo = new Object();
                unsupportedSession.setAttribute("loginMemberNo", unsupportedLoginMemberNo);
                unsupportedSession.setAttribute("memberNo", unsupportedMemberNo);
                MemberVO zeroMember = new MemberVO();
                zeroMember.setMemberNo(0L);
                unsupportedSession.setAttribute("loginMember", zeroMember);

                RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
                assertEquals(
                                "redirect:/member/login",
                                controller.businessMain(
                                                unsupportedSession,
                                                new ExtendedModelMap(),
                                                redirect));
                assertSame(unsupportedLoginMemberNo, unsupportedSession.getAttribute("loginMemberNo"));
                assertSame(unsupportedMemberNo, unsupportedSession.getAttribute("memberNo"));
                assertEquals(
                                "로그인 회원 정보를 확인할 수 없습니다. 다시 로그인해주세요.",
                                redirect.getFlashAttributes().get("errorMessage"));
        }

        @Test
        void invalidLoginMemberNumberShouldFallbackToLegacyPositiveNumber() {
                MockHttpSession session = new MockHttpSession();
                session.setAttribute("loginMemberNo", "invalid");
                session.setAttribute("memberNo", 230);
                when(businessService.getBusinessByMemberNo(230L)).thenReturn(null);

                assertEquals(
                                "redirect:/",
                                controller.productList(
                                                null,
                                                1,
                                                session,
                                                new ExtendedModelMap(),
                                                new RedirectAttributesModelMap()));
                assertEquals(230L, session.getAttribute("loginMemberNo"));
        }

        private EventFormVO eventForm(List<Integer> discountRates) {
                EventFormVO form = new EventFormVO();
                form.setEventTitle("테스트 이벤트");
                form.setDescription("설명");
                form.setStartDate(LocalDate.of(2026, Month.AUGUST, 10));
                form.setEndDate(LocalDate.of(2026, Month.AUGUST, 31));
                form.setProductNoList(List.of(1L));
                form.setDiscountRateList(discountRates);
                return form;
        }

        private BusinessVO business(long businessNo, String status) {
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
