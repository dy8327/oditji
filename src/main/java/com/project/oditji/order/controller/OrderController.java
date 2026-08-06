package com.project.oditji.order.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.oditji.common.util.ApiResponseUtil;
import com.project.oditji.common.util.DateTimeUtil;
import com.project.oditji.common.util.LoginMemberUtil;
import com.project.oditji.refund.service.OrderCancelRefundService;
import com.project.oditji.common.vo.PageVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.order.service.OrderService;
import com.project.oditji.order.vo.DeliveryVO;
import com.project.oditji.order.vo.OrderCheckoutRequestVO;
import com.project.oditji.order.vo.OrderDirectRequestVO;
import com.project.oditji.order.vo.OrderPaymentCancelRequestVO;
import com.project.oditji.order.vo.OrderPaymentCompleteRequestVO;
import com.project.oditji.order.vo.OrderPaymentPrepareVO;
import com.project.oditji.order.vo.OrderSheetItemVO;
import com.project.oditji.order.vo.OrderSubmitRequestVO;
import com.project.oditji.order.vo.OrderVO;

import jakarta.servlet.http.HttpSession;

/**
 * 주문 및 결제 요청을 처리하는 웹 컨트롤러 클래스.
 * 주문서 작성, 결제 준비/완료/취소, 주문 내역 및 상세 화면 조회를 담당한다.
 */
@Controller
@RequestMapping("/order")
public class OrderController {

        private static final String ORDER_SHEET_SESSION_KEY = "orderSheet";
        private static final String PAYMENT_PREPARE_SESSION_KEY = "orderPaymentPrepare";
        private static final String RESPONSE_REDIRECT_URL = "redirectUrl";
        private static final String ORDER_LIST_URL = "/order/list";
        private static final Logger log = LoggerFactory.getLogger(OrderController.class);

        private final OrderService orderService;
        private final OrderCancelRefundService orderCancelRefundService;

        @Value("${portone.store-id}")
        private String storeId;

        @Value("${portone.payment.channel-key}")
        private String paymentChannelKey;

        // [포트원 테스트 채널 부분 취소 제한 화면 전달 추가]
        @Value("${portone.payment.test-mode:true}")
        private boolean portOneTestMode;

        // 의존성 주입을 위한 생성자.

        public OrderController(OrderService orderService, OrderCancelRefundService orderCancelRefundService) {

                this.orderService = orderService;
                this.orderCancelRefundService = orderCancelRefundService;
        }

        // 장바구니에서 선택한 상품으로 주문서 작성 준비
        @PostMapping("/checkout")
        @ResponseBody
        public Map<String, Object> checkoutFromCart(@RequestBody OrderCheckoutRequestVO requestVO,
                        HttpSession session) {

                return prepareOrderSheet(
                                session,
                                memberNo -> orderService.prepareCheckoutFromCart(
                                                memberNo,
                                                requestVO.getCartItemNos()),
                                "장바구니 주문서 작성 중 오류");
        }

        // 상품 상세 바로 구매 주문서 작성 준비
        @PostMapping("/direct")
        @ResponseBody
        public Map<String, Object> directOrder(@RequestBody OrderDirectRequestVO requestVO, HttpSession session) {

                return prepareOrderSheet(
                                session,
                                memberNo -> orderService.prepareDirectOrder(
                                                memberNo,
                                                requestVO.getProductNo(),
                                                requestVO.getOptionNo(),
                                                requestVO.getQuantity()),
                                "바로 구매 주문서 작성 중 오류");
        }

