package com.project.oditji.business.dao;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.EventManageVO;
import com.project.oditji.business.vo.EventProductVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.business.vo.DeliveryManageVO;
import com.project.oditji.business.vo.SettlementManageVO;
import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.order.vo.OrderVO;

@Mapper
public interface BusinessDAO {

        // 로그인 회원과 연결된 사업자 조회
        BusinessVO selectBusinessByMemberNo(@Param("memberNo") long memberNo);

        // 사업자등록번호 중복 확인
        int countByBusinessNumber(@Param("businessNumber") String businessNumber);

        // 사업자 회원가입 정보 등록
        int insertBusiness(BusinessVO businessVO);

        // 상품 등록
        int insertProduct(GoodsManageVO goodsManageVO);

        // 상품 대표 이미지 등록
        int insertProductImage(GoodsManageVO goodsManageVO);

        // 콘텐츠 검색 목록
        List<ContentSearchVO> selectBusinessContentList(@Param("keyword") String keyword);

        /*
         * =========================================================
         * 콘텐츠 단건 조회
         * 제거하면 안 됨.
         * BusinessServiceImpl에서 콘텐츠 존재 여부 확인에 사용.
         * =========================================================
         */
        ContentSearchVO selectContentByNo(@Param("contentNo") long contentNo);

        // 선택한 콘텐츠에 연결된 배우 목록
        List<ActorSearchVO> selectActorListByContentNo(@Param("contentNo") long contentNo);

        // 선택한 콘텐츠와 배우의 연결 여부 확인
        int countContentActor(@Param("contentNo") long contentNo, @Param("actorNo") long actorNo);

        /*
         * =========================================================
         * 사업자가 등록한 상품 목록 (승인된 상품만)
         * 이벤트 등록/수정 화면의 상품 검색 모달에서 사용.
         * 승인 대기(WAITING)/반려(REJECTED) 상품은 이벤트에 연결할 수
         * 없으므로 목록 자체에 포함하지 않음.
         * =========================================================
         */
        List<GoodsManageVO> selectApprovedProductListByBusinessNo(@Param("businessNo") long businessNo);

        // 사업자가 등록한 상품 목록
        List<GoodsManageVO> selectProductListByBusinessNo(@Param("businessNo") long businessNo);

        /*
         * =========================================================
         * 상품 수정 화면용 상품 단건 조회
         * PRODUCT_NO와 BUSINESS_NO를 함께 검사하여
         * 다른 사업자의 상품에 접근할 수 없도록 함.
         * =========================================================
         */
        GoodsManageVO selectProductForUpdate(@Param("productNo") long productNo, @Param("businessNo") long businessNo);

        // 상품 기본 정보 수정
        int updateProduct(GoodsManageVO goodsManageVO);

        // 기존 대표 이미지 수정
        int updateProductMainImage(GoodsManageVO goodsManageVO);

        /*
         * =========================================================
         * 상품 삭제 요청
         * 실제 상품 행은 삭제하지 않고
         * STATUS를 DELETE_REQUESTED로 변경.
         * =========================================================
         */
        int updateProductDeleteRequest(@Param("productNo") long productNo, @Param("businessNo") long businessNo);

        /*
         * =========================================================
         * 이벤트 연결 상품 소유 여부 확인
         * 로그인한 사업자가 등록한 상품인지 확인.
         * excludeEventNo: 이벤트 수정 화면에서 사용.
         * "지금 수정 중인 이벤트 자신과의 연결"은 중복 연결로
         * 취급하지 않기 위해 검사 대상에서 제외한다.
         * 신규 등록 시에는 null을 전달.
         * =========================================================
         */
        int countProductByBusinessNo(@Param("productNo") long productNo, @Param("businessNo") long businessNo,
                        @Param("excludeEventNo") Long excludeEventNo);

        // 이벤트 등록
        int insertEvent(EventManageVO eventManageVO);

        /*
         * =========================================================
         * 이벤트와 상품 연결 등록
         * 하나의 이벤트에 여러 상품을 연결할 수 있으므로
         * 상품 1건당 1회씩 호출. (Service 계층에서 반복 호출)
         * =========================================================
         */
        int insertEventProduct(@Param("eventNo") long eventNo, @Param("productNo") long productNo,
                        @Param("discountRate") int discountRate);

        /*
         * =========================================================
         * 이벤트에 연결된 상품 전체 삭제
         * 이벤트 수정 시 기존 연결 상품을 모두 지우고
         * 새로 선택된 상품 목록을 다시 등록하는 방식으로 처리.
         * =========================================================
         */
        int deleteEventProductByEventNo(@Param("eventNo") long eventNo);

        /*
         * =========================================================
         * 이벤트에 연결된 상품 목록 조회
         * 이벤트 수정 화면 진입 시 기존에 연결되어 있던
         * 상품 목록을 그대로 화면에 다시 그려주기 위해 사용.
         * =========================================================
         */
        List<EventProductVO> selectEventProductListByEventNo(@Param("eventNo") long eventNo);

        // 사업자 이벤트 목록 조회 * EVENT_PRODUCT -> PRODUCT 경로로 사업자 소유권을 확인.
        List<EventManageVO> selectEventListByBusinessNo(@Param("businessNo") long businessNo,
                        @Param("keyword") String keyword);

