package com.project.oditji.admin.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.project.oditji.admin.vo.AdminVO;
import com.project.oditji.admin.vo.BusinessManageVO;
import com.project.oditji.admin.vo.BusinessStatVO;
import com.project.oditji.admin.vo.ContentManageVO;
import com.project.oditji.admin.vo.EventManageVO;
import com.project.oditji.admin.vo.EventStatVO;
import com.project.oditji.admin.vo.MemberManageVO;
import com.project.oditji.admin.vo.MemberStatVO;
import com.project.oditji.admin.vo.MonitoringVO;
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

@Repository
public class AdminDAO {

    private static final String PARAM_STATUS = "status";
    private static final String PARAM_BUSINESS_NO = "businessNo";

    private final SqlSessionTemplate sqlSession;

    public AdminDAO(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }

    /**
     * keyword 파라미터를 Map으로 감싼다.
     * 단일 String 파라미터를 그대로 넘기면 매퍼의 &lt;if test="keyword != null"&gt; 같은
     * OGNL 조건이 불안정하게 동작할 수 있어, 항상 Map 형태로 감싸서 전달한다.
     */
    private Map<String, Object> keywordParam(String keyword) {
        Map<String, Object> param = new HashMap<>();
        param.put("keyword", keyword);
        return param;
    }

    // ===================== 대시보드 =====================

    public AdminVO selectDashboardStats() {
        return sqlSession.selectOne("selectDashboardStats");
    }

    // ===================== 회원 관리 (MEMBER) =====================

    public List<MemberManageVO> selectMemberList(Map<String, Object> param) {
        return sqlSession.selectList("selectMemberList", param);
    }

    public int selectMemberListCount(Map<String, Object> param) {
        return sqlSession.selectOne("selectMemberListCount", param);
    }

    public MemberStatVO selectMemberStats() {
        return sqlSession.selectOne("selectMemberStats");
    }

    public int updateMemberStatus(Long memberNo, String status) {

        Map<String, Object> param = new HashMap<>();

        param.put("memberNo", memberNo);
        param.put(PARAM_STATUS, status);

        return sqlSession.update("updateMemberStatus", param);
    }

    public int restoreMember(Long memberNo) {
        return sqlSession.update("restoreMember", memberNo);
    }

    /**
     * 전달받은 회원번호 중, 이미 본인이 직접 탈퇴하여 자동삭제 대기 중(STATUS = 'WITHDRAWN')인
     * 회원번호만 골라 반환한다. 일괄 처리(정지/복구/완전삭제) 시 해당 회원을 걸러내기 위한 서버측 방어용.
     */
    public List<Long> selectWithdrawnMemberNos(List<Long> memberNos) {
        return sqlSession.selectList("selectWithdrawnMemberNos", memberNos);
    }

    // ===================== 탈퇴 회원 자동 삭제 =====================

    /**
     * 탈퇴 후 7일이 지난 회원 조회
     */
    public List<Long> selectExpiredWithdrawMembers() {
        return sqlSession.selectList("selectExpiredWithdrawMembers");
    }

    // ===================== 회원 삭제 전 FK 데이터 삭제 =====================