        // 주문서 화면
        @GetMapping
        public String orderSheet(HttpSession session, Model model) {

                MemberVO loginMember = LoginMemberUtil.getLoginMember(session);

                if (loginMember == null) {
                        return "redirect:/member/login?redirect=/order";
                }

                @SuppressWarnings("unchecked")
                List<OrderSheetItemVO> sheetItems = (List<OrderSheetItemVO>) session
                                .getAttribute(ORDER_SHEET_SESSION_KEY);

                if (sheetItems == null || sheetItems.isEmpty()) {

                        return "redirect:/cart";
                }

                long totalPrice = 0L;

                for (OrderSheetItemVO item : sheetItems) {
                        totalPrice += item.getItemTotalPrice();
                }

                model.addAttribute("orderItems", sheetItems);
                model.addAttribute("totalPrice", totalPrice);
                model.addAttribute("defaultReceiverName", loginMember.getMemberName());
                model.addAttribute("defaultReceiverPhone", loginMember.getPhone());
                model.addAttribute("defaultReceiverEmail", loginMember.getEmail());

                return "order/order";
        }

        // 포트원 결제창 호출 전 결제 준비
        @PostMapping("/payment/prepare")
        @ResponseBody
        public Map<String, Object> preparePayment(@RequestBody OrderSubmitRequestVO requestVO, HttpSession session) {

                MemberVO loginMember = LoginMemberUtil.getLoginMember(session);

                if (loginMember == null) {
                        return ApiResponseUtil.loginRequired();
                }

                @SuppressWarnings("unchecked")
                List<OrderSheetItemVO> sheetItems = (List<OrderSheetItemVO>) session
                                .getAttribute(ORDER_SHEET_SESSION_KEY);

                if (sheetItems == null || sheetItems.isEmpty()) {

                        return ApiResponseUtil.failure("주문서 정보가 만료되었습니다. " + "다시 주문해주세요.");
                }

                try {
                        OrderPaymentPrepareVO prepareVO = orderService.preparePayment(
                                        loginMember.getMemberNo(),
                                        sheetItems,
                                        requestVO.receiverName(),
                                        requestVO.receiverPhone(),
                                        requestVO.address());

                        prepareVO.setStoreId(storeId);
                        prepareVO.setChannelKey(paymentChannelKey);
                        session.setAttribute(PAYMENT_PREPARE_SESSION_KEY, prepareVO);

                        Map<String, Object> response = ApiResponseUtil.success("결제 준비가 완료되었습니다.");

                        response.put("storeId", prepareVO.getStoreId());
                        response.put("channelKey", prepareVO.getChannelKey());
                        response.put("paymentId", prepareVO.getPaymentId());
                        response.put("orderName", prepareVO.getOrderName());
                        response.put("totalAmount", prepareVO.getTotalAmount());

                        return response;

                } catch (IllegalArgumentException e) {

                        return ApiResponseUtil.failure(e.getMessage());

                } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                                log.error("결제 준비 중 오류", e);
                        }

