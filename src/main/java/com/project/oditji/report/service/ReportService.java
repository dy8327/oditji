package com.project.oditji.report.service;

import java.util.Set;

public interface ReportService {

    /**
     * 콘텐츠 리뷰 또는 상품 리뷰 신고 등록.
     *
     * @param memberNo         신고자 회원번호 (로그인 필수)
     * @param reviewType       "CONTENT" 또는 "PRODUCT"
     * @param contentReviewNo  reviewType == CONTENT 일 때 필수
     * @param productReviewNo  reviewType == PRODUCT 일 때 필수
     * @param reason           신고 사유
     * @param detail           상세 내용 (선택)
     *
     * @throws IllegalArgumentException 입력값이 올바르지 않을 때
     * @throws IllegalStateException    이미 신고한 리뷰일 때
     */
    void submitReport(
            Long memberNo,
            String reviewType,
            Integer contentReviewNo,
            Integer productReviewNo,
            String reason,
            String detail
    );

    /**
     * 로그인 회원이 신고한 상품 리뷰번호 목록.
     * 콘텐츠 리뷰 쪽은 ReviewService.getReportedReviewSet() 이 기존에 담당한다.
     */
    Set<Integer> getReportedProductReviewSet(Long memberNo);
}
