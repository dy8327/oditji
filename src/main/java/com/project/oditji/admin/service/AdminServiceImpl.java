package com.project.oditji.admin.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.project.oditji.admin.dao.AdminDAO;
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

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminDAO adminDAO;

    /*
     * 상품 삭제 승인 후 실제 업로드 파일까지 정리하기 위한 경로이다.
     * 사업자 상품 등록 서비스에서 사용하는 설정값과 동일한 값을 사용한다.
     */
    private final Path productUploadDirectory;

    public AdminServiceImpl(
            AdminDAO adminDAO,
            @Value("${oditji.upload.product-path:uploads/product}") String productUploadPath) {

        this.adminDAO = adminDAO;
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

    @Override
    public List<MemberManageVO> getMemberList(String keyword) {
        return adminDAO.selectMemberList(keyword);
    }

    @Override
    public void suspendMember(Long memberNo) {
        adminDAO.updateMemberStatus(memberNo, "BLOCKED");
    }

    @Override
    public void restoreMember(Long memberNo) {
        adminDAO.updateMemberStatus(memberNo, "ACTIVE");
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

    /*
     * 신고 승인: 신고가 정당하다고 판단 -> 대기 중인 신고를 ACCEPTED로 바꾸고
     * 리뷰를 소프트 삭제한다. REVIEW는 하드 삭제가 아니라 STATUS만 바뀌므로
     * REVIEW_REPORT의 ACCEPTED 이력은 그대로 보존된다.
     */
    @Override
    @Transactional
    public void approveContentReviewReport(Long reviewNo) {

        int updated = adminDAO.updateContentReviewReportStatus(reviewNo, "ACCEPTED");

        if (updated == 0) {
            throw new IllegalStateException("처리 대기 중인 신고 내역이 없습니다.");
        }

        adminDAO.deleteContentReview(reviewNo);
    }

    /*
     * 신고 반려: 신고가 부당하다고 판단 -> 대기 중인 신고를 REJECTED로 바꾸고
     * 리뷰는 그대로 둔다.
     */
    @Override
    public void rejectContentReviewReport(Long reviewNo) {

        int updated = adminDAO.updateContentReviewReportStatus(reviewNo, "REJECTED");

        if (updated == 0) {
            throw new IllegalStateException("처리 대기 중인 신고 내역이 없습니다.");
        }
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

        int updated = adminDAO.updateProductReviewReportStatus(reviewNo, "ACCEPTED");

        if (updated == 0) {
            throw new IllegalStateException("처리 대기 중인 신고 내역이 없습니다.");
        }

        adminDAO.adminDeleteProductReview(reviewNo);
    }

    /*
     * 신고 반려: 신고가 부당하다고 판단 -> 대기 중인 신고를 REJECTED로 바꾸고
     * 리뷰는 그대로 둔다.
     */
    @Override
    public void rejectProductReviewReport(Long reviewNo) {

        int updated = adminDAO.updateProductReviewReportStatus(reviewNo, "REJECTED");

        if (updated == 0) {
            throw new IllegalStateException("처리 대기 중인 신고 내역이 없습니다.");
        }
    }

    // ===================== 이벤트 관리 =====================

    @Override
    public List<EventManageVO> getEventList(String tab, String keyword) {

        return adminDAO.selectAdminEventList(tab, keyword);
    }


    @Override
    @Transactional
    public void approveEvent(Long eventNo) {

        validateEventNo(eventNo);

        int updateResult = adminDAO.updateEventStatus(
                eventNo,
                "APPROVED"
        );

        if (updateResult != 1) {
            throw new IllegalStateException(
                    "이벤트 승인 처리에 실패했습니다."
            );
        }

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
                "REJECTED"
        );

        if (updateResult != 1) {
            throw new IllegalStateException(
                    "이벤트 반려 처리에 실패했습니다."
            );
        }
    }

    // ===================== 상품 관리 =====================

    @Override
    public List<ProductManageVO> getProductRequestList(String tab, String keyword) {
        /*
         * PRODUCT 테이블에는 등록/수정 요청을 구분하는 별도 컬럼이 없으므로
         * register/update 탭은 WAITING 상태를 동일하게 조회한다.
         * delete 탭은 DELETE_REQUESTED 상태만 조회한다.
         */
        return adminDAO.selectProductRequestList(tab, keyword);
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
