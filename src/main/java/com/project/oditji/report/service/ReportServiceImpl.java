package com.project.oditji.report.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.report.dao.ReportDAO;
import com.project.oditji.report.vo.ReportVO;

@Service
public class ReportServiceImpl implements ReportService {

    private static final String CONTENT_REVIEW_TYPE = "CONTENT";

    private final ReportDAO reportDAO;
    private final NotificationService notificationService;

    public ReportServiceImpl(
            ReportDAO reportDAO,
            NotificationService notificationService) {

        this.reportDAO = reportDAO;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void submitReport(Long memberNo, String reviewType, Integer contentReviewNo, Integer productReviewNo, String reason, String detail) {

        if (memberNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        String normalizedType = reviewType == null ? "" : reviewType.trim().toUpperCase(Locale.ROOT);

        if (!CONTENT_REVIEW_TYPE.equals(normalizedType) && !"PRODUCT".equals(normalizedType)) {

            throw new IllegalArgumentException("잘못된 신고 대상입니다.");
        }

        if (CONTENT_REVIEW_TYPE.equals(normalizedType) && (contentReviewNo == null || contentReviewNo <= 0)) {

            throw new IllegalArgumentException("신고할 리뷰 정보가 없습니다.");
        }

        if ("PRODUCT".equals(normalizedType) && (productReviewNo == null || productReviewNo <= 0)) {

            throw new IllegalArgumentException("신고할 리뷰 정보가 없습니다.");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("신고 사유를 선택해주세요.");
        }

        Map<String, Object> checkParam = new HashMap<>();
        checkParam.put("memberNo", memberNo);
        checkParam.put("reviewType", normalizedType);
        checkParam.put("contentReviewNo", contentReviewNo);
        checkParam.put("productReviewNo", productReviewNo);

        int existingCount = reportDAO.countReport(checkParam);

        if (existingCount > 0) {
            // UQ_CONTENT_REVIEW_REPORT / UQ_PRODUCT_REVIEW_REPORT 제약조건과
            // 동일한 조건을 애플리케이션 단에서 먼저 체크하여
            // 사용자에게 명확한 메시지를 준다.
            throw new IllegalStateException("이미 신고한 리뷰입니다.");
        }

        ReportVO report = new ReportVO();
        report.setMemberNo(memberNo);
        report.setReviewType(normalizedType);
        report.setContentReviewNo(contentReviewNo);
        report.setProductReviewNo(productReviewNo);
        report.setReason(reason.trim());
        report.setDetail(detail == null || detail.trim().isEmpty() ? null : detail.trim());

        reportDAO.insertReport(report);

        createReportReceivedNotification(
                memberNo,
                normalizedType,
                contentReviewNo,
                productReviewNo);
    }

    /**
     * 신고 접수가 정상적으로 저장된 경우 신고자 본인에게 접수 알림을 생성합니다.
     * 신고 대상 리뷰 화면은 처리 과정에서 삭제될 수 있으므로 별도 이동 링크는 두지 않습니다.
     */
    private void createReportReceivedNotification(
            Long memberNo,
            String reviewType,
            Integer contentReviewNo,
            Integer productReviewNo) {

        boolean contentReport = CONTENT_REVIEW_TYPE.equals(reviewType);
        Long reviewNo = contentReport
                ? Long.valueOf(contentReviewNo)
                : Long.valueOf(productReviewNo);

        notificationService.createForMember(
                memberNo,
                contentReport
                        ? "CONTENT_REVIEW_REPORT_RECEIVED"
                        : "PRODUCT_REVIEW_REPORT_RECEIVED",
                "리뷰 신고 접수",
                contentReport
                        ? "콘텐츠 리뷰 신고가 접수되었습니다. 검토 후 결과를 안내해 드리겠습니다."
                        : "상품 리뷰 신고가 접수되었습니다. 검토 후 결과를 안내해 드리겠습니다.",
                null,
                contentReport ? "CONTENT_REVIEW" : "PRODUCT_REVIEW",
                reviewNo);
    }

    @Override
    public Set<Integer> getReportedProductReviewSet(Long memberNo) {

        if (memberNo == null) {
            return Collections.emptySet();
        }

        List<Integer> reportedList = reportDAO.selectReportedProductReviewNoList(memberNo);

        return reportedList == null ? Collections.emptySet() : new HashSet<>(reportedList);
    }
}
