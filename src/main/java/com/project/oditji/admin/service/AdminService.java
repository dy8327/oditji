package com.project.oditji.admin.service;

import java.util.List;

import com.project.oditji.admin.vo.AdminVO;
import com.project.oditji.admin.vo.BusinessManageVO;
import com.project.oditji.admin.vo.ContentManageVO;
import com.project.oditji.admin.vo.EventManageVO;
import com.project.oditji.admin.vo.EventStatVO;
import com.project.oditji.admin.vo.MemberManageVO;
import com.project.oditji.admin.vo.MemberStatVO;
import com.project.oditji.admin.vo.MonitoringVO;
import com.project.oditji.admin.vo.MonitoringSummaryVO;
import com.project.oditji.admin.vo.BusinessStatVO;
import com.project.oditji.admin.vo.OrderManageVO;
import com.project.oditji.admin.vo.OrderStatVO;
import com.project.oditji.admin.vo.PlatformVO;
import com.project.oditji.admin.vo.PopularClickVO;
import com.project.oditji.admin.vo.ProductManageVO;
import com.project.oditji.admin.vo.ProductStatVO;
import com.project.oditji.admin.vo.ReviewManageVO;
import com.project.oditji.admin.vo.ReviewStatVO;
import com.project.oditji.admin.vo.SettlementStatVO;
import com.project.oditji.admin.vo.VisitorTrendVO;
import com.project.oditji.common.vo.SettlementRequestVO;

public interface AdminService {

        // 대시보드
        AdminVO getDashboardStats();

        // 회원 관리
        // memberType: all(기본,전체 유저) / general(일반 회원) / sns(SNS 로그인 유저) / business(사업자
        // 회원)
        List<MemberManageVO> getMemberList(String keyword, String searchType, String status, String memberType,
                        int page,
                        int pageSize);

        int getMemberListCount(String keyword, String searchType, String status, String memberType);

        MemberStatVO getMemberStats();

        void suspendMember(Long memberNo);

        void restoreMember(Long memberNo);

        void deleteMember(Long memberNo);

        /**
         * 체크박스로 선택한 회원들에 대해 정지(suspend) / 복구(restore) / 완전삭제(delete)를 한 번에 처리한다.
         * 본인이 직접 탈퇴하여 자동삭제 대기 중(WITHDRAWN)인 회원은 선택 대상에서 제외하고 처리하며,
         * 제외된 회원 수를 반환한다.
         */
        int bulkMemberAction(List<Long> memberNos, String action);

        void deleteExpiredWithdrawMembers();

        // 콘텐츠 리뷰 관리
        // searchType: null/빈값(전체) / writer(작성자) / content(콘텐츠명)
        List<ReviewManageVO> getContentReviewList(String tab, String keyword, String searchType, int page,
                        int pageSize);

        int getContentReviewListCount(String tab, String keyword, String searchType);

        // 콘텐츠 리뷰 관리 상단 통계 카드 (전체 리뷰 / 신고 접수)
        ReviewStatVO getContentReviewStats();

        void deleteContentReview(Long reviewNo);

        void approveContentReviewReport(Long reviewNo);

        void rejectContentReviewReport(Long reviewNo);

        /**
         * 체크박스로 선택한 콘텐츠 리뷰들을 삭제(all 탭) / 신고 승인(report 탭) / 신고 반려(report 탭) 중
         * 하나로 한 번에 처리한다. 이미 처리된 신고 등 처리할 수 없는 건은 건너뛰고,
         * 건너뛴 건수를 반환한다.
         */
        int bulkContentReviewAction(List<Long> reviewNos, String action);

        // 상품 리뷰 관리
        // searchType: null/빈값(전체) / writer(작성자) / product(상품명)
        List<ReviewManageVO> getProductReviewList(String tab, String keyword, String searchType, int page,
                        int pageSize);

        int getProductReviewListCount(String tab, String keyword, String searchType);

        // 상품 리뷰 관리 상단 통계 카드 (전체 리뷰 / 신고 접수)
        ReviewStatVO getProductReviewStats();

        void deleteProductReview(Long reviewNo);

        void approveProductReviewReport(Long reviewNo);

        void rejectProductReviewReport(Long reviewNo);

        /**
         * 체크박스로 선택한 상품 리뷰들을 삭제(all 탭) / 신고 승인(report 탭) / 신고 반려(report 탭) 중
         * 하나로 한 번에 처리한다. 이미 처리된 신고 등 처리할 수 없는 건은 건너뛰고,
         * 건너뛴 건수를 반환한다.
         */
        int bulkProductReviewAction(List<Long> reviewNos, String action);

