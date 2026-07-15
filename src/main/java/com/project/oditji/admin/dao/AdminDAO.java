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
import com.project.oditji.admin.vo.ProductManageVO;
import com.project.oditji.admin.vo.ReviewManageVO;
import com.project.oditji.admin.vo.SettlementManageVO;

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

    /**
     * 완전삭제 가능 여부 체크용 (선행 조건: 탈퇴 처리(WITHDRAWN) 상태인지 확인).
     * adminDeleteMember 쿼리 자체도 STATUS='WITHDRAWN' 조건이 걸려 있어,
     * 이 상태가 아니면 삭제문이 0건 처리되어 "삭제가 안 먹는" 것처럼 보인다.
     * 그래서 서비스 단에서 미리 체크해 명확한 안내 메시지를 준다.
     */
    public String selectMemberStatusByNo(Long memberNo) {
        return sqlSession.selectOne("selectMemberStatusByNo", memberNo);
    }

    /**
     * 완전삭제 가능 여부 체크용.
     * BUSINESS로 등록된 회원은 PRODUCT/ORDER_ITEM/SETTLEMENT 등
     * 다른 회원의 데이터와 얽혀 있어 하드 삭제 시 정합성이 깨질 수 있으므로
     * 이 값이 0보다 크면 삭제를 막고 탈퇴 처리로 유도한다.
     */
    public int countBusinessByMemberNo(Long memberNo) {
        return sqlSession.selectOne("countBusinessByMemberNo", memberNo);
    }

    // ---- 완전삭제 전, FK 제약조건 위반(ORA-02292) 방지를 위한 자식 테이블 선삭제 ----
    // 반드시 자식(리프) 테이블부터 부모 방향으로 삭제해야 한다.

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

    public int deleteMemberPlatformByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteMemberPlatformByMember", memberNo);
    }

    public int deleteMemberSocialByMemberNo(Long memberNo) {
        return sqlSession.delete("adminDeleteMemberSocialByMember", memberNo);
    }

    // IDENTITY_VERIFY_LOG.MEMBER_NO는 FK가 ON DELETE SET NULL이라 별도 삭제가 필요 없다.

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

    public int deleteProductReviewReportByReviewNo(Long reviewNo) {
        // PRODUCT_REVIEW는 STATUS 컬럼이 없어 하드 삭제 -> FK(REVIEW_REPORT) 선삭제 필요
        return sqlSession.delete("deleteProductReviewReportByReviewNo", reviewNo);
    }

    public int deleteProductReview(Long reviewNo) {
        return sqlSession.delete("deleteProductReview", reviewNo);
    }

    // ===================== 이벤트 관리 (EVENT) =====================

    public List<EventManageVO> selectEventList(String keyword) {
        // EVENT 테이블에 요청유형 구분 컬럼이 없어 tab 구분 없이 동일 목록 조회
        return sqlSession.selectList("selectEventList", keywordParam(keyword));
    }

    public int updateEventStatus(Long eventNo, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("eventNo", eventNo);
        param.put("status", status);
        return sqlSession.update("updateEventStatus", param);
    }

    // ===================== 상품 관리 (PRODUCT) =====================

    public List<ProductManageVO> selectProductRequestList(String keyword) {
        // PRODUCT 테이블에 요청유형 구분 컬럼이 없어 tab 구분 없이 STATUS 기준 동일 목록 조회
        return sqlSession.selectList("selectProductRequestList", keywordParam(keyword));
    }

    public int updateProductStatus(Long productNo, String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("productNo", productNo);
        param.put("status", status);
        return sqlSession.update("updateProductStatus", param);
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

    // ===================== 콘텐츠 관리 (CONTENT) =====================

    public List<ContentManageVO> selectContentList(String keyword) {
        return sqlSession.selectList("selectContentList", keywordParam(keyword));
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
