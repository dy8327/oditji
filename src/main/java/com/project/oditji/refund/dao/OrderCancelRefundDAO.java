package com.project.oditji.refund.dao;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.refund.vo.OrderCancelRefundVO;

@Mapper
public interface OrderCancelRefundDAO {

        /* [추가] 사용자 취소/환불 내역 조건 조회 */
        List<OrderCancelRefundVO> selectMemberCancelRefundHistory(
                        @Param("memberNo") Long memberNo,
                        @Param("historyType") String historyType,
                        @Param("status") String status,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        List<OrderItemVO> selectCancelableItemsByOrder(@Param("memberNo") Long memberNo,
                        @Param("orderNo") Long orderNo);

        OrderItemVO selectCancelableItem(@Param("memberNo") Long memberNo,
                        @Param("orderItemNo") Long orderItemNo);

        int countWaitingCancelByOrderItemNo(@Param("orderItemNo") Long orderItemNo);

        Long selectNextCancelGroupNo();

        int insertCancelRequest(OrderCancelRefundVO cancelRequestVO);

        int updateOrderItemCancelRequested(@Param("orderItemNo") Long orderItemNo);

        // [페이징 리팩터링] currentPage/pageSize로 페이지 단위 조회
        List<OrderCancelRefundVO> selectCancelListByBusiness(@Param("businessNo") Long businessNo,
                        @Param("status") String status, @Param("offset") int offset,
                        @Param("pageSize") int pageSize);

        // [페이징 리팩터링 추가] 사업자 취소 목록 전체 건수 (검색 조건 동일 적용)
        int selectCancelListByBusinessCount(@Param("businessNo") Long businessNo,
                        @Param("status") String status);

        OrderCancelRefundVO selectCancelRequestForBusiness(@Param("cancelNo") Long cancelNo,
                        @Param("businessNo") Long businessNo);

        int approveCancelRequest(@Param("cancelNo") Long cancelNo);

        int approveFullGroupForBusiness(@Param("cancelGroupNo") Long cancelGroupNo,
                        @Param("businessNo") Long businessNo);

        int rejectCancelRequest(@Param("cancelNo") Long cancelNo,
                        @Param("rejectReason") String rejectReason);

        int rejectFullGroup(@Param("cancelGroupNo") Long cancelGroupNo,
                        @Param("rejectReason") String rejectReason);

        int countWaitingByGroup(@Param("cancelGroupNo") Long cancelGroupNo);

        int countRejectedByGroup(@Param("cancelGroupNo") Long cancelGroupNo);

        List<OrderCancelRefundVO> selectRequestsByGroup(@Param("cancelGroupNo") Long cancelGroupNo);

        int restoreOrderItemStatus(@Param("orderItemNo") Long orderItemNo);

        int restoreOrderItemsByGroup(@Param("cancelGroupNo") Long cancelGroupNo);

        int cancelOrderItem(@Param("orderItemNo") Long orderItemNo);

        int cancelOrderItemsByGroup(@Param("cancelGroupNo") Long cancelGroupNo);

        /*
         * =========================================================
         * [부분 취소 완료 정산 제외 추가]
         * =========================================================
         */
        int rejectSettlementByOrderItemNo(@Param("orderItemNo") Long orderItemNo);

        /*
         * =========================================================
         * [전체 취소 완료 정산 제외 추가]
         * =========================================================
         */
        int rejectSettlementsByCancelGroupNo(@Param("cancelGroupNo") Long cancelGroupNo);

        int restoreProductStock(@Param("productNo") Long productNo, @Param("quantity") Integer quantity);

        /*
         * =========================================================
         * [상품 옵션 재고 복구 추가]
         *
         * 취소/환불이 승인된 ORDER_ITEM의 OPTION_NO를 조회하여
         * 실제 구매했던 옵션 재고도 주문 수량만큼 복구합니다.
         *
         * OPTION_NO가 없는 일반 상품은 수정 대상이 없습니다.
         * =========================================================
         */
        int restoreProductOptionStockByOrderItemNo(@Param("orderItemNo") Long orderItemNo);

        int updateOrderStatusByItems(@Param("orderNo") Long orderNo);
}