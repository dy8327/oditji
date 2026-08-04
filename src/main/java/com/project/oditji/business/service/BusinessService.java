package com.project.oditji.business.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.EventManageVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.business.vo.BusinessDashboardVO;
import com.project.oditji.business.vo.DeliveryManageVO;
import com.project.oditji.business.vo.SettlementManageVO;
import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.order.vo.OrderVO;
import com.project.oditji.common.vo.SettlementRequestVO;

public interface BusinessService {

        // 로그인 회원과 연결된 사업자 조회
        BusinessVO getBusinessByMemberNo(long memberNo);

        // 사업자 마이페이지 대시보드 통계 조회
        BusinessDashboardVO getBusinessDashboard(long businessNo);

        // 사업자등록번호 사용 가능 여부 확인
        boolean isBusinessNumberAvailable(
                        String businessNumber);

        // 상품 등록
        long registerProduct(GoodsManageVO goodsManageVO, MultipartFile productImage);

        // 기존 DB 콘텐츠 검색 목록 조회
        List<ContentSearchVO> getContentList(String keyword);

        // 상품 등록 화면용 JSONL 콘텐츠 검색 목록 조회
        List<ContentSearchVO> getCachedContentList(String keyword);

        // JSONL 콘텐츠 선택 후 TMDB 배우 미리보기 조회
        List<ActorSearchVO> getActorPreview(
                        Long tmdbId,
                        String contentType);

        // 콘텐츠 단건 조회
        ContentSearchVO getContentByNo(long contentNo);

        // 선택한 콘텐츠에 연결된 배우 목록 조회
        List<ActorSearchVO> getActorListByContentNo(long contentNo);

        /*
         * =========================================================
         * 사업자가 등록한 상품 목록 조회 (승인된 상품만)
         * 이벤트 등록/수정 화면의 상품 검색 모달 전용
         * =========================================================
         */
        List<GoodsManageVO> getApprovedProductListByBusinessNo(
                        long businessNo);

        // 사업자가 등록한 상품 목록 조회
        List<GoodsManageVO> getProductListByBusinessNo(long businessNo);

        /*
         * =========================================================
         * 상품 수정 화면용 상품 단건 조회
         * 현재 로그인한 사업자의 상품인지 함께 확인
         * =========================================================
         */
        GoodsManageVO getProductForUpdate(long productNo, long businessNo);

        /*
         * =========================================================
         * 상품 수정 요청
         * 새 이미지가 전달되지 않으면 기존 이미지를 유지
         * =========================================================
         */
        void updateProduct(GoodsManageVO goodsManageVO, MultipartFile productImage);

        /*
         * =========================================================
         * 상품 삭제 요청
         * 상품을 즉시 삭제하지 않고
         * PRODUCT.STATUS를 DELETE_REQUESTED로 변경
         * =========================================================
         */
        void requestProductDelete(long productNo, long businessNo, String reason);

        /*
         * =========================================================
         * 이벤트 등록
         * EVENT 테이블에 이벤트를 저장하고,
         * 상품이 선택된 경우 EVENT_PRODUCT에도 연결 정보를 저장
         * =========================================================
         */
        long registerEvent(EventManageVO eventManageVO, MultipartFile eventImage);

        // 사업자 이벤트 목록 조회
        List<EventManageVO> getEventListByBusinessNo(long businessNo, String keyword);

        /*
         * =========================================================
         * 승인된 이벤트 단건 조회
         * 현재 로그인한 사업자의 이벤트인지 함께 확인
         * =========================================================
         */
        EventManageVO getApprovedEventForBusiness(long eventNo, long businessNo);

        /*
         * =========================================================
         * 승인된 이벤트 즉시 수정
         * 별도 수정 요청 테이블이 없으므로
         * EVENT와 EVENT_PRODUCT를 즉시 변경.
         * =========================================================
         */
        void updateApprovedEvent(EventManageVO eventManageVO, MultipartFile eventImage);

        /*
         * =========================================================
         * 승인된 이벤트 즉시 연장
         * 별도 연장 요청 테이블이 없으므로
         * EVENT.END_DATE를 즉시 변경
         * =========================================================
         */
        void extendApprovedEvent(long eventNo, long businessNo, java.time.LocalDate extendEndDate, String extendReason);

        List<GoodsManageVO> getPopularProducts(long businessNo);

        // 사업자 주문 현황 - 주문 목록 조회
        List<OrderVO> getBusinessOrderList(long businessNo);

        // 사업자 주문 현황 - 주문 상품 목록 조회
        List<OrderItemVO> getBusinessOrderItemList(long businessNo);

        /*
         * [리팩터링] 사업자 주문 상세 조회(getBusinessOrderDetail)는 제거했다.
         * 주문 상세는 이제 orderList.jsp 모달에서 getBusinessOrderList가 이미
         * 채워주는 데이터(주문별 배송지/상품 목록 포함)를 그대로 사용한다.
         */

        /*
         * =========================================================
         * 사업자 판매 현황 조회
         * 선택한 기간의 요약 정보와 날짜별 판매 내역을 조회한다.
         * =========================================================
         */
        SettlementManageVO getBusinessSalesStatus(long businessNo, LocalDate startDate, LocalDate endDate);

        List<SettlementManageVO> getBusinessSalesHistory(long businessNo, LocalDate startDate, LocalDate endDate);

        /*
        * =========================================================
        * 사업자 정산 관리
        * 정산 가능 금액, 정산 요청 내역, 정산 계좌를 관리한다.
        * =========================================================
        */
        SettlementManageVO getMonthlySettlementSummary(long businessNo);

        List<SettlementRequestVO> getSettlementPaymentHistory(long businessNo);

        void requestSettlementConfirmation(long businessNo);

        SettlementManageVO getSettlementAccount(long businessNo);

        void updateSettlementAccount(long businessNo, String bankName, String accountNumber, String accountHolder);

        /*
         * =========================================================
         * 사업자 배송 관리 목록 조회
         * 상태와 검색어는 선택 조건이며 사업자 소유 주문상품만 반환.
         * =========================================================
         */
        List<DeliveryManageVO> getBusinessDeliveryList(long businessNo, String status, String keyword);

        /*
         * =========================================================
         * 운송장 정보 및 배송 상태 저장
         * DELIVERY와 ORDER_ITEM 상태를 함께 변경하고 주문 상태도 재계산.
         * =========================================================
         */
        void updateBusinessDelivery(long businessNo, long orderItemNo, String courier,
                        String trackingNumber, String status);

}