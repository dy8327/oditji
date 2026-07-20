package com.project.oditji.order.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.order.service.OrderService;
import com.project.oditji.order.vo.OrderCheckoutRequestVO;
import com.project.oditji.order.vo.OrderDirectRequestVO;
import com.project.oditji.order.vo.OrderPaymentCompleteRequestVO;
import com.project.oditji.order.vo.OrderPaymentPrepareVO;
import com.project.oditji.order.vo.OrderSheetItemVO;
import com.project.oditji.order.vo.OrderSubmitRequestVO;
import com.project.oditji.order.vo.OrderVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/order")
public class OrderController {

    private static final String ORDER_SHEET_SESSION_KEY = "orderSheet";

    private static final String PAYMENT_PREPARE_SESSION_KEY = "orderPaymentPrepare";

    private final OrderService orderService;

    @Value("${portone.store-id}")
    private String storeId;

    @Value("${portone.payment.channel-key}")
    private String paymentChannelKey;

    public OrderController(
            OrderService orderService) {

        this.orderService = orderService;
    }

    /**
     * 장바구니에서 선택한 상품으로 주문서 작성 준비
     */
    @PostMapping("/checkout")
    @ResponseBody
    public Map<String, Object> checkoutFromCart(
            @RequestBody OrderCheckoutRequestVO requestVO,
            HttpSession session) {

        MemberVO loginMember = getLoginMember(session);

        if (loginMember == null) {
            return loginRequiredResponse();
        }

        try {

            List<OrderSheetItemVO> sheetItems = orderService.prepareCheckoutFromCart(
                    loginMember.getMemberNo(),
                    requestVO.getCartItemNos());

            session.setAttribute(
                    ORDER_SHEET_SESSION_KEY,
                    sheetItems);

            session.removeAttribute(
                    PAYMENT_PREPARE_SESSION_KEY);

            Map<String, Object> response = successResponse(
                    "주문서를 작성해주세요.");

            response.put(
                    "redirectUrl",
                    "/order");

            return response;

        } catch (IllegalArgumentException e) {

            return failResponse(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return failResponse(
                    "주문서 작성 중 오류가 발생했습니다.");
        }
    }

    /**
     * 상품 상세 바로 구매 주문서 작성 준비
     */
    @PostMapping("/direct")
    @ResponseBody
    public Map<String, Object> directOrder(
            @RequestBody OrderDirectRequestVO requestVO,
            HttpSession session) {

        MemberVO loginMember = getLoginMember(session);

        if (loginMember == null) {
            return loginRequiredResponse();
        }

        try {

            List<OrderSheetItemVO> sheetItems = orderService.prepareDirectOrder(
                    loginMember.getMemberNo(),
                    requestVO.getProductNo(),
                    requestVO.getQuantity());

            session.setAttribute(
                    ORDER_SHEET_SESSION_KEY,
                    sheetItems);

            session.removeAttribute(
                    PAYMENT_PREPARE_SESSION_KEY);

            Map<String, Object> response = successResponse(
                    "주문서를 작성해주세요.");

            response.put(
                    "redirectUrl",
                    "/order");

            return response;

        } catch (IllegalArgumentException e) {

            return failResponse(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return failResponse(
                    "주문서 작성 중 오류가 발생했습니다.");
        }
    }

    /**
     * 주문서 화면
     */
    @GetMapping
    public String orderSheet(
            HttpSession session,
            Model model) {

        MemberVO loginMember = getLoginMember(session);

        if (loginMember == null) {
            return "redirect:/member/login?redirect=/order";
        }

        @SuppressWarnings("unchecked")
        List<OrderSheetItemVO> sheetItems = (List<OrderSheetItemVO>) session.getAttribute(
                ORDER_SHEET_SESSION_KEY);

        if (sheetItems == null || sheetItems.isEmpty()) {
            return "redirect:/cart";
        }

        long totalPrice = 0L;

        for (OrderSheetItemVO item : sheetItems) {
            totalPrice += item.getItemTotalPrice();
        }

        model.addAttribute(
                "orderItems",
                sheetItems);

        model.addAttribute(
                "totalPrice",
                totalPrice);

        model.addAttribute(
                "defaultReceiverName",
                loginMember.getMemberName());

        model.addAttribute(
                "defaultReceiverPhone",
                loginMember.getPhone());

        model.addAttribute(
                "defaultReceiverEmail",
                loginMember.getEmail());

        return "order/order";
    }

    /**
     * 포트원 결제창 호출 전 결제 준비
     */
    @PostMapping("/payment/prepare")
    @ResponseBody
    public Map<String, Object> preparePayment(
            @RequestBody OrderSubmitRequestVO requestVO,
            HttpSession session) {

        MemberVO loginMember = getLoginMember(session);

        if (loginMember == null) {
            return loginRequiredResponse();
        }

        @SuppressWarnings("unchecked")
        List<OrderSheetItemVO> sheetItems = (List<OrderSheetItemVO>) session.getAttribute(
                ORDER_SHEET_SESSION_KEY);

        if (sheetItems == null || sheetItems.isEmpty()) {
            return failResponse(
                    "주문서 정보가 만료되었습니다. "
                            + "다시 주문해주세요.");
        }

        try {

            OrderPaymentPrepareVO prepareVO = orderService.preparePayment(
                    loginMember.getMemberNo(),
                    sheetItems,
                    requestVO.getReceiverName(),
                    requestVO.getReceiverPhone(),
                    requestVO.getAddress());

            prepareVO.setStoreId(storeId);
            prepareVO.setChannelKey(
                    paymentChannelKey);

            session.setAttribute(
                    PAYMENT_PREPARE_SESSION_KEY,
                    prepareVO);

            Map<String, Object> response = successResponse(
                    "결제 준비가 완료되었습니다.");

            response.put(
                    "storeId",
                    prepareVO.getStoreId());

            response.put(
                    "channelKey",
                    prepareVO.getChannelKey());

            response.put(
                    "paymentId",
                    prepareVO.getPaymentId());

            response.put(
                    "orderName",
                    prepareVO.getOrderName());

            response.put(
                    "totalAmount",
                    prepareVO.getTotalAmount());

            return response;

        } catch (IllegalArgumentException e) {

            return failResponse(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return failResponse(
                    "결제 준비 중 오류가 발생했습니다.");
        }
    }

    /**
     * 포트원 결제 완료 후 서버 검증 및 주문 확정
     */
    @PostMapping("/payment/complete")
    @ResponseBody
    public Map<String, Object> completePayment(
            @RequestBody OrderPaymentCompleteRequestVO requestVO,
            HttpSession session) {

        MemberVO loginMember = getLoginMember(session);

        if (loginMember == null) {
            return loginRequiredResponse();
        }

        if (requestVO == null
                || requestVO.getPaymentId() == null
                || requestVO.getPaymentId().isBlank()) {

            return failResponse(
                    "결제 ID가 없습니다.");
        }

        OrderPaymentPrepareVO prepareVO = (OrderPaymentPrepareVO) session.getAttribute(
                PAYMENT_PREPARE_SESSION_KEY);

        if (prepareVO == null) {
            return failResponse(
                    "결제 준비 정보가 만료되었습니다. "
                            + "주문서를 다시 작성해주세요.");
        }

        try {

            Long orderNo = orderService.completePaidOrder(
                    loginMember.getMemberNo(),
                    prepareVO,
                    requestVO.getPaymentId());

            session.removeAttribute(
                    ORDER_SHEET_SESSION_KEY);

            session.removeAttribute(
                    PAYMENT_PREPARE_SESSION_KEY);

            Map<String, Object> response = successResponse(
                    "결제와 주문이 완료되었습니다.");

            response.put(
                    "orderNo",
                    orderNo);

            response.put(
                    "redirectUrl",
                    "/order/complete/" + orderNo);

            return response;

        } catch (IllegalArgumentException e) {

            return failResponse(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return failResponse(
                    "결제 검증 또는 주문 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 주문 완료 화면
     */
    @GetMapping("/complete/{orderNo}")
    public String orderComplete(
            @PathVariable Long orderNo,
            HttpSession session,
            Model model) {

        MemberVO loginMember = getLoginMember(session);

        if (loginMember == null) {
            return "redirect:/member/login?redirect=/order/list";
        }

        OrderVO order;

        try {

            order = orderService.getOrderDetail(
                    loginMember.getMemberNo(),
                    orderNo);

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    e.getMessage());
        }

        model.addAttribute("order", order);

        return "order/orderComplete";
    }

    /**
     * 주문 내역 화면
     */
    @GetMapping("/list")
    public String orderList(
            HttpSession session,
            Model model) {

        MemberVO loginMember = getLoginMember(session);

        if (loginMember == null) {
            return "redirect:/member/login?redirect=/order/list";
        }

        List<OrderVO> orderList = orderService.getOrderList(loginMember.getMemberNo());

        model.addAttribute(
                "orderList",
                orderList);

        return "order/orderList";
    }

    private MemberVO getLoginMember(
            HttpSession session) {

        Object sessionMember = session.getAttribute("loginMember");

        if (!(sessionMember instanceof MemberVO)) {
            return null;
        }

        MemberVO loginMember = (MemberVO) sessionMember;

        if (loginMember.getMemberNo() == null
                || loginMember.getMemberNo() <= 0) {

            return null;
        }

        return loginMember;
    }

    private Map<String, Object> successResponse(
            String message) {

        Map<String, Object> response = new LinkedHashMap<String, Object>();

        response.put("success", true);
        response.put("message", message);

        return response;
    }

    private Map<String, Object> failResponse(
            String message) {

        Map<String, Object> response = new LinkedHashMap<String, Object>();

        response.put("success", false);
        response.put("message", message);

        return response;
    }

    private Map<String, Object> loginRequiredResponse() {

        Map<String, Object> response = failResponse("로그인이 필요한 서비스입니다.");

        response.put(
                "loginRequired", true);

        return response;
    }
}