package com.project.oditji.order.service;

import java.util.List;

import com.project.oditji.order.vo.OrderPaymentPrepareVO;
import com.project.oditji.order.vo.OrderSheetItemVO;
import com.project.oditji.order.vo.OrderVO;

public interface OrderService {

        /**
         * 장바구니에서 선택한 상품으로 주문서 항목을 구성한다.
         * 로그인 회원 소유 + 판매중 + 재고 보유 상품만 반환한다.
         */
        List<OrderSheetItemVO> prepareCheckoutFromCart(
                        Long memberNo,
                        List<Long> cartItemNos);

        /**
         * 상품 상세의 "바로 구매"로 주문서 항목을 구성한다.
         */
        List<OrderSheetItemVO> prepareDirectOrder(
                        Long memberNo,
                        Integer productNo,
                        Integer quantity);

        /**
         * 포트원 결제창을 호출하기 전 서버 결제정보를 준비한다.
         */
        OrderPaymentPrepareVO preparePayment(
                        Long memberNo,
                        List<OrderSheetItemVO> sheetItems,
                        String receiverName,
                        String receiverPhone,
                        String address);

        /**
         * 포트원 결제를 검증한 뒤 주문과 결제를 최종 저장한다.
         */
        Long completePaidOrder(
                        Long memberNo,
                        OrderPaymentPrepareVO paymentPrepareVO,
                        String paymentId);

        /**
         * 주문서 항목을 최종 확정하여 주문을 생성한다.
         * 재고 차감, (장바구니 유입분) 장바구니 항목 삭제까지 하나의 트랜잭션으로 처리한다.
         *
         * // * @return 생성된 주문 번호
         * //
         */
        // Long submitOrder(
        // Long memberNo,
        // List<OrderSheetItemVO> sheetItems,
        // String receiverName,
        // String receiverPhone,
        // String address
        // );

        /**
         * 로그인 회원의 주문 목록 (각 주문에 속한 상품 목록 포함)
         */
        List<OrderVO> getOrderList(
                        Long memberNo);

        /**
         * 주문 상세 (주문 완료 화면 등에서 사용, 로그인 회원 소유 검증 포함)
         */
        OrderVO getOrderDetail(
                        Long memberNo,
                        Long orderNo);
}