    public int deleteReviewReportByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteReviewReportByMember", memberNo);
    }

    public int deleteSettlementByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteSettlementByMember", memberNo);
    }

    public int deleteProductReviewByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteProductReviewByMember", memberNo);
    }

    public int deleteCancelRequestByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteCancelRequestByMember", memberNo);
    }

    public int deleteDeliveryByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteDeliveryByMember", memberNo);
    }

    public int deleteOrderItemByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteOrderItemByMember", memberNo);
    }

    public int deleteOrdersByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteOrdersByMember", memberNo);
    }

    public int deleteCartItemByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteCartItemByMember", memberNo);
    }

    public int deleteCartByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteCartByMember", memberNo);
    }

    public int deleteProductWishByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteProductWishByMember", memberNo);
    }

    public int deleteFavoriteByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteFavoriteByMember", memberNo);
    }

    public int deleteReviewByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteReviewByMember", memberNo);
    }

    public int deleteProductClickLogByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteProductClickLogByMember", memberNo);
    }

    public int deleteAccessLogByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteAccessLogByMember", memberNo);
    }

    public int deleteAdminLogByAdminNo(Long memberNo) {
        return sqlSession.delete("adminDeleteAdminLogByAdmin", memberNo);
    }

    public int deleteContentViewHistoryByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteContentViewHistoryByMember", memberNo);
    }

    public int deleteMemberPlatformByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteMemberPlatformByMember", memberNo);
    }

    public int deleteMemberSocialByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteMemberSocialByMember", memberNo);
    }

    public int deleteSubscriptionResultByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteSubscriptionResultByMember", memberNo);
    }

    public int deleteNotificationSettingByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteNotificationSettingByMember", memberNo);
    }

    public int deleteSearchKeywordHistoryByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteSearchKeywordHistoryByMember", memberNo);
    }

    // 최종 MEMBER 삭제
    public int deleteMember(Long memberNo) {
        return sqlSession.delete("adminDeleteMember", memberNo);
    }

    // ===================== 콘텐츠 리뷰 관리 (REVIEW) =====================

    public List<ReviewManageVO> selectContentReviewList(Map<String, Object> param) {
        return sqlSession.selectList("selectContentReviewList", param);
    }

    public int selectContentReviewListCount(Map<String, Object> param) {
        return sqlSession.selectOne("selectContentReviewListCount", param);
    }

    public List<ReviewManageVO> selectContentReviewReportList(Map<String, Object> param) {
        return sqlSession.selectList("selectContentReviewReportList", param);
    }

    public int selectContentReviewReportListCount(Map<String, Object> param) {
        return sqlSession.selectOne("selectContentReviewReportListCount", param);
    }

    public ReviewStatVO selectContentReviewStats() {
        return sqlSession.selectOne("selectContentReviewStats");
    }

    public int deleteContentReview(Long reviewNo) {
        // REVIEW 테이블은 STATUS 컬럼이 있어 소프트 삭제 처리
        return sqlSession.update("adminDeleteContentReview", reviewNo);
    }

    // ===================== 상품 리뷰 관리 (PRODUCT_REVIEW) =====================

    public List<ReviewManageVO> selectProductReviewList(Map<String, Object> param) {
        return sqlSession.selectList("selectProductReviewList", param);
    }

    public int selectProductReviewListCount(Map<String, Object> param) {
        return sqlSession.selectOne("selectProductReviewListCount", param);
    }

    public List<ReviewManageVO> selectProductReviewReportList(Map<String, Object> param) {
        return sqlSession.selectList("selectProductReviewReportList", param);
    }

    public int selectProductReviewReportListCount(Map<String, Object> param) {
        return sqlSession.selectOne("selectProductReviewReportListCount", param);
    }

    public ReviewStatVO selectProductReviewStats() {
        return sqlSession.selectOne("selectProductReviewStats");
    }

    public int adminDeleteProductReview(Long reviewNo) {
        // PRODUCT_REVIEW는 STATUS 컬럼이 없어 하드 삭제.
        // FK_REPORT_PRODUCT_REVIEW가 ON DELETE CASCADE로 걸려 있어
        // 연결된 REVIEW_REPORT 행은 자동으로 함께 삭제된다.
        return sqlSession.delete("deleteProductReview", reviewNo);
    }

    /**
     * 콘텐츠 리뷰 신고 처리 전에 WAITING 상태 신고자 회원번호를 조회합니다.
     */
    public List<Long> selectWaitingContentReviewReporterMemberNos(Long reviewNo) {
        return sqlSession.selectList(
                "selectWaitingContentReviewReporterMemberNos",
                reviewNo);
    }

    /**
     * 상품 리뷰 신고 처리 전에 WAITING 상태 신고자 회원번호를 조회합니다.
     * 상품 리뷰 승인 시 REVIEW_REPORT가 함께 삭제되므로 반드시 삭제 전에 호출합니다.
     */
    public List<Long> selectWaitingProductReviewReporterMemberNos(Long reviewNo) {
        return sqlSession.selectList(
                "selectWaitingProductReviewReporterMemberNos",
                reviewNo);
    }

    /**
     * 해당 콘텐츠 리뷰에 걸린 WAITING 상태 신고를 전부 ACCEPTED/REJECTED로 변경한다.
     */
    public int updateContentReviewReportStatus(Long reviewNo, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("reviewNo", reviewNo);
        param.put(PARAM_STATUS, status);
        return sqlSession.update("adminUpdateContentReviewReportStatus", param);
    }

    /**
     * 해당 상품 리뷰에 걸린 WAITING 상태 신고를 전부 ACCEPTED/REJECTED로 변경한다.
     */
    public int updateProductReviewReportStatus(Long reviewNo, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("reviewNo", reviewNo);
        param.put(PARAM_STATUS, status);
        return sqlSession.update("adminUpdateProductReviewReportStatus", param);
    }

    // ===================== 이벤트 관리 (EVENT) =====================

    public List<EventManageVO> selectAdminEventList(Map<String, Object> param) {

        return sqlSession.selectList(
                "selectAdminEventList",
                param);
    }

    public int selectAdminEventListCount(Map<String, Object> param) {

        return sqlSession.selectOne(
                "selectAdminEventListCount",
                param);
    }

    public EventStatVO selectEventStats() {

        return sqlSession.selectOne("selectEventStats");
    }

    public int updateEventStatus(Long eventNo, String status) {

        Map<String, Object> param = new HashMap<>();

        param.put("eventNo", eventNo);
        param.put(PARAM_STATUS, status);

        return sqlSession.update(
                "updateEventStatus",
                param);
    }

    /*
     * [정리됨] selectEventProductByEventNo / applyEventDiscountToProduct는
     * 이벤트 승인 시점에 PRODUCT.DISCOUNT_RATE를 즉시 덮어쓰던 로직에서만
     * 쓰였다. 지금은 goodsMapper.xml의 상품 조회 쿼리가 SYSDATE 기준으로
     * 진행 중인 이벤트를 매번 계산해서 할인율을 실시간으로 반영하므로
     * 더 이상 필요하지 않아 제거했다.
     */

    // ===================== 상품 관리 (PRODUCT) =====================

    public List<ProductManageVO> selectProductRequestList(Map<String, Object> param) {
        return sqlSession.selectList("selectProductRequestList", param);
    }

    public int selectProductRequestListCount(Map<String, Object> param) {
        return sqlSession.selectOne("selectProductRequestListCount", param);
    }

    public ProductStatVO selectProductStats() {
        return sqlSession.selectOne("selectProductStats");
    }

    public int updateProductStatus(Long productNo, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("productNo", productNo);
        param.put(PARAM_STATUS, status);
        return sqlSession.update("updateProductStatus", param);
    }

    /**
     * 상품 승인/반려 처리 전에 현재 상태를 확인한다.
     * DELETE_REQUESTED 상태이면 일반 승인 상태 변경이 아니라
     * 상품 최종 삭제 또는 삭제 요청 반려 로직을 수행한다.
     */
    public String selectProductStatusByNo(Long productNo) {
        return sqlSession.selectOne("selectProductStatusByNo", productNo);
    }

    /**
     * 상품과 연결된 주문 이력 수를 확인한다.
     * 주문 이력이 존재하면 주문/매출 기록 보존을 위해 상품 완전삭제를 막는다.
     */
    public int countOrderItemByProductNo(Long productNo) {
        return sqlSession.selectOne("countOrderItemByProductNo", productNo);
    }

    /**
     * DB 삭제 이후 서버에 저장된 이미지 파일도 정리하기 위해
     * PRODUCT_IMAGE의 전체 이미지 경로를 먼저 조회한다.
     */
    public List<String> selectProductImagePathList(Long productNo) {
        return sqlSession.selectList("selectProductImagePathList", productNo);
    }

    // ---- 상품 완전삭제 전, FK 제약조건 위반 방지를 위한 자식 테이블 선삭제 ----

    public int deleteProductReviewByProductNo(Long productNo) {
        // REVIEW_REPORT는 FK_REPORT_PRODUCT_REVIEW의 ON DELETE CASCADE로
        // 이 상품의 리뷰들을 삭제할 때 자동으로 함께 정리된다.
        return sqlSession.delete("adminDeleteProductReviewByProduct", productNo);
    }

    public int deleteCartItemByProductNo(Long productNo) {
        return sqlSession.delete("adminDeleteCartItemByProduct", productNo);
    }

    public int deleteProductWishByProductNo(Long productNo) {
        return sqlSession.delete("adminDeleteProductWishByProduct", productNo);
    }

    public int deleteProductClickLogByProductNo(Long productNo) {
        return sqlSession.delete("adminDeleteProductClickLogByProduct", productNo);
    }

    public int deleteEventProductByProductNo(Long productNo) {
        return sqlSession.delete("adminDeleteEventProductByProduct", productNo);
    }

    public int deleteProductImageByProductNo(Long productNo) {
        return sqlSession.delete("adminDeleteProductImageByProduct", productNo);
    }

    public int deleteProduct(Long productNo) {
        return sqlSession.delete("adminDeleteProduct", productNo);
    }

    // ===================== 주문 조회 (ORDER_ITEM / DELIVERY, 조회 전용)
    // =====================
    // 배송 상태 변경/주문 취소는 사업자(Business) 담당이므로 관리자 DAO에는 조회만 둔다.

    public List<OrderManageVO> selectOrderList(Map<String, Object> param) {
        return sqlSession.selectList("selectOrderList", param);
    }

    public int selectOrderListCount(Map<String, Object> param) {
        return sqlSession.selectOne("selectOrderListCount", param);
    }

    public OrderStatVO selectOrderStats() {
        return sqlSession.selectOne("selectOrderStats");
    }

    // ===================== 환불 조회 (CANCEL_REQUEST, 조회 전용) =====================
    // 환불 승인/거절은 사업자 담당이므로 관리자 DAO에는 조회만 둔다.

    public List<OrderManageVO> selectRefundList(Map<String, Object> param) {
        return sqlSession.selectList("selectRefundList", param);
    }

    public int selectRefundListCount(Map<String, Object> param) {
        return sqlSession.selectOne("selectRefundListCount", param);
    }

    // ===================== 사업자 관리 (BUSINESS) =====================

    public List<BusinessManageVO> selectBusinessList(Map<String, Object> param) {
        return sqlSession.selectList("selectBusinessList", param);
    }

    public int selectBusinessListCount(Map<String, Object> param) {
        return sqlSession.selectOne("selectBusinessListCount", param);
    }

    public List<BusinessManageVO> selectBusinessApprovalList(Map<String, Object> param) {
        return sqlSession.selectList("selectBusinessApprovalList", param);
    }

    public int selectBusinessApprovalListCount(Map<String, Object> param) {
        return sqlSession.selectOne("selectBusinessApprovalListCount", param);
    }

    public BusinessStatVO selectBusinessStats() {
        return sqlSession.selectOne("selectBusinessStats");
    }

    public int updateBusinessGrade(Long businessNo, String gradeName) {
        Map<String, Object> param = new HashMap<>();
        param.put(PARAM_BUSINESS_NO, businessNo);
        param.put("gradeName", gradeName);
        return sqlSession.update("updateBusinessGrade", param);
    }

    /*
     * [사업자 자동 등급 관리 추가]
     * 결제 완료 후 누적 실매출을 기준으로 현재 등급보다 높은 등급으로 자동 승급한다.
     */
    public int updateBusinessGradesBySales() {
        return sqlSession.update("updateBusinessGradesBySales");
    }

    /*
     * [사업자 등급별 수수료율 정산 반영 추가]
     * 관리자가 수동으로 등급을 변경한 사업자의 이번 달 미확정 정산을
     * 변경된 등급의 수수료율로 다시 계산한다.
     */
    public int updateCurrentMonthSettlementRateByBusiness(Long businessNo) {
        return sqlSession.update(
                "updateCurrentMonthSettlementRateByBusiness",
                businessNo);
    }

    /*
     * [사업자 자동 등급별 수수료율 정산 반영 추가]
     * 자동 승급 처리 후 모든 사업자의 이번 달 미확정 정산을
     * 현재 등급 정책에 맞게 다시 계산한다.
     */
    public int updateCurrentMonthSettlementRates() {
        return sqlSession.update("updateCurrentMonthSettlementRates");
    }

    public int updateBusinessStatus(Long businessNo, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put(PARAM_BUSINESS_NO, businessNo);
        param.put(PARAM_STATUS, status);
        return sqlSession.update("updateBusinessStatus", param);
    }

    // ===================== 정산 관리 (SETTLEMENT) =====================

    public SettlementStatVO selectSettlementStats() {
        return sqlSession.selectOne("selectSettlementStats");
    }

    /* 정산 요청 목록 조회 */
    public List<SettlementRequestVO> selectSettlementRequestList(Map<String, Object> param) {
        return sqlSession.selectList("selectSettlementRequestList", param);
    }

    /* 정산 요청 목록 수 조회 */
    public int selectSettlementRequestListCount(Map<String, Object> param) {
        return sqlSession.selectOne("selectSettlementRequestListCount", param);
    }

    /* 정산 요청 단건 조회 */
    public SettlementRequestVO selectSettlementRequest(Long requestNo) {
        return sqlSession.selectOne("selectSettlementRequest", requestNo);
    }

    /* 정산 요청 승인·반려 처리 */
    public int updateSettlementRequestStatus(Long requestNo, String status, String rejectReason) {
        Map<String, Object> param = new HashMap<>();
        param.put("requestNo", requestNo);
        param.put(PARAM_STATUS, status);
        param.put("rejectReason", rejectReason);
        return sqlSession.update("updateSettlementRequestStatus", param);
    }

    /* 지급 완료된 요청의 정산 원장 완료 처리 */
    public int completeSettlementItems(Long requestNo) {
        return sqlSession.update("completeSettlementItems", requestNo);
    }

    /* 반려된 요청의 정산 원장 연결 해제 */
    public int releaseRejectedSettlementItems(Long requestNo) {
        return sqlSession.update("releaseRejectedSettlementItems", requestNo);
    }

    // ===================== 시스템 관리 (모니터링) =====================

    public List<MonitoringVO> selectMonitoringList() {
        return sqlSession.selectList("selectMonitoringList");
    }

    public List<VisitorTrendVO> selectVisitorTrend() {
        return sqlSession.selectList("selectVisitorTrend");
    }

    public List<PopularClickVO> selectPopularProductClicks() {
        return sqlSession.selectList("selectPopularProductClicks");
    }

    // ===================== 콘텐츠 관리 (CONTENT) =====================

    public List<ContentManageVO> selectAdminContentList(String keyword) {
        return sqlSession.selectList("selectAdminContentList", keywordParam(keyword));
    }

    public int updateContent(ContentManageVO content) {
        return sqlSession.update("updateContent", content);
    }

    public int deleteContentPlatforms(Long contentNo) {
        return sqlSession.delete("deleteContentPlatforms", contentNo);
    }

    public int insertContentPlatform(Long contentNo, Long platformNo) {
        Map<String, Object> param = new HashMap<>();
        param.put("contentNo", contentNo);
        param.put("platformNo", platformNo);
        return sqlSession.insert("com.project.oditji.admin.dao.AdminDAO.insertContentPlatform", param);
    }

    // ===================== OTT 플랫폼 관리 (OTT_PLATFORM) =====================

    public List<PlatformVO> selectPlatformList() {
        return sqlSession.selectList("selectPlatformList");
    }

    public int insertPlatform(PlatformVO platform) {
        return sqlSession.insert("insertPlatform", platform);
    }

    public int updatePlatform(PlatformVO platform) {
        return sqlSession.update("updatePlatform", platform);
    }
}