                        return ApiResponseUtil.failure("결제 준비 중 오류가 발생했습니다.");
                }
        }

        // 포트원 결제 완료 후 서버 검증 및 주문 확정
        @PostMapping("/payment/complete")
        @ResponseBody
        public Map<String, Object> completePayment(@RequestBody OrderPaymentCompleteRequestVO requestVO,
                        HttpSession session) {

                MemberVO loginMember = LoginMemberUtil.getLoginMember(session);

                if (loginMember == null) {
                        return ApiResponseUtil.loginRequired();
                }

                if (requestVO == null || requestVO.getPaymentId() == null || requestVO.getPaymentId().isBlank()) {

                        return ApiResponseUtil.failure("결제 ID가 없습니다.");
                }

                OrderPaymentPrepareVO prepareVO = (OrderPaymentPrepareVO) session
                                .getAttribute(PAYMENT_PREPARE_SESSION_KEY);

                if (prepareVO == null) {
                        return ApiResponseUtil.failure("결제 준비 정보가 만료되었습니다. " + "주문서를 다시 작성해주세요.");
                }

                try {
                        Long orderNo = orderService.completePaidOrder(
                                        loginMember.getMemberNo(),
                                        prepareVO,
                                        requestVO.getPaymentId());

                        session.removeAttribute(ORDER_SHEET_SESSION_KEY);
                        session.removeAttribute(PAYMENT_PREPARE_SESSION_KEY);

                        Map<String, Object> response = ApiResponseUtil.success("결제와 주문이 완료되었습니다.");

                        response.put("orderNo", orderNo);
                        response.put(RESPONSE_REDIRECT_URL, "/order/complete/" + orderNo);

                        return response;

                } catch (IllegalArgumentException e) {

                        return ApiResponseUtil.failure(e.getMessage());

                } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                                log.error("결제 검증 및 주문 확정 중 오류 - paymentId: {}", requestVO.getPaymentId(), e);
                        }
                        return ApiResponseUtil.failure("결제 검증 또는 주문 처리 중 오류가 발생했습니다.");
                }
        }

        // 사용자 주문 결제 전액 취소
        @PostMapping("/payment/cancel")
        @ResponseBody
        public Map<String, Object> cancelPayment(@RequestBody OrderPaymentCancelRequestVO requestVO,
                        HttpSession session) {

                MemberVO loginMember = LoginMemberUtil.getLoginMember(session);

                if (loginMember == null) {
                        return ApiResponseUtil.loginRequired();
                }

                if (requestVO == null) {
                        return ApiResponseUtil.failure(
                                        "결제 취소 요청 정보가 없습니다.");
                }

                return processCancelRequest(
                                () -> orderCancelRefundService.requestOrderCancel(
                                                loginMember.getMemberNo(),
                                                requestVO.getOrderNo(),
                                                requestVO.getReason()),
                                "주문 취소 요청이 접수되었습니다. 사업자 승인 후 환불됩니다.",
                                "주문 취소 요청 처리 중 오류가 발생했습니다.",
                                "주문 전체 취소 요청 처리 중 오류 - orderNo: "
                                                + requestVO.getOrderNo());
        }
        /*
         * =========================================================
         * [상품별 부분 취소 요청 기능 추가]
         *
         * 사용자 주문 내역에서 선택한 주문상품 한 건만
         * 사업자 승인 대기 상태로 등록한다.
         * =========================================================
         */
        @PostMapping("/payment/cancel/item")
        @ResponseBody
        public Map<String, Object> cancelOrderItem(@RequestBody OrderPaymentCancelRequestVO requestVO,
                        HttpSession session) {

                MemberVO loginMember = LoginMemberUtil.getLoginMember(session);

                if (loginMember == null) {
                        return ApiResponseUtil.loginRequired();
                }

                if (requestVO == null || requestVO.getOrderItemNo() == null) {
                        return ApiResponseUtil.failure("부분 취소 요청 정보가 없습니다.");
                }

                return processCancelRequest(
                                () -> orderCancelRefundService.requestOrderItemCancel(
                                                loginMember.getMemberNo(),
                                                requestVO.getOrderItemNo(),
                                                requestVO.getReason()),
                                "상품 부분 취소 요청이 접수되었습니다. 사업자 승인 후 환불됩니다.",
                                "상품 부분 취소 요청 처리 중 오류가 발생했습니다.",
                                "상품 부분 취소 요청 처리 중 오류 - orderItemNo: "
                                                + requestVO.getOrderItemNo());
        }
        /*
         * =========================================================
         * [추가] 선택 상품 일괄 취소/환불 요청
         * Service의 단일 트랜잭션으로 처리하여 일부 성공을 방지한다.
         * =========================================================
         */
        @PostMapping("/payment/cancel/items")
        @ResponseBody
        public Map<String, Object> cancelOrderItems(
                        @RequestBody OrderPaymentCancelRequestVO requestVO,
                        HttpSession session) {

                MemberVO loginMember = LoginMemberUtil.getLoginMember(session);
                if (loginMember == null) {
                        return ApiResponseUtil.loginRequired();
                }

                if (requestVO == null || requestVO.getOrderItemNos() == null
                                || requestVO.getOrderItemNos().isEmpty()) {
                        return ApiResponseUtil.failure("선택한 주문상품이 없습니다.");
                }

                return processCancelRequest(
                                () -> orderCancelRefundService.requestOrderItemsCancel(
                                                loginMember.getMemberNo(),
                                                requestVO.getOrderItemNos(),
                                                requestVO.getReason()),
                                "선택한 상품의 취소/환불 요청이 접수되었습니다. 사업자 승인 후 환불됩니다.",
                                "선택 상품 취소/환불 요청 처리 중 오류가 발생했습니다.",
                                "선택 상품 일괄 취소/환불 요청 처리 중 오류");
        }
        // 주문 완료 화면
        @GetMapping("/complete/{orderNo}")
        public String orderComplete(@PathVariable Long orderNo, HttpSession session, Model model) {

                MemberVO loginMember = LoginMemberUtil.getLoginMember(session);

                if (loginMember == null) {
                        return "redirect:/member/login?redirect=/order/list";
                }

                OrderVO order;

                try {
                        order = orderService.getOrderDetail(loginMember.getMemberNo(), orderNo);

                } catch (IllegalArgumentException e) {
                        if (log.isWarnEnabled()) {
                                log.warn("주문 완료 정보 조회 실패 - orderNo: {}, message: {}", orderNo, e.getMessage());
                        }
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "주문 정보를 찾을 수 없습니다.", e);
                }

                model.addAttribute("order", order);

                return "order/orderComplete";
        }

        // 주문 내역 화면
        @GetMapping("/list")
        public String orderList(
                        @RequestParam(name = "page", defaultValue = "1") int page,
                        /* [추가] 취소/환불 내역 조회 조건 */
                        @RequestParam(name = "historyType", defaultValue = "ALL") String historyType,
                        @RequestParam(name = "historyStatus", defaultValue = "ALL") String historyStatus,
                        @RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam(name = "tab", defaultValue = "order") String tab,
                        HttpSession session, Model model) {

                MemberVO loginMember = LoginMemberUtil.getLoginMember(session);

                if (loginMember == null) {
                        return "redirect:/member/login?redirect=/order/list";
                }

                /*
                 * =========================================================
                 * [추가] 취소/환불 내역 조회 시작일 기본값
                 *
                 * 최초 진입 또는 초기화로 시작일이 전달되지 않은 경우
                 * 현재 날짜를 기본 조회 시작일로 설정한다.
                 * 사용자가 달력에서 다른 날짜를 선택한 경우에는
                 * 전달받은 날짜를 그대로 사용한다.
                 * =========================================================
                 */
                if (startDate == null) {
                        startDate = LocalDate.now(DateTimeUtil.KOREA_ZONE);
                }

                final int pageSize = 3;
                final int pageBlockSize = 5;

                int totalCount = orderService.getOrderCount(loginMember.getMemberNo());
                int totalPage = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));
                int currentPage = Math.clamp(page, 1, totalPage);
                int startRow = (currentPage - 1) * pageSize + 1;
                int endRow = currentPage * pageSize;

                List<OrderVO> orderList = orderService.getOrderList(loginMember.getMemberNo(), startRow, endRow);

                int startPage = (currentPage - 1) / pageBlockSize * pageBlockSize + 1;
                int endPage = Math.min(startPage + pageBlockSize - 1, totalPage);

                PageVO pageVO = new PageVO();

                pageVO.setCurrentPage(currentPage);
                pageVO.setPageSize(pageSize);
                pageVO.setTotalCount(totalCount);
                pageVO.setTotalPage(totalPage);
                pageVO.setStartPage(startPage);
                pageVO.setEndPage(endPage);
                pageVO.setPrev(startPage > 1);
                pageVO.setNext(endPage < totalPage);

                model.addAttribute("orderList", orderList);
                model.addAttribute("pageVO", pageVO);
                model.addAttribute("portOneTestMode", portOneTestMode);

                /* [추가] 취소/환불 내역은 주문 페이지네이션과 분리하여 조건 조회한다. */
                model.addAttribute("cancelRefundHistory",
                                orderCancelRefundService.getMemberCancelRefundHistory(
                                                loginMember.getMemberNo(), historyType, historyStatus, startDate,
                                                endDate));
                model.addAttribute("historyType", historyType);
                model.addAttribute("historyStatus", historyStatus);
                model.addAttribute("historyStartDate", startDate);
                model.addAttribute("historyEndDate", endDate);
                model.addAttribute("activeTab", "history".equalsIgnoreCase(tab) ? "history" : "order");

                return "order/orderList";
        }

        /*
         * =========================================================
         * [배송 조회 화면 추가]
         *
         * orderList.jsp에서 "배송조회" 버튼을 눌러 모달을 열 때
         * 호출하는 AJAX 전용 API이다. 화면 이동 없이 선택한
         * 주문상품의 배송 정보만 JSON으로 내려준다.
         * =========================================================
         */
        @GetMapping("/delivery")
        @ResponseBody
        public Map<String, Object> orderDeliveryDetail(
                        @RequestParam(name = "orderItemNo") Long orderItemNo,
                        HttpSession session) {

                MemberVO loginMember = LoginMemberUtil.getLoginMember(session);

                if (loginMember == null) {
                        return ApiResponseUtil.loginRequired();
                }

                if (orderItemNo == null) {
                        return ApiResponseUtil.failure("조회할 주문상품 번호가 없습니다.");
                }

                try {
                        DeliveryVO delivery = orderService.getDeliveryDetail(
                                        loginMember.getMemberNo(),
                                        orderItemNo);

                        Map<String, Object> response = ApiResponseUtil.success("배송 정보를 조회했습니다.");
                        response.put("delivery", delivery);

                        return response;

                } catch (IllegalArgumentException e) {
                        return ApiResponseUtil.failure(e.getMessage());

                } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                                log.error("배송 조회 처리 중 오류 - orderItemNo: {}", orderItemNo, e);
                        }
                        return ApiResponseUtil.failure("배송 정보 조회 중 오류가 발생했습니다.");
                }
        }

        /**
         * 장바구니 주문과 바로 구매에서 공통으로 사용하는 주문서 준비 처리입니다.
         */
        private Map<String, Object> prepareOrderSheet(
                        HttpSession session,
                        Function<Long, List<OrderSheetItemVO>> sheetItemLoader,
                        String logMessage) {

                MemberVO loginMember = LoginMemberUtil.getLoginMember(session);

                if (loginMember == null) {
                        return ApiResponseUtil.loginRequired();
                }

                try {
                        List<OrderSheetItemVO> sheetItems = sheetItemLoader.apply(loginMember.getMemberNo());

                        session.setAttribute(ORDER_SHEET_SESSION_KEY, sheetItems);
                        session.removeAttribute(PAYMENT_PREPARE_SESSION_KEY);

                        Map<String, Object> response = ApiResponseUtil.success("주문서를 작성해주세요.");
                        response.put(RESPONSE_REDIRECT_URL, "/order");

                        return response;

                } catch (IllegalArgumentException e) {
                        return ApiResponseUtil.failure(e.getMessage());

                } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                                log.error(logMessage, e);
                        }
                        return ApiResponseUtil.failure("주문서 작성 중 오류가 발생했습니다.");
                }
        }

        private Map<String, Object> processCancelRequest(
                        Runnable requestAction,
                        String successMessage,
                        String errorMessage,
                        String logMessage) {

                return ApiResponseUtil.execute(
                                requestAction,
                                successMessage,
                                errorMessage,
                                log,
                                logMessage,
                                response -> response.put(
                                                RESPONSE_REDIRECT_URL,
                                                ORDER_LIST_URL));
        }

}