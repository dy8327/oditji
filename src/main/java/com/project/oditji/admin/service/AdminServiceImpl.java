package com.project.oditji.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.admin.dao.AdminDAO;
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

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminDAO adminDAO;

    public AdminServiceImpl(AdminDAO adminDAO) {
        this.adminDAO = adminDAO;
    }

    // ===================== 대시보드 =====================

    @Override
    public AdminVO getDashboardStats() {
        return adminDAO.selectDashboardStats();
    }

    // ===================== 회원 관리 =====================

    @Override
    public List<MemberManageVO> getMemberList(String keyword) {
        return adminDAO.selectMemberList(keyword);
    }

    @Override
    public void suspendMember(Long memberNo) {
        adminDAO.updateMemberStatus(memberNo, "BLOCKED");
    }

    @Override
    public void withdrawMember(Long memberNo) {
        adminDAO.updateMemberStatus(memberNo, "WITHDRAWN");
    }

    @Override
    public void restoreMember(Long memberNo) {
        adminDAO.updateMemberStatus(memberNo, "ACTIVE");
    }

    @Override
    public void deleteMember(Long memberNo) {

        adminDAO.deleteMember(memberNo);

    }

    // ===================== 콘텐츠 리뷰 관리 =====================

    @Override
    public List<ReviewManageVO> getContentReviewList(String tab, String keyword) {
        if ("report".equals(tab)) {
            return adminDAO.selectContentReviewReportList(keyword);
        }
        return adminDAO.selectContentReviewList(keyword);
    }

    @Override
    public void deleteContentReview(Long reviewNo) {
        adminDAO.deleteContentReview(reviewNo);
    }

    // ===================== 상품 리뷰 관리 =====================

    @Override
    public List<ReviewManageVO> getProductReviewList(String tab, String keyword) {
        if ("report".equals(tab)) {
            return adminDAO.selectProductReviewReportList(keyword);
        }
        return adminDAO.selectProductReviewList(keyword);
    }

    @Override
    @Transactional
    public void deleteProductReview(Long reviewNo) {
        // PRODUCT_REVIEW는 STATUS 컬럼이 없어 하드 삭제.
        // REVIEW_REPORT가 PRODUCT_REVIEW_NO를 참조하므로 신고 내역을 먼저 삭제한다.
        adminDAO.deleteProductReviewReportByReviewNo(reviewNo);
        adminDAO.deleteProductReview(reviewNo);
    }

    // ===================== 이벤트 관리 =====================

    @Override
    public List<EventManageVO> getEventList(String tab, String keyword) {
        // EVENT 테이블에 요청유형 구분 컬럼이 없어 tab 값과 무관하게 동일 목록을 조회한다.
        return adminDAO.selectEventList(keyword);
    }

    @Override
    public void approveEvent(Long requestNo) {
        adminDAO.updateEventStatus(requestNo, "APPROVED");
    }

    @Override
    public void rejectEvent(Long requestNo) {
        adminDAO.updateEventStatus(requestNo, "REJECTED");
    }

    // ===================== 상품 관리 =====================

    @Override
    public List<ProductManageVO> getProductRequestList(String tab, String keyword) {
        // PRODUCT 테이블에 요청유형 구분 컬럼이 없어 tab 값과 무관하게 동일 목록을 조회한다.
        return adminDAO.selectProductRequestList(keyword);
    }

    @Override
    public void approveProduct(Long productNo) {
        adminDAO.updateProductStatus(productNo, "APPROVED");
    }

    @Override
    public void rejectProduct(Long productNo) {
        adminDAO.updateProductStatus(productNo, "REJECTED");
    }

    // ===================== 주문 관리 =====================

    @Override
    public List<OrderManageVO> getOrderList(String keyword) {
        return adminDAO.selectOrderList(keyword);
    }

    @Override
    public void updateOrderStatus(Long orderNo, String orderStatus) {
        adminDAO.updateDeliveryStatusByOrderNo(orderNo, orderStatus);
    }

    @Override
    public void cancelOrder(Long orderItemNo) {
        adminDAO.updateOrderItemStatusCancel(orderItemNo);
    }

    // ===================== 환불 관리 =====================

    @Override
    public List<OrderManageVO> getRefundList(String keyword) {
        return adminDAO.selectRefundList(keyword);
    }

    @Override
    @Transactional
    public void approveRefund(Long cancelNo) {
        adminDAO.updateCancelRequestStatus(cancelNo, "APPROVED");
        Long orderItemNo = adminDAO.selectOrderItemNoByCancelNo(cancelNo);
        if (orderItemNo != null) {
            adminDAO.updateOrderItemStatus(orderItemNo, "CANCELED");
        }
    }

    @Override
    public void rejectRefund(Long cancelNo) {
        adminDAO.updateCancelRequestStatus(cancelNo, "REJECTED");
    }

    // ===================== 사업자 관리 =====================

    @Override
    public List<BusinessManageVO> getBusinessList(String keyword) {
        return adminDAO.selectBusinessList(keyword);
    }

    @Override
    public List<BusinessManageVO> getBusinessApprovalList(String keyword) {
        return adminDAO.selectBusinessApprovalList(keyword);
    }

    @Override
    public void updateBusinessGrade(Long businessNo, String gradeName) {
        adminDAO.updateBusinessGrade(businessNo, gradeName);
    }

    @Override
    public void approveBusiness(Long businessNo) {
        adminDAO.updateBusinessStatus(businessNo, "APPROVED");
    }

    @Override
    public void rejectBusiness(Long businessNo) {
        adminDAO.updateBusinessStatus(businessNo, "REJECTED");
    }

    // ===================== 정산 관리 =====================

    @Override
    public List<SettlementManageVO> getSettlementList(String keyword) {
        return adminDAO.selectSettlementList(keyword);
    }

    @Override
    public void confirmSettlement(Long settlementNo) {
        adminDAO.updateSettlementStatus(settlementNo, "DONE");
    }

    @Override
    public void rejectSettlement(Long settlementNo) {
        adminDAO.updateSettlementStatus(settlementNo, "REJECTED");
    }

    // ===================== 모니터링 =====================

    @Override
    public List<MonitoringVO> getMonitoringList() {
        return adminDAO.selectMonitoringList();
    }

    // ===================== 콘텐츠 관리 =====================

    @Override
    public List<ContentManageVO> getContentList(String keyword) {
        return adminDAO.selectContentList(keyword);
    }

    @Override
    public List<PlatformVO> getPlatformList() {
        return adminDAO.selectPlatformList();
    }

    @Override
    @Transactional
    public void updateContent(ContentManageVO content, List<Long> platformNos) {
        adminDAO.updateContent(content);

        if (platformNos != null) {
            adminDAO.deleteContentPlatforms(content.getContentNo());
            for (Long platformNo : platformNos) {
                adminDAO.insertContentPlatform(content.getContentNo(), platformNo);
            }
        }
    }

    @Override
    public void registerPlatform(PlatformVO platform) {
        adminDAO.insertPlatform(platform);
    }

    @Override
    public void updatePlatform(PlatformVO platform) {
        adminDAO.updatePlatform(platform);
    }
}