        // 이벤트 관리
        // tab: null/빈값(전체) / waiting(승인 대기) / approved(승인 완료) / end(종료)
        // period: null/빈값(전체) / today(오늘) / week(최근 7일) / month(최근 30일) -
        // 요청일(CREATED_AT) 기준
        List<EventManageVO> getEventList(String tab, String keyword, String period, int page, int pageSize);

        int getEventListCount(String tab, String keyword, String period);

        // 이벤트 관리 상단 통계 카드 (전체 / 승인 대기 / 승인 완료 / 종료)
        EventStatVO getEventStats();

        void approveEvent(Long eventNo);

        void rejectEvent(Long eventNo);

        // 상품 관리
        // searchType: null/빈값(전체) / business(사업자명) / product(상품명)
        List<ProductManageVO> getProductRequestList(String tab, String keyword, String searchType, int page,
                        int pageSize);

        int getProductRequestListCount(String tab, String keyword, String searchType);

        // 상품 관리 상단 통계 카드 (전체 / 승인 대기 / 승인 완료 / 삭제 요청)
        ProductStatVO getProductStats();

        void approveProduct(Long productNo);

        void rejectProduct(Long productNo);

        // 주문 조회 (조회 전용 - 처리는 사업자 담당)
        List<OrderManageVO> getOrderList(String keyword, int page, int pageSize);

        int getOrderListCount(String keyword);

        // 주문/환불 관리 화면 상단 통계 카드 (전체 주문 / 전체 환불요청 / 환불 대기 / 환불 완료)
        OrderStatVO getOrderStats();

        // 환불 조회 (조회 전용 - 승인/거절은 사업자 담당)
        List<OrderManageVO> getRefundList(String keyword, String status, int page, int pageSize);

        int getRefundListCount(String keyword, String status);

        // 사업자 관리
        // searchType: null/빈값(전체) / name(이름) / id(아이디) / email(이메일)
        List<BusinessManageVO> getBusinessList(String keyword, String searchType, int page, int pageSize);

        int getBusinessListCount(String keyword, String searchType);

        List<BusinessManageVO> getBusinessApprovalList(String keyword, String searchType, int page, int pageSize);

        int getBusinessApprovalListCount(String keyword, String searchType);

        // 사업자 관리 화면 상단 통계 카드 (입점 완료 / 승인 대기)
        BusinessStatVO getBusinessStats();

        void updateBusinessGrade(Long businessNo, String gradeName);

        /* [사업자 자동 등급 관리 추가] 누적 실매출 기준으로 전체 사업자를 자동 승급한다. */
        int updateBusinessGradesBySales();

        void approveBusiness(Long businessNo);

        void rejectBusiness(Long businessNo);

        // 정산 요청 관리
        List<SettlementRequestVO> getSettlementList(String keyword, String status, String period, int page,
                        int pageSize);

        int getSettlementListCount(String keyword, String status, String period);

        // 정산 관리 화면 상단 통계 카드
        SettlementStatVO getSettlementStats();

        /* 정산 요청 지급 완료 처리 */
        void confirmSettlement(Long requestNo);

        /* 정산 요청 반려 처리 */
        void rejectSettlement(Long requestNo, String rejectReason);

        // 모니터링
        List<MonitoringVO> getMonitoringList();

        /* [모니터링 화면 개편] 상단 요약 통계 */
        MonitoringSummaryVO getMonitoringSummary();

        /*
         * [모니터링 방문 수 기간 연동]
         * 선택한 기간(7일/3개월/6개월/1년)의 순 방문자 수를 조회한다.
         * 같은 회원이 여러 번 접속해도 한 명으로 집계한다.
         */
        long getUniqueVisitorCountByPeriod(String period);

        List<VisitorTrendVO> getVisitorTrend();

        /* [모니터링 화면 개편] 기간별 방문자 추이 */
        List<VisitorTrendVO> getVisitorTrendByPeriod(String period);

        List<PopularClickVO> getPopularProductClicks();

        /*
         * [모니터링 공통 조회 기간]
         * 회원 수는 전체 회원 기준으로 유지하고,
         * 방문/주문/매출은 선택 기간 기준으로 조회한다.
         */
        MonitoringSummaryVO getMonitoringSummaryByPeriod(String period);

        /*
         * [모니터링 공통 조회 기간]
         * 선택 기간 기준 상품 클릭 TOP 5를 조회한다.
         */
        List<PopularClickVO> getPopularProductClicksByPeriod(String period);

        // 콘텐츠 관리
        List<ContentManageVO> getContentList(String keyword);

        List<PlatformVO> getPlatformList();

        void updateContent(ContentManageVO content, List<Long> platformNos);

        void registerPlatform(PlatformVO platform);

        void updatePlatform(PlatformVO platform);
}