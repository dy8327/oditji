package com.project.oditji.refund.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.refund.vo.OrderCancelRefundVO;

@Mapper
public interface OrderCancelRefundDAO {

        List<OrderItemVO> selectCancelableItemsByOrder(@Param("memberNo") Long memberNo,
                        @Param("orderNo") Long orderNo);

        OrderItemVO selectCancelableItem(@Param("memberNo") Long memberNo,
                        @Param("orderItemNo") Long orderItemNo);

        int countWaitingCancelByOrderItemNo(@Param("orderItemNo") Long orderItemNo);

        Long selectNextCancelGroupNo();

        int insertCancelRequest(OrderCancelRefundVO cancelRequestVO);

        int updateOrderItemCancelRequested(@Param("orderItemNo") Long orderItemNo);

        List<OrderCancelRefundVO> selectCancelListByBusiness(@Param("businessNo") Long businessNo,
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

        int restoreProductStock(@Param("productNo") Long productNo,
                        @Param("quantity") Integer quantity);

        int updateOrderStatusByItems(@Param("orderNo") Long orderNo);
}
