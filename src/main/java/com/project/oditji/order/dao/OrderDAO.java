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
            @Param("productNo") Integer productNo
    );

    /**
     * 장바구니에서 선택한 상품들을 주문서 작성용으로 조회.
     * 로그인 회원 소유의 장바구니 상품만 조회한다.
     */
    List<OrderSheetItemVO> selectCartItemsForOrder(
            @Param("memberNo") Long memberNo,
            @Param("cartItemNos") List<Long> cartItemNos
    );

    /*
     * ================= 주문 생성 =================
     */

    int insertOrder(
            OrderVO orderVO
    );

    int insertOrderItem(
            OrderItemVO orderItemVO
    );

    /**
     * 주문 확정 시 재고 차감.
     * WHERE 조건에 재고 수량을 포함해 동시성 문제로 인한
     * 재고 초과 차감을 방지한다 (영향받은 행이 없으면 재고 부족으로 판단).
     */
    int decreaseProductStock(
            @Param("productNo") Integer productNo,
            @Param("quantity") Integer quantity
    );

    /*
     * ================= 주문 조회 =================
     */

    /**
     * 로그인 회원의 주문 목록 (주문 헤더만, 최신순)
     */
    List<OrderVO> selectOrderListByMember(
            @Param("memberNo") Long memberNo
    );

    /**
     * 로그인 회원의 전체 주문에 속한 주문 상세 목록.
     * 서비스 계층에서 ORDER_NO 기준으로 그룹핑한다.
     */
    List<OrderItemVO> selectOrderItemListByMember(
            @Param("memberNo") Long memberNo
    );

    /**
     * 주문 단건 조회 (로그인 회원 소유 여부 확인 포함)
     */
    OrderVO selectOrderByMember(
            @Param("memberNo") Long memberNo,
            @Param("orderNo") Long orderNo
    );

    /**
     * 특정 주문의 주문 상세 목록
     */
    List<OrderItemVO> selectOrderItemListByOrderNo(
            @Param("orderNo") Long orderNo
    );
}
