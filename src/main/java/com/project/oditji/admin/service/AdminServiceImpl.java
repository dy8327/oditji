package com.project.oditji.admin.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.common.util.PaginationUtil;
import com.project.oditji.admin.vo.AdminVO;
import com.project.oditji.admin.vo.BusinessManageVO;
import com.project.oditji.admin.vo.ContentManageVO;
import com.project.oditji.admin.vo.EventManageVO;
import com.project.oditji.admin.vo.EventStatVO;
import com.project.oditji.admin.vo.MemberManageVO;
import com.project.oditji.admin.vo.MemberStatVO;
import com.project.oditji.admin.vo.MonitoringVO;
import com.project.oditji.admin.vo.BusinessStatVO;
import com.project.oditji.admin.vo.OrderManageVO;
import com.project.oditji.admin.vo.OrderStatVO;
import com.project.oditji.admin.vo.PlatformVO;
import com.project.oditji.admin.vo.PopularClickVO;
import com.project.oditji.admin.vo.ProductManageVO;
import com.project.oditji.admin.vo.ProductStatVO;
import com.project.oditji.admin.vo.ReviewManageVO;
import com.project.oditji.admin.vo.ReviewStatVO;
import com.project.oditji.admin.vo.SettlementManageVO;
import com.project.oditji.admin.vo.SettlementStatVO;
import com.project.oditji.admin.vo.VisitorTrendVO;
import com.project.oditji.notification.service.NotificationService;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminDAO adminDAO;
    private final NotificationService notificationService;

    /*
     * 상품 삭제 승인 후 실제 업로드 파일까지 정리하기 위한 경로이다.
     * 사업자 상품 등록 서비스에서 사용하는 설정값과 동일한 값을 사용한다.
     */
    private final Path productUploadDirectory;

    public AdminServiceImpl(
            AdminDAO adminDAO,
            NotificationService notificationService,
            @Value("${oditji.upload.product-path:uploads/product}") String productUploadPath) {

        this.adminDAO = adminDAO;
        this.notificationService = notificationService;
        this.productUploadDirectory = Paths.get(productUploadPath)
                .toAbsolutePath()
                .normalize();
    }

    // ===================== 대시보드 =====================

    @Override
    public AdminVO getDashboardStats() {
        return adminDAO.selectDashboardStats();
    }

    // ===================== 회원 관리 =====================

    // 본인 직접 탈퇴 후 자동삭제까지 유예되는 기간(일). 스케줄러(MemberDeleteScheduler)의 7일 기준과 맞춘다.
    private static final int WITHDRAW_AUTO_DELETE_DAYS = 7;

    @Override
    public List<MemberManageVO> getMemberList(String keyword, String searchType, String status, String memberType,
            int page, int pageSize) {

        Map<String, Object> param = memberSearchParam(keyword, searchType, status, memberType);

        int currentPage = (page < 1) ? 1 : page;

        param.put("offset", (currentPage - 1) * pageSize);
        param.put("pageSize", pageSize);

        List<MemberManageVO> memberList = adminDAO.selectMemberList(param);
        memberList.forEach(this::fillRemainingDeleteDays);

        return memberList;
    }

    @Override
    public int getMemberListCount(String keyword, String searchType, String status, String memberType) {
        return adminDAO.selectMemberListCount(memberSearchParam(keyword, searchType, status, memberType));
    }

    @Override
    public MemberStatVO getMemberStats() {
        return adminDAO.selectMemberStats();
    }

    /** keyword 하나만 필요한 목록 조회를 위한 파라미터 Map 생성 헬퍼. */
    private Map<String, Object> keywordParam(String keyword) {
        Map<String, Object> param = new HashMap<>();
        param.put("keyword", keyword);
        return param;
    }

    /**
     * 리뷰/이벤트/상품/주문/사업자/정산 등 회원 관리 외 목록 조회에서 공통으로 쓰는
     * offset/pageSize 파라미터를 채워 넣는다. (memberSearchParam과 동일한 방식)
     */
    private Map<String, Object> withPaging(Map<String, Object> param, int page, int pageSize) {
        param.put("offset", PaginationUtil.offset(page, pageSize));
        param.put("pageSize", pageSize);
        return param;
    }

    /** keyword + searchType만 필요한 목록 조회를 위한 파라미터 Map 생성 헬퍼. (memberSearchParam의 축소판) */
    private Map<String, Object> keywordSearchTypeParam(String keyword, String searchType) {
        Map<String, Object> param = new HashMap<>();
        param.put("keyword", keyword);
        param.put("searchType", searchType);
        return param;
    }

    private Map<String, Object> memberSearchParam(String keyword, String searchType, String status, String memberType) {
        Map<String, Object> param = new HashMap<>();
        param.put("keyword", keyword);
        param.put("searchType", searchType);
        param.put("status", status);
        param.put("memberType", memberType);
        return param;
    }

    /*
     * 본인이 직접 탈퇴(STATUS = 'WITHDRAWN')한 회원에 한해, 자동삭제까지 남은 일수를 계산해 채운다.
     * 스케줄러는 WITHDRAWN_AT 기준 7일이 지나면 삭제하므로, 남은 일수 = 7 - (오늘 - 탈퇴일).
     * 음수가 되지 않도록(다음 스케줄 실행 전까지의 짧은 텀 등) 0으로 하한을 둔다.
     */
    private void fillRemainingDeleteDays(MemberManageVO member) {

        if (!"WITHDRAWN".equals(member.getStatus()) || member.getWithdrawnAt() == null) {
            return;
        }

        long elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(
                member.getWithdrawnAt().toInstant(),
                java.time.Instant.now());

        int remaining = (int) (WITHDRAW_AUTO_DELETE_DAYS - elapsedDays);
        member.setRemainingDeleteDays(Math.max(remaining, 0));
    }

    @Override
    public void suspendMember(Long memberNo) {
        adminDAO.updateMemberStatus(memberNo, "BLOCKED");
    }

    @Override
    public void restoreMember(Long memberNo) {
        adminDAO.updateMemberStatus(memberNo, "ACTIVE");
    }

    /*
     * 목록 화면에서 체크박스로 선택한 회원들을 한 번의 요청으로 일괄 처리한다.
     * delete는 기존 deleteMember(단건)를 그대로 재사용해 FK 정리 순서를 그대로 유지한다.
     * 화면 체크박스에서 이미 선택 자체를 막고 있지만, 본인이 직접 탈퇴하여 자동삭제 대기 중인
     * (STATUS = 'WITHDRAWN') 회원이 우회 요청 등으로 포함되어 들어올 경우를 대비해
     * 서버에서 한 번 더 걸러내고, 걸러낸 회원 수를 반환한다.
     */
    @Override
    @Transactional
    public int bulkMemberAction(List<Long> memberNos, String action) {

        if (memberNos == null || memberNos.isEmpty()) {
            return 0;
        }

        List<Long> withdrawnNos = adminDAO.selectWithdrawnMemberNos(memberNos);

        for (Long memberNo : memberNos) {

            if (withdrawnNos.contains(memberNo)) {
                continue;
            }

            switch (action) {
                case "suspend" -> adminDAO.updateMemberStatus(memberNo, "BLOCKED");
                case "restore" -> adminDAO.updateMemberStatus(memberNo, "ACTIVE");
                case "delete" -> deleteMember(memberNo);
                default -> throw new IllegalArgumentException("알 수 없는 처리 유형입니다.");
            }
        }

        return withdrawnNos.size();
    }

    @Override
    @Transactional
    public void deleteMember(Long memberNo) {

        // 회원 삭제 전 FK 참조 데이터 제거

        adminDAO.deleteReviewReportByMemberNo(memberNo);
        adminDAO.deleteSettlementByMemberNo(memberNo);

        adminDAO.deleteProductReviewByMemberNo(memberNo);
        adminDAO.deleteCancelRequestByMemberNo(memberNo);

        adminDAO.deleteDeliveryByMemberNo(memberNo);
        adminDAO.deleteOrderItemByMemberNo(memberNo);
        adminDAO.deleteOrdersByMemberNo(memberNo);

        adminDAO.deleteCartItemByMemberNo(memberNo);
        adminDAO.deleteCartByMemberNo(memberNo);

        adminDAO.deleteProductWishByMemberNo(memberNo);
        adminDAO.deleteFavoriteByMemberNo(memberNo);

        adminDAO.deleteReviewByMemberNo(memberNo);
        adminDAO.deleteContentViewHistoryByMemberNo(memberNo);

        adminDAO.deleteProductClickLogByMemberNo(memberNo);
        adminDAO.deleteAccessLogByMemberNo(memberNo);

        adminDAO.deleteAdminLogByAdminNo(memberNo);

        adminDAO.deleteMemberPlatformByMemberNo(memberNo);
        adminDAO.deleteMemberSocialByMemberNo(memberNo);

        // MEMBER 최종 삭제
        adminDAO.deleteMember(memberNo);
    }

    @Override
    @Transactional
    public void deleteExpiredWithdrawMembers() {

        List<Long> memberList = adminDAO.selectExpiredWithdrawMembers();

        for (Long memberNo : memberList) {
            deleteMember(memberNo);
        }
    }

    // ===================== 콘텐츠 리뷰 관리 =====================

    @Override
    public List<ReviewManageVO> getContentReviewList(String tab, String keyword, String searchType, int page,
            int pageSize) {
        Map<String, Object> param = withPaging(keywordSearchTypeParam(keyword, searchType), page, pageSize);
        if ("report".equals(tab)) {
            return adminDAO.selectContentReviewReportList(param);
        }
        return adminDAO.selectContentReviewList(param);
    }

    @Override
    public int getContentReviewListCount(String tab, String keyword, String searchType) {
        if ("report".equals(tab)) {
            return adminDAO.selectContentReviewReportListCount(keywordSearchTypeParam(keyword, searchType));
        }
        return adminDAO.selectContentReviewListCount(keywordSearchTypeParam(keyword, searchType));
    }

    @Override
    public ReviewStatVO getContentReviewStats() {
        return adminDAO.selectContentReviewStats();
    }

    @Override
    public void deleteContentReview(Long reviewNo) {
        adminDAO.deleteContentReview(reviewNo);
    }

    /*
     * 신고 승인: 신고가 정당하다고 판단 -> 대기 중인 신고를 ACCEPTED로 바꾸고
     * 리뷰를 소프트 삭제한다. REVIEW는 하드 삭제가 아니라 STATUS만 바뀌므로
     * REVIEW_REPORT의 ACCEPTED 이력은 그대로 보존된다.
     */
    @Override
    @Transactional
    public void approveContentReviewReport(Long reviewNo) {

        List<Long> reporterMemberNos =
                adminDAO.selectWaitingContentReviewReporterMemberNos(reviewNo);

        int updated = adminDAO.updateContentReviewReportStatus(reviewNo, "ACCEPTED");

        if (updated == 0) {
            throw new IllegalStateException("처리 대기 중인 신고 내역이 없습니다.");
        }

        adminDAO.deleteContentReview(reviewNo);

        createReviewReportResultNotifications(
                reporterMemberNos,
                "CONTENT",
                true,
                reviewNo);
    }

    /*
     * 신고 반려: 신고가 부당하다고 판단 -> 대기 중인 신고를 REJECTED로 바꾸고
     * 리뷰는 그대로 둔다.
     */
    @Override
    @Transactional
    public void rejectContentReviewReport(Long reviewNo) {

        List<Long> reporterMemberNos =
                adminDAO.selectWaitingContentReviewReporterMemberNos(reviewNo);

        int updated = adminDAO.updateContentReviewReportStatus(reviewNo, "REJECTED");

        if (updated == 0) {
            throw new IllegalStateException("처리 대기 중인 신고 내역이 없습니다.");
        }

        createReviewReportResultNotifications(
                reporterMemberNos,
                "CONTENT",
                false,
                reviewNo);
    }

    /*
     * 목록 화면에서 체크박스로 선택한 콘텐츠 리뷰들을 한 번의 요청으로 일괄 처리한다.
     * bulkMemberAction과 동일하게, 이미 처리되어 대상이 아닌 건(예: 이미 처리된 신고)은
     * 건너뛰고 몇 건을 건너뛰었는지 반환한다. 각 처리는 기존 단건 메서드를 그대로
     * 재사용해 삭제/상태 변경 로직을 중복 작성하지 않는다.
     */
    @Override
    @Transactional
    public int bulkContentReviewAction(List<Long> reviewNos, String action) {

        if (reviewNos == null || reviewNos.isEmpty()) {
            return 0;
        }

        int skipped = 0;

        for (Long reviewNo : reviewNos) {

            try {
                switch (action) {
                    case "delete" -> deleteContentReview(reviewNo);
                    case "approve" -> approveContentReviewReport(reviewNo);
                    case "reject" -> rejectContentReviewReport(reviewNo);
                    default -> throw new IllegalArgumentException("알 수 없는 처리 유형입니다.");
                }
            } catch (IllegalStateException e) {
                // 이미 처리된 신고 등 대상이 아닌 건은 건너뛴다.
                skipped++;
            }
        }

        return skipped;
    }

    // ===================== 상품 리뷰 관리 =====================

    @Override
    public List<ReviewManageVO> getProductReviewList(String tab, String keyword, String searchType, int page,
            int pageSize) {
        Map<String, Object> param = withPaging(keywordSearchTypeParam(keyword, searchType), page, pageSize);
        if ("report".equals(tab)) {
            return adminDAO.selectProductReviewReportList(param);
        }
        return adminDAO.selectProductReviewList(param);
    }

    @Override
    public int getProductReviewListCount(String tab, String keyword, String searchType) {
        if ("report".equals(tab)) {
            return adminDAO.selectProductReviewReportListCount(keywordSearchTypeParam(keyword, searchType));
        }
        return adminDAO.selectProductReviewListCount(keywordSearchTypeParam(keyword, searchType));
    }

    @Override
    public ReviewStatVO getProductReviewStats() {
        return adminDAO.selectProductReviewStats();
    }

    @Override
    public void deleteProductReview(Long reviewNo) {
        // PRODUCT_REVIEW는 STATUS 컬럼이 없어 하드 삭제.
        // REVIEW_REPORT가 PRODUCT_REVIEW_NO를 참조하지만 FK_REPORT_PRODUCT_REVIEW가
        // ON DELETE CASCADE로 걸려 있어 신고 내역을 별도로 먼저 삭제할 필요가 없다.
        adminDAO.adminDeleteProductReview(reviewNo);
    }

    /*
     * 신고 승인: 신고가 정당하다고 판단 -> 대기 중인 신고를 ACCEPTED로 바꾼 뒤
     * 리뷰를 삭제한다. PRODUCT_REVIEW는 STATUS 컬럼이 없어 소프트 삭제가
     * 불가능하므로 하드 삭제하며, FK_REPORT_PRODUCT_REVIEW의 ON DELETE CASCADE로
     * 방금 ACCEPTED 처리한 REVIEW_REPORT 행도 리뷰와 함께 삭제된다.
     * (콘텐츠 리뷰와 달리 처리 이력이 남지 않는다 - PRODUCT_REVIEW에 소프트 삭제용
     * STATUS 컬럼이 없는 현재 테이블 구조상의 한계)
     */
    @Override
    @Transactional
    public void approveProductReviewReport(Long reviewNo) {

        List<Long> reporterMemberNos =
                adminDAO.selectWaitingProductReviewReporterMemberNos(reviewNo);

        int updated = adminDAO.updateProductReviewReportStatus(reviewNo, "ACCEPTED");

        if (updated == 0) {
            throw new IllegalStateException("처리 대기 중인 신고 내역이 없습니다.");
        }

        adminDAO.adminDeleteProductReview(reviewNo);

        createReviewReportResultNotifications(
                reporterMemberNos,
                "PRODUCT",
                true,
                reviewNo);
    }

    /*
     * 신고 반려: 신고가 부당하다고 판단 -> 대기 중인 신고를 REJECTED로 바꾸고
     * 리뷰는 그대로 둔다.
     */
    @Override
    @Transactional
    public void rejectProductReviewReport(Long reviewNo) {

        List<Long> reporterMemberNos =
                adminDAO.selectWaitingProductReviewReporterMemberNos(reviewNo);

        int updated = adminDAO.updateProductReviewReportStatus(reviewNo, "REJECTED");

        if (updated == 0) {
            throw new IllegalStateException("처리 대기 중인 신고 내역이 없습니다.");
        }

        createReviewReportResultNotifications(
                reporterMemberNos,
                "PRODUCT",
                false,
                reviewNo);
    }

    /*
     * 목록 화면에서 체크박스로 선택한 상품 리뷰들을 한 번의 요청으로 일괄 처리한다.
     * bulkContentReviewAction과 동일한 방식으로, 처리할 수 없는 건은 건너뛰고
     * 건너뛴 건수를 반환한다.
     */
    @Override
    @Transactional
    public int bulkProductReviewAction(List<Long> reviewNos, String action) {

        if (reviewNos == null || reviewNos.isEmpty()) {
            return 0;
        }

        int skipped = 0;

        for (Long reviewNo : reviewNos) {

            try {
                switch (action) {
                    case "delete" -> deleteProductReview(reviewNo);
                    case "approve" -> approveProductReviewReport(reviewNo);
                    case "reject" -> rejectProductReviewReport(reviewNo);
                    default -> throw new IllegalArgumentException("알 수 없는 처리 유형입니다.");
                }
            } catch (IllegalStateException e) {
                skipped++;
            }
        }

        return skipped;
    }

    /**
     * 동일 리뷰를 신고한 모든 회원에게 관리자 검토 결과를 알립니다.
     *
     * 상품 리뷰 승인 시 원본 리뷰와 신고 행이 함께 삭제되므로,
     * 호출 측에서 신고자 목록을 삭제 전에 먼저 조회해야 합니다.
     */
    private void createReviewReportResultNotifications(
            List<Long> reporterMemberNos,
            String reviewType,
            boolean accepted,
            Long reviewNo) {

        if (reporterMemberNos == null || reporterMemberNos.isEmpty()) {
            return;
        }

        boolean contentReview = "CONTENT".equals(reviewType);
        String targetLabel = contentReview ? "콘텐츠 리뷰" : "상품 리뷰";
        String notificationType = contentReview
                ? "CONTENT_REVIEW_REPORT_PROCESSED"
                : "PRODUCT_REVIEW_REPORT_PROCESSED";
        String message = accepted
                ? "신고하신 " + targetLabel
                        + "에서 운영 정책 위반이 확인되어 해당 리뷰를 삭제했습니다."
                : "신고하신 " + targetLabel
                        + "를 검토한 결과 운영 정책 위반 사항이 확인되지 않았습니다.";

        for (Long reporterMemberNo : reporterMemberNos) {
            notificationService.createForMember(
                    reporterMemberNo,
                    notificationType,
                    "리뷰 신고 검토 완료",
                    message,
                    null,
                    contentReview ? "CONTENT_REVIEW" : "PRODUCT_REVIEW",
                    reviewNo);
        }
    }

    // ===================== 이벤트 관리 =====================

    @Override
    public List<EventManageVO> getEventList(String tab, String keyword, String period, int page, int pageSize) {

        Map<String, Object> param = new HashMap<>();
        param.put("tab", tab);
        param.put("keyword", keyword);
        param.put("period", period);
        withPaging(param, page, pageSize);

        return adminDAO.selectAdminEventList(param);
    }

    @Override
    public int getEventListCount(String tab, String keyword, String period) {

        Map<String, Object> param = new HashMap<>();
        param.put("tab", tab);
        param.put("keyword", keyword);
        param.put("period", period);

        return adminDAO.selectAdminEventListCount(param);
    }


    @Override
    public EventStatVO getEventStats() {

        return adminDAO.selectEventStats();
    }

    @Override
    @Transactional
    public void approveEvent(Long eventNo) {

        validateEventNo(eventNo);

        int updateResult = adminDAO.updateEventStatus(
                eventNo,
                "APPROVED");

        if (updateResult != 1) {
            throw new IllegalStateException(
                    "이벤트 승인 처리에 실패했습니다.");
        }

        notificationService.createForEventOwner(
                eventNo,
                "EVENT_APPROVED",
                "이벤트 승인 완료",
                "요청한 이벤트가 승인되었습니다.",
                "/business/event/list",
                "EVENT",
                eventNo);

        /*
         * [리팩토링] PRODUCT.DISCOUNT_RATE를 승인 시점에 직접 덮어쓰던
         * 기존 로직을 제거했다.
         *
         * 기존 문제점: 이벤트를 승인하면 START_DATE(진행 예정)와 무관하게
         * 즉시 상품 할인가가 노출되고, END_DATE가 지나도 되돌아가지 않았다.
         *
         * 변경 후: goodsMapper.xml의 상품 조회 쿼리가 SYSDATE 기준으로
         * "지금 진행 중인 이벤트(STATUS='APPROVED' AND SYSDATE BETWEEN
         * START_DATE AND END_DATE)"를 매번 계산해서 할인율을 실시간으로
         * 반영한다. 따라서 이 메서드는 EVENT.STATUS만 APPROVED로 바꾸면 되고,
         * 실제 할인 노출/종료는 별도 배치 없이 날짜에 따라 자동으로 처리된다.
         */
    }

    @Override
    @Transactional
    public void rejectEvent(Long eventNo) {

        validateEventNo(eventNo);

        int updateResult = adminDAO.updateEventStatus(
                eventNo,
                "REJECTED");

        if (updateResult != 1) {
            throw new IllegalStateException(
                    "이벤트 반려 처리에 실패했습니다.");
        }

        notificationService.createForEventOwner(
                eventNo,
                "EVENT_REJECTED",
                "이벤트 승인 반려",
                "요청한 이벤트가 반려되었습니다. 이벤트 목록을 확인해주세요.",
                "/business/event/list",
                "EVENT",
                eventNo);
    }

    // ===================== 상품 관리 =====================

    @Override
    public List<ProductManageVO> getProductRequestList(String tab, String keyword, String searchType, int page,
            int pageSize) {
        Map<String, Object> param = withPaging(keywordSearchTypeParam(keyword, searchType), page, pageSize);
        param.put("tab", tab);
        return adminDAO.selectProductRequestList(param);
    }

    @Override
    public int getProductRequestListCount(String tab, String keyword, String searchType) {
        Map<String, Object> param = keywordSearchTypeParam(keyword, searchType);
        param.put("tab", tab);
        return adminDAO.selectProductRequestListCount(param);
    }

    @Override
    public ProductStatVO getProductStats() {
        return adminDAO.selectProductStats();
    }

    @Override
    @Transactional
    public void approveProduct(Long productNo) {

        validateProductNo(productNo);

        String status = adminDAO.selectProductStatusByNo(productNo);

        if (status == null) {
            throw new IllegalArgumentException("존재하지 않는 상품입니다.");
        }

        /*
         * 사업자가 삭제 요청한 상품이면 단순 상태 변경이 아니라
         * PRODUCT를 참조하는 자식 데이터부터 삭제한 후 상품을 최종 삭제한다.
         */
        if ("DELETE_REQUESTED".equals(status)) {

            /*
             * ORDER_ITEM은 주문·결제·정산 이력을 보존해야 하므로
             * 주문 이력이 있는 상품은 DB에서 완전히 삭제하지 않는다.
             */
            if (adminDAO.countOrderItemByProductNo(productNo) > 0) {
                throw new IllegalStateException(
                        "주문 이력이 있는 상품은 완전삭제할 수 없습니다. 판매 중지 방식으로 처리해주세요.");
            }

            /*
             * PRODUCT_IMAGE 행을 삭제하기 전에 실제 이미지 파일 경로를 조회한다.
             * DB 트랜잭션이 커밋된 뒤 서버 파일을 삭제한다.
             */
            List<String> imagePathList = adminDAO.selectProductImagePathList(productNo);

            /* 상품 행 삭제 전에 소유 사업자를 조회하여 결과 알림을 저장합니다. */
            notificationService.createForProductOwner(
                    productNo,
                    "PRODUCT_DELETE_APPROVED",
                    "상품 삭제 완료",
                    "요청한 상품 삭제가 승인되어 상품이 삭제되었습니다.",
                    "/business/product/list",
                    "PRODUCT",
                    productNo);

            // FK 제약조건(ORA-02292) 위반을 막기 위해 자식 테이블부터 삭제한다.
            // REVIEW_REPORT는 FK_REPORT_PRODUCT_REVIEW의 ON DELETE CASCADE로
            // 아래 상품 리뷰 삭제 시 자동으로 함께 정리되므로 별도 단계가 필요 없다.
            adminDAO.deleteProductReviewByProductNo(productNo);
            adminDAO.deleteCartItemByProductNo(productNo);
            adminDAO.deleteProductWishByProductNo(productNo);
            adminDAO.deleteProductClickLogByProductNo(productNo);
            adminDAO.deleteEventProductByProductNo(productNo);
            adminDAO.deleteProductImageByProductNo(productNo);

            int deleteResult = adminDAO.deleteProduct(productNo);

            if (deleteResult != 1) {
                throw new IllegalStateException("상품 최종 삭제에 실패했습니다.");
            }

            registerImageFileDeleteAfterCommit(imagePathList);
            return;
        }

        int updateResult = adminDAO.updateProductStatus(productNo, "APPROVED");

        if (updateResult != 1) {
            throw new IllegalStateException("상품 승인 처리에 실패했습니다.");
        }

        notificationService.createForProductOwner(
                productNo,
                "PRODUCT_APPROVED",
                "상품 승인 완료",
                "등록 또는 수정한 상품이 승인되었습니다.",
                "/business/product/list",
                "PRODUCT",
                productNo);
    }

    @Override
    @Transactional
    public void rejectProduct(Long productNo) {

        validateProductNo(productNo);

        String status = adminDAO.selectProductStatusByNo(productNo);

        if (status == null) {
            throw new IllegalArgumentException("존재하지 않는 상품입니다.");
        }

        /*
         * 삭제 요청 반려 시 상품 자체를 삭제하지 않고
         * 삭제 요청 전 승인 상태로 되돌린다.
         */
        String nextStatus = "DELETE_REQUESTED".equals(status)
                ? "APPROVED"
                : "REJECTED";

        int updateResult = adminDAO.updateProductStatus(productNo, nextStatus);

        if (updateResult != 1) {
            throw new IllegalStateException("상품 반려 처리에 실패했습니다.");
        }

        boolean deleteRequest = "DELETE_REQUESTED".equals(status);

        notificationService.createForProductOwner(
                productNo,
                deleteRequest
                        ? "PRODUCT_DELETE_REJECTED"
                        : "PRODUCT_REJECTED",
                deleteRequest
                        ? "상품 삭제 요청 반려"
                        : "상품 승인 반려",
                deleteRequest
                        ? "상품 삭제 요청이 반려되어 기존 승인 상태로 복구되었습니다."
                        : "상품 승인 요청이 반려되었습니다. 상품 목록을 확인해주세요.",
                "/business/product/list",
                "PRODUCT",
                productNo);
    }

    // ===================== 주문 조회 (조회 전용) =====================
    // 배송 상태 변경, 주문 취소 등 실제 처리는 사업자(Business)가 담당하며,
    // 관리자는 분쟁 확인 등을 위해 상세 내역만 조회한다.

    @Override
    public List<OrderManageVO> getOrderList(String keyword, int page, int pageSize) {
        return adminDAO.selectOrderList(withPaging(keywordParam(keyword), page, pageSize));
    }

    @Override
    public int getOrderListCount(String keyword) {
        return adminDAO.selectOrderListCount(keywordParam(keyword));
    }

    @Override
    public OrderStatVO getOrderStats() {
        return adminDAO.selectOrderStats();
    }

    // ===================== 환불 조회 (조회 전용) =====================
    // 환불 승인/거절은 사업자가 처리하며, 관리자는 사유/처리 결과만 조회한다.

    @Override
    public List<OrderManageVO> getRefundList(String keyword, String status, int page, int pageSize) {
        Map<String, Object> param = keywordParam(keyword);
        param.put("status", status);
        withPaging(param, page, pageSize);
        return adminDAO.selectRefundList(param);
    }

    @Override
    public int getRefundListCount(String keyword, String status) {
        Map<String, Object> param = keywordParam(keyword);
        param.put("status", status);
        return adminDAO.selectRefundListCount(param);
    }

    // ===================== 사업자 관리 =====================

    @Override
    public List<BusinessManageVO> getBusinessList(String keyword, String searchType, int page, int pageSize) {
        return adminDAO.selectBusinessList(withPaging(keywordSearchTypeParam(keyword, searchType), page, pageSize));
    }

    @Override
    public int getBusinessListCount(String keyword, String searchType) {
        return adminDAO.selectBusinessListCount(keywordSearchTypeParam(keyword, searchType));
    }

    @Override
    public List<BusinessManageVO> getBusinessApprovalList(String keyword, String searchType, int page,
            int pageSize) {
        return adminDAO.selectBusinessApprovalList(
                withPaging(keywordSearchTypeParam(keyword, searchType), page, pageSize));
    }

    @Override
    public int getBusinessApprovalListCount(String keyword, String searchType) {
        return adminDAO.selectBusinessApprovalListCount(keywordSearchTypeParam(keyword, searchType));
    }

    @Override
    public BusinessStatVO getBusinessStats() {
        return adminDAO.selectBusinessStats();
    }

    @Override
    public void updateBusinessGrade(Long businessNo, String gradeName) {
        adminDAO.updateBusinessGrade(businessNo, gradeName);
    }

    @Override
    @Transactional
    public void approveBusiness(Long businessNo) {

        if (adminDAO.updateBusinessStatus(businessNo, "APPROVED") != 1) {
            throw new IllegalStateException("사업자 승인 처리에 실패했습니다.");
        }

        notificationService.createForBusiness(
                businessNo,
                "BUSINESS_APPROVED",
                "사업자 승인 완료",
                "사업자 가입 신청이 승인되었습니다.",
                "/member/mypage",
                "BUSINESS",
                businessNo);
    }

    @Override
    @Transactional
    public void rejectBusiness(Long businessNo) {

        if (adminDAO.updateBusinessStatus(businessNo, "REJECTED") != 1) {
            throw new IllegalStateException("사업자 반려 처리에 실패했습니다.");
        }

        notificationService.createForBusiness(
                businessNo,
                "BUSINESS_REJECTED",
                "사업자 승인 반려",
                "사업자 가입 신청이 반려되었습니다.",
                "/member/mypage",
                "BUSINESS",
                businessNo);
    }

    // ===================== 정산 관리 =====================

    @Override
    public List<SettlementManageVO> getSettlementList(String keyword, String status, String period, int page,
            int pageSize) {
        Map<String, Object> param = keywordParam(keyword);
        param.put("status", status);
        param.put("period", period);
        withPaging(param, page, pageSize);
        return adminDAO.selectSettlementList(param);
    }

    @Override
    public int getSettlementListCount(String keyword, String status, String period) {
        Map<String, Object> param = keywordParam(keyword);
        param.put("status", status);
        param.put("period", period);
        return adminDAO.selectSettlementListCount(param);
    }

    @Override
    public SettlementStatVO getSettlementStats() {
        return adminDAO.selectSettlementStats();
    }

    /* [수정] 월별 입금 확인 요청 건 전체를 완료 처리한다. */
    @Override
    @Transactional
    public void confirmSettlement(Long businessNo, String settlementMonth) {
        if (businessNo == null || businessNo <= 0 || settlementMonth == null || settlementMonth.isBlank()) {
            throw new IllegalArgumentException("올바르지 않은 정산 요청입니다.");
        }

        if (adminDAO.updateSettlementStatus(businessNo, settlementMonth, "DONE") <= 0) {
            throw new IllegalStateException("정산 완료 처리할 내역이 없습니다.");
        }

        notificationService.createForBusiness(
                businessNo,
                "SETTLEMENT_APPROVED",
                "정산 확인 완료",
                settlementMonth + " 정산 입금 확인이 완료되었습니다.",
                "/business/settlement/complete",
                "BUSINESS",
                businessNo);
    }

    /* [수정] 월별 입금 확인 요청 건 전체를 반려 처리한다. */
    @Override
    @Transactional
    public void rejectSettlement(Long businessNo, String settlementMonth) {
        if (businessNo == null || businessNo <= 0 || settlementMonth == null || settlementMonth.isBlank()) {
            throw new IllegalArgumentException("올바르지 않은 정산 요청입니다.");
        }

        if (adminDAO.updateSettlementStatus(businessNo, settlementMonth, "REJECTED") <= 0) {
            throw new IllegalStateException("정산 반려 처리할 내역이 없습니다.");
        }

        notificationService.createForBusiness(
                businessNo,
                "SETTLEMENT_REJECTED",
                "정산 확인 반려",
                settlementMonth + " 정산 입금 확인 요청이 반려되었습니다.",
                "/business/settlement/main",
                "BUSINESS",
                businessNo);
    }

    // ===================== 모니터링 =====================

    @Override
    public List<MonitoringVO> getMonitoringList() {
        return adminDAO.selectMonitoringList();
    }

    @Override
    public List<VisitorTrendVO> getVisitorTrend() {
        return adminDAO.selectVisitorTrend();
    }

    @Override
    public List<PopularClickVO> getPopularProductClicks() {
        return adminDAO.selectPopularProductClicks();
    }

    // ===================== 콘텐츠 관리 =====================

    @Override
    public List<ContentManageVO> getContentList(String keyword) {
        return adminDAO.selectAdminContentList(keyword);
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

    /*
     * =========================================================
     * 이벤트 번호 검증
     * =========================================================
     */
    private void validateEventNo(Long eventNo) {

        if (eventNo == null || eventNo <= 0) {
            throw new IllegalArgumentException("올바르지 않은 이벤트 번호입니다.");
        }
    }

    /*
     * =========================================================
     * 상품 번호 검증
     * =========================================================
     */
    private void validateProductNo(Long productNo) {

        if (productNo == null || productNo <= 0) {
            throw new IllegalArgumentException("올바르지 않은 상품 번호입니다.");
        }
    }

    /*
     * =========================================================
     * DB 트랜잭션 커밋 후 실제 상품 이미지 파일 삭제 등록
     * =========================================================
     */
    private void registerImageFileDeleteAfterCommit(List<String> imagePathList) {

        if (imagePathList == null || imagePathList.isEmpty()) {
            return;
        }

        List<String> copiedImagePathList = List.copyOf(imagePathList);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {

                        @Override
                        public void afterCommit() {
                            deletePhysicalImageFiles(copiedImagePathList);
                        }
                    });

            return;
        }

        deletePhysicalImageFiles(copiedImagePathList);
    }

    /*
     * =========================================================
     * 서버에 저장된 실제 상품 이미지 파일 삭제
     * =========================================================
     */
    private void deletePhysicalImageFiles(List<String> imagePathList) {

        for (String imagePath : imagePathList) {

            if (imagePath == null || imagePath.isBlank()) {
                continue;
            }

            try {
                String normalizedPath = imagePath.replace("\\", "/");
                int lastSlashIndex = normalizedPath.lastIndexOf('/');

                String filename = lastSlashIndex >= 0
                        ? normalizedPath.substring(lastSlashIndex + 1)
                        : normalizedPath;

                if (filename.isBlank()) {
                    continue;
                }

                Path targetPath = productUploadDirectory
                        .resolve(filename)
                        .normalize();

                // 상위 경로 이동 공격을 방지한다.
                if (!targetPath.startsWith(productUploadDirectory)) {
                    System.err.println("허용되지 않은 상품 이미지 경로: " + targetPath);
                    continue;
                }

                Files.deleteIfExists(targetPath);

            } catch (IOException e) {
                /*
                 * DB 삭제는 이미 정상 커밋되었으므로
                 * 파일 삭제 실패는 로그로 남기고 전체 DB 처리를 되돌리지 않는다.
                 */
                System.err.println("상품 이미지 실제 파일 삭제 실패: " + imagePath);
                e.printStackTrace();
            }
        }
    }
}