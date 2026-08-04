package com.project.oditji.order.service;

import java.util.List;

import com.project.oditji.order.vo.DeliveryVO;
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
                        Long optionNo,
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
         * 로그인 회원이 결제한 주문을 포트원에서 전액 취소한다.
         *
         * 포트원 취소가 확인되면 주문·주문상품·결제 상태를
         * CANCELED로 변경하고 상품 재고를 복구한다.
         */
        void cancelPaidOrder(
                        Long memberNo,
                        Long orderNo,
                        String reason);

        /**
         * 로그인 회원의 주문 목록 (각 주문에 속한 상품 목록 포함)
         */
        List<OrderVO> getOrderList(
                        Long memberNo,
                        int startRow,
                        int endRow);

        int getOrderCount(
                        Long memberNo);

        /**
         * 주문 상세 (주문 완료 화면 등에서 사용, 로그인 회원 소유 검증 포함)
         */
        OrderVO getOrderDetail(
                        Long memberNo,
                        Long orderNo);

        /**
         * 로그인 회원 소유의 주문상품 배송 조회 (orderList 배송 조회 모달에서 사용)
         */
        DeliveryVO getDeliveryDetail(
                        Long memberNo,
                        Long orderItemNo);
}