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
import com.project.oditji.admin.vo.ProductManageVO;
import com.project.oditji.admin.vo.ReviewManageVO;
import com.project.oditji.admin.vo.SettlementManageVO;

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
    @Transactional
    public void approveEvent(Long requestNo) {

        validateEventNo(requestNo);

        int updateResult = adminDAO.updateEventStatus(
                requestNo,
                "APPROVED");

        if (updateResult != 1) {
            throw new IllegalStateException(
                    "승인 대기 중인 이벤트가 아니거나 이벤트 승인 처리에 실패했습니다.");
        }
    }

    @Override
    @Transactional
    public void rejectEvent(Long requestNo) {

        validateEventNo(requestNo);

        int updateResult = adminDAO.updateEventStatus(
                requestNo,
                "REJECTED");

        if (updateResult != 1) {
            throw new IllegalStateException(
                    "승인 대기 중인 이벤트가 아니거나 이벤트 반려 처리에 실패했습니다.");
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
            adminDAO.deleteReviewReportByProductNo(productNo);
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
