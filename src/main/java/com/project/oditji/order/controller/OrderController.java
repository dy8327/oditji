package com.project.oditji.order.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import com.project.oditji.order.vo.OrderSheetItemVO;
import com.project.oditji.order.vo.OrderSubmitRequestVO;
import com.project.oditji.order.vo.OrderVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/order")
public class OrderController {

    /*
     * 결제 전 임시 주문서 정보를 담아두는 세션 키.
     * 로그인 회원 1명당 1개의 진행 중인 주문서만 유지한다.
     */
    private static final String ORDER_SHEET_SESSION_KEY = "orderSheet";

    private final OrderService orderService;

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

            List<OrderSheetItemVO> sheetItems =
                    orderService.prepareCheckoutFromCart(
                            loginMember.getMemberNo(),
                            requestVO.getCartItemNos()
                    );

            session.setAttribute(
                    ORDER_SHEET_SESSION_KEY,
                    sheetItems
            );

            Map<String, Object> response =
                    successResponse("주문서를 작성해주세요.");

            response.put("redirectUrl", "/order");

            return response;

        } catch (IllegalArgumentException e) {

            return failResponse(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return failResponse("주문서 작성 중 오류가 발생했습니다.");
        }
    }

    /**
     * 상품 상세의 "바로 구매"로 주문서 작성 준비
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

            List<OrderSheetItemVO> sheetItems =
                    orderService.prepareDirectOrder(
                            loginMember.getMemberNo(),
                            requestVO.getProductNo(),
                            requestVO.getQuantity()
                    );

            session.setAttribute(
                    ORDER_SHEET_SESSION_KEY,
                    sheetItems
            );

            Map<String, Object> response =
                    successResponse("주문서를 작성해주세요.");

            response.put("redirectUrl", "/order");

            return response;

        } catch (IllegalArgumentException e) {

            return failResponse(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return failResponse("주문서 작성 중 오류가 발생했습니다.");
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
        List<OrderSheetItemVO> sheetItems =
                (List<OrderSheetItemVO>) session.getAttribute(
                        ORDER_SHEET_SESSION_KEY
                );

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

        return "order/order";
    }

    /**
     * 주문서 최종 제출 (결제하기)
     */
    @PostMapping("/submit")
    @ResponseBody
    public Map<String, Object> submitOrder(
            @RequestBody OrderSubmitRequestVO requestVO,
            HttpSession session) {

        MemberVO loginMember = getLoginMember(session);

        if (loginMember == null) {
            return loginRequiredResponse();
        }

        @SuppressWarnings("unchecked")
        List<OrderSheetItemVO> sheetItems =
                (List<OrderSheetItemVO>) session.getAttribute(
                        ORDER_SHEET_SESSION_KEY
                );

        if (sheetItems == null || sheetItems.isEmpty()) {
            return failResponse("주문서 정보가 만료되었습니다. 다시 시도해주세요.");
        }

        try {

            /*
             * TODO: 실제 결제(PG) 연동 지점.
             * 현재는 결제 승인 절차 없이 바로 주문을 확정한다.
             * PortOne 등 PG 연동 시, 결제 승인 콜백 이후 이 지점에서
             * 주문을 확정하도록 순서를 조정해야 한다.
             */
            Long orderNo =
                    orderService.submitOrder(
                            loginMember.getMemberNo(),
                            sheetItems,
                            requestVO.getReceiverName(),
                            requestVO.getReceiverPhone(),
                            requestVO.getAddress()
                    );

            session.removeAttribute(ORDER_SHEET_SESSION_KEY);

            Map<String, Object> response =
                    successResponse("주문이 완료되었습니다.");

            response.put("orderNo", orderNo);
            response.put("redirectUrl", "/order/complete/" + orderNo);

            return response;

        } catch (IllegalArgumentException e) {

            return failResponse(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return failResponse("주문 처리 중 오류가 발생했습니다.");
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
                    orderNo
            );

        } catch (IllegalArgumentException e) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    e.getMessage()
            );
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

        List<OrderVO> orderList =
                orderService.getOrderList(
                        loginMember.getMemberNo()
                );

        model.addAttribute("orderList", orderList);

        return "order/orderList";
    }

    private MemberVO getLoginMember(
            HttpSession session) {

        Object sessionMember =
                session.getAttribute("loginMember");

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

        Map<String, Object> response =
                new LinkedHashMap<String, Object>();

        response.put("success", true);
        response.put("message", message);

        return response;
    }

    private Map<String, Object> failResponse(
            String message) {

        Map<String, Object> response =
                new LinkedHashMap<String, Object>();

        response.put("success", false);
        response.put("message", message);

        return response;
    }

    private Map<String, Object> loginRequiredResponse() {

        Map<String, Object> response =
                failResponse("로그인이 필요한 서비스입니다.");

        response.put("loginRequired", true);

        return response;
    }
}
