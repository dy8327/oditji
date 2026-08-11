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
import com.project.oditji.common.vo.SettlementRequestVO;
import com.project.oditji.review.vo.ProductReviewVO;

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
        // [상품 옵션 기능 추가] 색상-사이즈 조합별 재고 저장
        int insertProductOption(com.project.oditji.goods.vo.ProductOptionVO productOptionVO);

        /*
         * [상품 옵션 기능 추가]
         * 상품 목록/수정 모달에서 기존 색상-사이즈 옵션을 다시 보여주기 위한 조회.
         */
        List<com.project.oditji.goods.vo.ProductOptionVO> selectProductOptionsByProductNo(
                        @Param("productNo") long productNo);

        /*
         * [상품 옵션 기능 추가]
         * 수정 요청 처리 시 기존 옵션 조합을 전부 지우고 화면에서 넘어온
         * 조합으로 다시 채워 넣기 위한 전체 삭제(교체 방식).
         */
        int deleteProductOptionsByProductNo(@Param("productNo") long productNo);

        int insertProductImage(GoodsManageVO goodsManageVO);

        /*
         * =========================================================
         * [상품 세부 이미지 수정]
         *
         * 상품에 등록된 세부 이미지 경로를 조회합니다.
         * 대표 이미지(IS_MAIN='Y')는 제외합니다.
         * =========================================================
         */
        List<String> selectProductDetailImagePathList(@Param("productNo") long productNo);

        /*
         * =========================================================
         * [상품 세부 이미지 수정]
         *
         * 새 세부 이미지가 전달된 경우 기존 세부 이미지 DB 행을 삭제합니다.
         * 대표 이미지는 삭제하지 않습니다.
         * =========================================================
         */
        int deleteProductDetailImagesByProductNo(@Param("productNo") long productNo);

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

        /*
         * =========================================================
         * 사업자가 등록한 상품 목록
         * [페이징 리팩터링] 관리자 목록 화면과 동일하게 검색어/offset/pageSize를
         * 받아 페이지 단위로 조회한다. 전체 건수는 selectProductListCountByBusinessNo로 별도 조회.
         * =========================================================
         */
        List<GoodsManageVO> selectProductListByBusinessNo(
                        @Param("businessNo") long businessNo,
                        @Param("keyword") String keyword,
                        @Param("startDate") String startDate,
                        @Param("endDate") String endDate,
                        @Param("status") String status,
                        @Param("offset") int offset,
                        @Param("pageSize") int pageSize);

        /*
         * [페이징 리팩터링 추가]
         * [기간/승인 상태 조회 추가]
         * 상품 목록 전체 건수
         */
        int selectProductListCountByBusinessNo(
                        @Param("businessNo") long businessNo,
                        @Param("keyword") String keyword,
                        @Param("startDate") String startDate,
                        @Param("endDate") String endDate,
                        @Param("status") String status);

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

        /* [상품 이미지 개별 삭제 추가] 현재 상품의 대표 이미지 한 건 삭제 */
        int deleteProductMainImageByProductNo(@Param("productNo") long productNo);

        /* [상품 이미지 개별 삭제 추가] 현재 상품의 특정 세부 이미지 한 건 삭제 */
        int deleteProductDetailImageByPath(@Param("productNo") long productNo,
                        @Param("imagePath") String imagePath);

        /*
         * [상품 세부 이미지 수정]
         * 기본 이미지(IS_MAIN='Y')는 유지하고
         * 기존 세부 이미지(IS_MAIN='N')만 삭제합니다.
         */
        int deleteProductDetailImages(@Param("productNo") long productNo);

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

        /*
         * [페이징 리팩터링] 사업자 이벤트 목록 조회
         * EVENT_PRODUCT -> PRODUCT 경로로 사업자 소유권을 확인하며,
         * offset/pageSize로 페이지 단위 조회한다.
         * [기간/승인 상태 조회 추가]
         */
        List<EventManageVO> selectEventListByBusinessNo(
                        @Param("businessNo") long businessNo,
                        @Param("keyword") String keyword,
                        @Param("startDate") String startDate,
                        @Param("endDate") String endDate,
                        @Param("status") String status,
                        @Param("offset") int offset,
                        @Param("pageSize") int pageSize);

        /*
         * [페이징 리팩터링 추가]
         * [기간/승인 상태 조회 추가]
         * 사업자 이벤트 목록 전체 건수
         */
        int selectEventListCountByBusinessNo(
                        @Param("businessNo") long businessNo,
                        @Param("keyword") String keyword,
                        @Param("startDate") String startDate,
                        @Param("endDate") String endDate,
                        @Param("status") String status);

        // 승인된 이벤트 단건 조회 * EVENT_NO와 BUSINESS_NO를 함께 검사.
        EventManageVO selectApprovedEventForBusiness(@Param("eventNo") long eventNo,
                        @Param("businessNo") long businessNo);

        // 승인된 이벤트 기본 정보 수정
        int updateApprovedEvent(EventManageVO eventManageVO);

        // 승인된 이벤트 종료일 연장
        int extendApprovedEvent(@Param("eventNo") long eventNo, @Param("businessNo") long businessNo,
                        @Param("extendEndDate") LocalDate extendEndDate);

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

        // 마이페이지 대시보드 - 지급 대기 정산 금액 합계
        long selectWaitingSettlementAmountByBusinessNo(@Param("businessNo") long businessNo);

        // 마이페이지 대시보드 - 승인 대기 상품 수
        int selectWaitingProductCountByBusinessNo(@Param("businessNo") long businessNo);

        // 마이페이지 대시보드 - 인기 상품 목록 (클릭수 내림차순)
        List<GoodsManageVO> selectPopularProductsByBusinessNo(@Param("businessNo") long businessNo,
                        @Param("limit") int limit);

        /*
         * =========================================================
         * [사업자 대시보드 최근 주문]
         * 해당 사업자의 가장 최근 주문 상품 5건 조회
         * =========================================================
         */
        List<OrderItemVO> selectRecentOrdersByBusinessNo(
                        @Param("businessNo") long businessNo);

        /*
         * =========================================================
         * [사업자 대시보드 최근 리뷰]
         * 해당 사업자의 상품에 작성된 최근 리뷰 5건 조회
         * =========================================================
         */
        List<ProductReviewVO> selectRecentReviewsByBusinessNo(
                        @Param("businessNo") long businessNo);

        /*
         * =========================================================
         * [사업자 대시보드 평균 리뷰 점수]
         * 해당 사업자가 판매하는 상품에 작성된 상품 리뷰 평균
         * =========================================================
         */
        double selectAverageRatingByBusinessNo(
                        @Param("businessNo") long businessNo);

        // [페이징 리팩터링] 사업자 주문 현황 - 주문 목록 조회 (offset/pageSize로 페이지 단위 조회)
        List<OrderVO> selectBusinessOrderList(@Param("businessNo") long businessNo,
                        @Param("offset") int offset, @Param("pageSize") int pageSize);

        // [페이징 리팩터링 추가] 사업자 주문 목록 전체 건수
        int selectBusinessOrderListCount(@Param("businessNo") long businessNo);

        // 사업자 주문 현황 - 주문 상품 목록 조회
        List<OrderItemVO> selectBusinessOrderItemList(@Param("businessNo") long businessNo);

        /*
         * [리팩터링] 사업자 주문 상세 조회 매퍼(selectBusinessOrderDetail /
         * selectBusinessOrderItemDetailList)는 제거했다. 주문 상세는 이제
         * orderList.jsp 모달에서 selectBusinessOrderList / selectBusinessOrderItemList가
         * 이미 채워주는 데이터를 그대로 사용한다.
         */

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

        /*
         * [페이징 리팩터링] 날짜별 판매 내역 - offset/pageSize로 페이지 단위 조회한다.
         * 조회 기간 요약(selectBusinessSalesStatus)은 DUAL 기준 단일 행이라
         * 페이징 대상이 아니다.
         */
        List<SettlementManageVO> selectBusinessSalesHistory(
                        @Param("businessNo") long businessNo,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("offset") int offset,
                        @Param("pageSize") int pageSize);

        // [페이징 리팩터링 추가] 판매 내역 전체 건수 (조회 기간 내 판매가 발생한 날짜 수)
        int selectBusinessSalesHistoryCount(
                        @Param("businessNo") long businessNo,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /* 사업자 정산 관리 조회/변경 */
        SettlementManageVO selectMonthlySettlementSummary(@Param("businessNo") long businessNo);

        /* [정산 월 구분 추가] 선택한 매출 기간 기준 정산 요약 */
        SettlementManageVO selectSettlementSummaryByPeriod(
                        @Param("businessNo") long businessNo,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("settlementMonth") String settlementMonth);

        List<SettlementRequestVO> selectSettlementPaymentHistory(@Param("businessNo") long businessNo);

        /* 정산 요청 대상 금액 및 계좌 조회 */
        SettlementRequestVO selectSettlementRequestTarget(@Param("businessNo") long businessNo);

        /* 정산 요청 묶음 생성 */
        int insertSettlementRequest(SettlementRequestVO settlementRequestVO);

        /* 요청 대상 정산 원장에 요청 번호 연결 */
        int updateSettlementRequestNo(
                        @Param("businessNo") long businessNo,
                        @Param("requestNo") long requestNo);

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
                        @Param("keyword") String keyword,
                        @Param("offset") int offset,
                        @Param("pageSize") int pageSize);

        // [페이징 리팩터링 추가] 배송 목록 전체 건수 (검색 조건 동일 적용)
        int selectBusinessDeliveryListCount(
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

        /*
         * =========================================================
         * [옵션별 재입고 알림 추가]
         *
         * 기존 상품 옵션의 OPTION_NO를 유지하기 위해
         * 색상/사이즈/재고 값을 UPDATE합니다.
         *
         * 기존 옵션을 삭제 후 재등록하지 않기 때문에
         * PRODUCT_RESTOCK_REQUEST.OPTION_NO 연결도 유지됩니다.
         * =========================================================
         */
        int updateProductOption(com.project.oditji.goods.vo.ProductOptionVO productOptionVO);

        /*
         * =========================================================
         * [옵션별 재입고 알림 추가]
         *
         * 상품 수정 화면에서 실제로 제거된 옵션만 개별 삭제합니다.
         * 기존처럼 상품의 모든 옵션을 한 번에 삭제하지 않습니다.
         * =========================================================
         */
        int deleteProductOptionByOptionNo(@Param("optionNo") long optionNo);
}