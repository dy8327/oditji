package com.project.oditji.admin.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.project.oditji.admin.vo.AdminVO;
import com.project.oditji.admin.vo.BusinessManageVO;
import com.project.oditji.admin.vo.ContentManageVO;
import com.project.oditji.admin.vo.EventManageVO;
import com.project.oditji.admin.vo.MemberManageVO;
import com.project.oditji.admin.vo.MonitoringVO;
import com.project.oditji.admin.vo.OrderManageVO;
import com.project.oditji.admin.vo.PlatformVO;
import com.project.oditji.admin.vo.PopularClickVO;
import com.project.oditji.admin.vo.ProductManageVO;
import com.project.oditji.admin.vo.ReviewManageVO;
import com.project.oditji.admin.vo.SettlementManageVO;
import com.project.oditji.admin.vo.VisitorTrendVO;

@Repository
public class AdminDAO {

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

    public List<MemberManageVO> selectMemberList(String keyword) {
        return sqlSession.selectList("selectMemberList", keywordParam(keyword));
    }

    public int updateMemberStatus(Long memberNo, String status) {

        Map<String, Object> param = new HashMap<>();

        param.put("memberNo", memberNo);
        param.put("status", status);

        return sqlSession.update("updateMemberStatus", param);
    }

    public int restoreMember(Long memberNo) {
        return sqlSession.update("restoreMember", memberNo);
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

    // 최종 MEMBER 삭제
    public int deleteMember(Long memberNo) {
        return sqlSession.delete("adminDeleteMember", memberNo);
    }

    // ===================== 콘텐츠 리뷰 관리 (REVIEW) =====================

    public List<ReviewManageVO> selectContentReviewList(String keyword) {
        return sqlSession.selectList("selectContentReviewList", keywordParam(keyword));
    }

    public List<ReviewManageVO> selectContentReviewReportList(String keyword) {
        return sqlSession.selectList("selectContentReviewReportList", keywordParam(keyword));
    }

    public int deleteContentReview(Long reviewNo) {
        // REVIEW 테이블은 STATUS 컬럼이 있어 소프트 삭제 처리
        return sqlSession.update("adminDeleteContentReview", reviewNo);
    }

    // ===================== 상품 리뷰 관리 (PRODUCT_REVIEW) =====================

    public List<ReviewManageVO> selectProductReviewList(String keyword) {
        return sqlSession.selectList("selectProductReviewList", keywordParam(keyword));
    }

    public List<ReviewManageVO> selectProductReviewReportList(String keyword) {
        return sqlSession.selectList("selectProductReviewReportList", keywordParam(keyword));
    }

    public int adminDeleteProductReview(Long reviewNo) {
        // PRODUCT_REVIEW는 STATUS 컬럼이 없어 하드 삭제.
        // FK_REPORT_PRODUCT_REVIEW가 ON DELETE CASCADE로 걸려 있어
        // 연결된 REVIEW_REPORT 행은 자동으로 함께 삭제된다.
        return sqlSession.delete("deleteProductReview", reviewNo);
    }

    /**
     * 해당 콘텐츠 리뷰에 걸린 WAITING 상태 신고를 전부 ACCEPTED/REJECTED로 변경한다.
     */
    public int updateContentReviewReportStatus(Long reviewNo, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("reviewNo", reviewNo);
        param.put("status", status);
        return sqlSession.update("adminUpdateContentReviewReportStatus", param);
    }

    /**
     * 해당 상품 리뷰에 걸린 WAITING 상태 신고를 전부 ACCEPTED/REJECTED로 변경한다.
     */
    public int updateProductReviewReportStatus(Long reviewNo, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("reviewNo", reviewNo);
        param.put("status", status);
        return sqlSession.update("adminUpdateProductReviewReportStatus", param);
    }

    // ===================== 이벤트 관리 (EVENT) =====================

    public List<EventManageVO> selectAdminEventList(String tab, String keyword) {

        Map<String, Object> param = new HashMap<>();

        param.put("tab", tab);
        param.put("keyword", keyword);

        return sqlSession.selectList(
                "selectAdminEventList",
                param
        );
    }

    public int updateEventStatus(Long eventNo, String status) {

        Map<String, Object> param = new HashMap<>();

        param.put("eventNo", eventNo);
        param.put("status", status);

        return sqlSession.update(
                "updateEventStatus",
                param
        );
    }

    /*
     * [정리됨] selectEventProductByEventNo / applyEventDiscountToProduct는
     * 이벤트 승인 시점에 PRODUCT.DISCOUNT_RATE를 즉시 덮어쓰던 로직에서만
     * 쓰였다. 지금은 goodsMapper.xml의 상품 조회 쿼리가 SYSDATE 기준으로
     * 진행 중인 이벤트를 매번 계산해서 할인율을 실시간으로 반영하므로
     * 더 이상 필요하지 않아 제거했다.
     */

    // ===================== 상품 관리 (PRODUCT) =====================

    public List<ProductManageVO> selectProductRequestList(String tab, String keyword) {
        Map<String, Object> param = keywordParam(keyword);
        param.put("tab", tab);
        return sqlSession.selectList("selectProductRequestList", param);
    }

    public int updateProductStatus(Long productNo, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("productNo", productNo);
        param.put("status", status);
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

    // ===================== 주문 관리 (ORDER_ITEM / DELIVERY) =====================

    public List<OrderManageVO> selectOrderList(String keyword) {
        return sqlSession.selectList("selectOrderList", keywordParam(keyword));
    }

    public int updateDeliveryStatusByOrderNo(Long orderNo, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("orderNo", orderNo);
        param.put("status", status);
        return sqlSession.update("updateDeliveryStatusByOrderNo", param);
    }

    public int updateOrderItemStatusCancel(Long orderItemNo) {
        return sqlSession.update("updateOrderItemStatusCancel", orderItemNo);
    }

    // ===================== 환불 관리 (CANCEL_REQUEST) =====================

    public List<OrderManageVO> selectRefundList(String keyword) {
        return sqlSession.selectList("selectRefundList", keywordParam(keyword));
    }

    public int updateCancelRequestStatus(Long cancelNo, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("cancelNo", cancelNo);
        param.put("status", status);
        return sqlSession.update("updateCancelRequestStatus", param);
    }

    public Long selectOrderItemNoByCancelNo(Long cancelNo) {
        return sqlSession.selectOne("selectOrderItemNoByCancelNo", cancelNo);
    }

    public int updateOrderItemStatus(Long orderItemNo, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("orderItemNo", orderItemNo);
        param.put("status", status);
        return sqlSession.update("updateOrderItemStatus", param);
    }

    // ===================== 사업자 관리 (BUSINESS) =====================

    public List<BusinessManageVO> selectBusinessList(String keyword) {
        return sqlSession.selectList("selectBusinessList", keywordParam(keyword));
    }

    public List<BusinessManageVO> selectBusinessApprovalList(String keyword) {
        return sqlSession.selectList("selectBusinessApprovalList", keywordParam(keyword));
    }

    public int updateBusinessGrade(Long businessNo, String gradeName) {
        Map<String, Object> param = new HashMap<>();
        param.put("businessNo", businessNo);
        param.put("gradeName", gradeName);
        return sqlSession.update("updateBusinessGrade", param);
    }

    public int updateBusinessStatus(Long businessNo, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("businessNo", businessNo);
        param.put("status", status);
        return sqlSession.update("updateBusinessStatus", param);
    }

    // ===================== 정산 관리 (SETTLEMENT) =====================

    public List<SettlementManageVO> selectSettlementList(String keyword) {
        return sqlSession.selectList("selectSettlementList", keywordParam(keyword));
    }

    public int updateSettlementStatus(Long settlementNo, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("settlementNo", settlementNo);
        param.put("status", status);
        return sqlSession.update("updateSettlementStatus", param);
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
        return sqlSession.insert("insertContentPlatform", param);
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