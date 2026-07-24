package com.project.oditji.order.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.order.vo.OrderSheetItemVO;
import com.project.oditji.order.vo.OrderVO;

public interface OrderDAO {

        /*
         * ================= 주문서 작성 단계 =================
         */

        /**
         * 바로 구매(직접 구매)용 상품 단건 조회 (판매 상태/재고 검증용)
         */
        OrderSheetItemVO selectProductForOrder(
                        @Param("productNo") Integer productNo);

        /**
         * 장바구니에서 선택한 상품들을 주문서 작성용으로 조회.
         * 로그인 회원 소유의 장바구니 상품만 조회한다.
         */
        List<OrderSheetItemVO> selectCartItemsForOrder(
                        @Param("memberNo") Long memberNo,
                        @Param("cartItemNos") List<Long> cartItemNos);

        /*
         * ================= 주문 생성 =================
         */

        int insertOrder(
                        OrderVO orderVO);

        int insertOrderItem(
                        OrderItemVO orderItemVO);

        /*
         * =========================================================
         * [결제 완료 주문상품 정산 예정 데이터 생성 추가]
         * 주문상품별 수수료와 실 정산 예정 금액을 저장한다.
         * =========================================================
         */
        int insertWaitingSettlement(
                        @Param("orderItemNo") Long orderItemNo);

        /**
         * 주문 확정 시 재고 차감.
         * WHERE 조건에 재고 수량을 포함해 동시성 문제로 인한
         * 재고 초과 차감을 방지한다 (영향받은 행이 없으면 재고 부족으로 판단).
         */
        int decreaseProductStock(
                        @Param("productNo") Integer productNo,
                        @Param("quantity") Integer quantity);

        /*
         * =========================================================
         * 결제 취소
         * =========================================================
         */

        /**
         * 해당 주문의 배송 중 또는 배송 완료 상품 개수를 조회한다.
         *
         * DELIVERY 행이 없거나 PREPARING 상태이면 취소할 수 있다.
         */
        int countStartedDeliveryByOrderNo(
                        @Param("orderNo") Long orderNo);

        /**
         * 결제 취소 시 주문에 포함된 상품 재고를 복구한다.
         *
         * 아직 PAID 상태인 주문상품만 집계하여 중복 복구를 방지한다.
         */
        int restoreProductStockByOrderNo(
                        @Param("orderNo") Long orderNo);

        /**
         * 주문상품 상태를 PAID에서 CANCELED로 변경한다.
         */
        int updateOrderItemsCanceled(
                        @Param("orderNo") Long orderNo);

        /**
         * 로그인 회원 소유의 PAID 주문을 CANCELED로 변경한다.
         */
        int updateOrderCanceled(
                        @Param("memberNo") Long memberNo,
                        @Param("orderNo") Long orderNo);

        /*
         * =========================================================
         * [사용자 즉시 전액 취소 정산 제외 추가]
         * 해당 주문의 WAITING 정산을 REJECTED로 변경한다.
         * =========================================================
         */
        int rejectSettlementsByOrderNo(
                        @Param("orderNo") Long orderNo);

        /*
         * ================= 주문 조회 =================
         */

        /**
         * 로그인 회원의 주문 목록 (주문 헤더만, 최신순)
         */
        List<OrderVO> selectOrderListByMember(
                        @Param("memberNo") Long memberNo,
                        @Param("startRow") int startRow,
                        @Param("endRow") int endRow);

        int countOrderListByMember(
                        @Param("memberNo") Long memberNo);

        /**
         * 로그인 회원의 전체 주문에 속한 주문 상세 목록.
         * 서비스 계층에서 ORDER_NO 기준으로 그룹핑한다.
         */
        List<OrderItemVO> selectOrderItemListByMember(
                        @Param("memberNo") Long memberNo);

        /**
         * 주문 단건 조회 (로그인 회원 소유 여부 확인 포함)
         */
        OrderVO selectOrderByMember(
                        @Param("memberNo") Long memberNo,
                        @Param("orderNo") Long orderNo);

        /**
         * 특정 주문의 주문 상세 목록
         */
        List<OrderItemVO> selectOrderItemListByOrderNo(
                        @Param("orderNo") Long orderNo);
}
