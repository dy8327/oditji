package com.project.oditji.admin.service;

import java.util.List;

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

public interface AdminService {

    // 대시보드
    AdminVO getDashboardStats();

    // 회원 관리
    List<MemberManageVO> getMemberList(String keyword);
    void suspendMember(Long memberNo);
    void withdrawMember(Long memberNo);
    void restoreMember(Long memberNo);
    void deleteMember(Long memberNo);

    // 콘텐츠 리뷰 관리
    List<ReviewManageVO> getContentReviewList(String tab, String keyword);
    void deleteContentReview(Long reviewNo);

    // 상품 리뷰 관리
    List<ReviewManageVO> getProductReviewList(String tab, String keyword);
    void deleteProductReview(Long reviewNo);

    // 이벤트 관리
    List<EventManageVO> getEventList(String tab, String keyword);
    void approveEvent(Long requestNo);
    void rejectEvent(Long requestNo);

    // 상품 관리
    List<ProductManageVO> getProductRequestList(String tab, String keyword);
    void approveProduct(Long productNo);
    void rejectProduct(Long productNo);

    // 주문 관리
    List<OrderManageVO> getOrderList(String keyword);
    void updateOrderStatus(Long orderNo, String orderStatus);
    void cancelOrder(Long orderItemNo);

    // 환불 관리
    List<OrderManageVO> getRefundList(String keyword);
    void approveRefund(Long cancelNo);
    void rejectRefund(Long cancelNo);

    // 사업자 관리
    List<BusinessManageVO> getBusinessList(String keyword);
    List<BusinessManageVO> getBusinessApprovalList(String keyword);
    void updateBusinessGrade(Long businessNo, String gradeName);
    void approveBusiness(Long businessNo);
    void rejectBusiness(Long businessNo);

    // 정산 관리
    List<SettlementManageVO> getSettlementList(String keyword);
    void confirmSettlement(Long settlementNo);
    void rejectSettlement(Long settlementNo);

    // 모니터링
    List<MonitoringVO> getMonitoringList();

    // 콘텐츠 관리
    List<ContentManageVO> getContentList(String keyword);
    List<PlatformVO> getPlatformList();
    void updateContent(ContentManageVO content, List<Long> platformNos);
    void registerPlatform(PlatformVO platform);
    void updatePlatform(PlatformVO platform);
}
