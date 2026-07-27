package com.project.oditji.admin.service;

import java.util.List;

import com.project.oditji.admin.vo.AdminVO;
import com.project.oditji.admin.vo.BusinessManageVO;
import com.project.oditji.admin.vo.ContentManageVO;
import com.project.oditji.admin.vo.EventManageVO;
import com.project.oditji.admin.vo.MemberManageVO;
import com.project.oditji.admin.vo.MemberStatVO;
import com.project.oditji.admin.vo.MonitoringVO;
import com.project.oditji.admin.vo.OrderManageVO;
import com.project.oditji.admin.vo.PlatformVO;
import com.project.oditji.admin.vo.PopularClickVO;
import com.project.oditji.admin.vo.ProductManageVO;
import com.project.oditji.admin.vo.ReviewManageVO;
import com.project.oditji.admin.vo.SettlementManageVO;
import com.project.oditji.admin.vo.VisitorTrendVO;

public interface AdminService {

    // 대시보드
    AdminVO getDashboardStats();

    // 회원 관리
    // memberType: all(기본,전체 유저) / general(일반 회원) / sns(SNS 로그인 유저) / business(사업자
    // 회원)
    List<MemberManageVO> getMemberList(String keyword, String searchType, String status, String memberType, int page,
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
    List<ReviewManageVO> getContentReviewList(String tab, String keyword);

    void deleteContentReview(Long reviewNo);

    void approveContentReviewReport(Long reviewNo);

    void rejectContentReviewReport(Long reviewNo);

    // 상품 리뷰 관리
    List<ReviewManageVO> getProductReviewList(String tab, String keyword);

    void deleteProductReview(Long reviewNo);

    void approveProductReviewReport(Long reviewNo);

    void rejectProductReviewReport(Long reviewNo);

    // 이벤트 관리
    List<EventManageVO> getEventList(String tab, String keyword);

    void approveEvent(Long eventNo);

    void rejectEvent(Long eventNo);

    // 상품 관리
    List<ProductManageVO> getProductRequestList(String tab, String keyword);

    void approveProduct(Long productNo);

    void rejectProduct(Long productNo);

    // 주문 조회 (조회 전용 - 처리는 사업자 담당)
    List<OrderManageVO> getOrderList(String keyword);

    // 환불 조회 (조회 전용 - 승인/거절은 사업자 담당)
    List<OrderManageVO> getRefundList(String keyword, String status);

    // 사업자 관리
    List<BusinessManageVO> getBusinessList(String keyword);

    List<BusinessManageVO> getBusinessApprovalList(String keyword);

    void updateBusinessGrade(Long businessNo, String gradeName);

    void approveBusiness(Long businessNo);

    void rejectBusiness(Long businessNo);

    // 정산 관리
    List<SettlementManageVO> getSettlementList(String keyword);

    /* [수정] 사업자와 정산 월을 기준으로 해당 월 요청 건 전체를 처리한다. */
    void confirmSettlement(Long businessNo, String settlementMonth);

    void rejectSettlement(Long businessNo, String settlementMonth);

    // 모니터링
    List<MonitoringVO> getMonitoringList();

    List<VisitorTrendVO> getVisitorTrend();

    List<PopularClickVO> getPopularProductClicks();

    // 콘텐츠 관리
    List<ContentManageVO> getContentList(String keyword);

    List<PlatformVO> getPlatformList();

    void updateContent(ContentManageVO content, List<Long> platformNos);

    void registerPlatform(PlatformVO platform);

    void updatePlatform(PlatformVO platform);
}