        // 승인된 이벤트 단건 조회 * EVENT_NO와 BUSINESS_NO를 함께 검사.
        EventManageVO selectApprovedEventForBusiness(@Param("eventNo") long eventNo,
                        @Param("businessNo") long businessNo);

        // 승인된 이벤트 기본 정보 수정
        int updateApprovedEvent(EventManageVO eventManageVO);

        // 승인된 이벤트 종료일 연장
        int extendApprovedEvent(@Param("eventNo") long eventNo, @Param("businessNo") long businessNo,
                        @Param("extendEndDate") java.time.LocalDate extendEndDate);

        /*
         * =========================================================
         * 종료일이 지난 승인 이벤트 일괄 종료 처리
         * END_DATE < 오늘 이면서 STATUS = 'APPROVED'인 이벤트를
         * 'END'로 전환한다. (EventStatusScheduler에서 매일 호출)
         * =========================================================
         */
        int updateExpiredEventStatus();

        // 마이페이지 대시보드 - 오늘 매출 합계
        long selectTodaySalesByBusinessNo(@Param("businessNo") long businessNo);

        // 마이페이지 대시보드 - 오늘 주문 건수
        int selectTodayOrderCountByBusinessNo(@Param("businessNo") long businessNo);

        /*
         * =========================================================
         * [오늘 구매 고객 수 조회 추가]
         * 오늘 정상 판매 상태의 상품을 구매한 중복되지 않은 회원 수 조회.
         * =========================================================
         */
        int selectTodayCustomerCountByBusinessNo(@Param("businessNo") long businessNo);

        // 마이페이지 대시보드 - 오늘 상품 클릭 수
        int selectTodayClickCountByBusinessNo(@Param("businessNo") long businessNo);

        // 마이페이지 대시보드 - 입금 대기 정산 금액 합계
        long selectWaitingSettlementAmountByBusinessNo(@Param("businessNo") long businessNo);

        // 마이페이지 대시보드 - 승인 대기 상품 수
        int selectWaitingProductCountByBusinessNo(@Param("businessNo") long businessNo);

        // 마이페이지 대시보드 - 인기 상품 목록 (클릭수 내림차순)
        List<GoodsManageVO> selectPopularProductsByBusinessNo(@Param("businessNo") long businessNo,
                        @Param("limit") int limit);

        // 사업자 주문 현황 - 주문 목록 조회
        List<OrderVO> selectBusinessOrderList(@Param("businessNo") long businessNo);

        // 사업자 주문 현황 - 주문 상품 목록 조회
        List<OrderItemVO> selectBusinessOrderItemList(@Param("businessNo") long businessNo);

        // 사업자 주문 상세 조회
        OrderVO selectBusinessOrderDetail(@Param("businessNo") long businessNo, @Param("orderNo") long orderNo);

        // 사업자 주문 상세 상품 목록 조회
        List<OrderItemVO> selectBusinessOrderItemDetailList(@Param("businessNo") long businessNo,
                        @Param("orderNo") long orderNo);

        /*
         * =========================================================
         * 사업자 판매 현황
         * 조회 기간 요약과 날짜별 판매 내역을 사업자 번호로 제한한다.
         * =========================================================
         */
        SettlementManageVO selectBusinessSalesStatus(
                        @Param("businessNo") long businessNo,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        List<SettlementManageVO> selectBusinessSalesHistory(
                        @Param("businessNo") long businessNo,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /* [수정] 사업자 수수료 관리 조회/변경 */
        SettlementManageVO selectMonthlySettlementSummary(@Param("businessNo") long businessNo);

        List<SettlementManageVO> selectSettlementPaymentHistory(@Param("businessNo") long businessNo);

        int updateSettlementRequestStatus(@Param("businessNo") long businessNo);

        SettlementManageVO selectSettlementAccount(@Param("businessNo") long businessNo);

        int updateSettlementAccount(
                        @Param("businessNo") long businessNo,
                        @Param("bankName") String bankName,
                        @Param("accountNumber") String accountNumber,
                        @Param("accountHolder") String accountHolder);

        /*
         * =========================================================
         * 사업자 배송 관리 목록 조회
         * 로그인 사업자의 ORDER_ITEM만 조회하며 상태/검색어 필터를 지원.
         * =========================================================
         */
        List<DeliveryManageVO> selectBusinessDeliveryList(
                        @Param("businessNo") long businessNo,
                        @Param("status") String status,
                        @Param("keyword") String keyword);

        /* 배송 변경 전 주문상품 소유권과 현재 상태를 확인. */
        DeliveryManageVO selectBusinessDeliveryItem(
                        @Param("businessNo") long businessNo,
                        @Param("orderItemNo") long orderItemNo);

        /* DELIVERY가 없으면 등록하고, 있으면 운송장/택배사/상태를 수정. */
        int mergeDelivery(DeliveryManageVO deliveryManageVO);

        /* 배송 상태와 주문상품 상태를 동일하게 맞춤. */
        int updateOrderItemDeliveryStatus(
                        @Param("businessNo") long businessNo,
                        @Param("orderItemNo") long orderItemNo,
                        @Param("status") String status);

        /* 한 주문의 모든 ORDER_ITEM 상태를 기준으로 ORDERS.ORDER_STATUS를 재계산. */
        int updateOrderStatusByOrderItem(@Param("orderNo") long orderNo);
}