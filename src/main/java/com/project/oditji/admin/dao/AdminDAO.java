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
