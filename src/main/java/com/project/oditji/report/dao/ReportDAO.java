package com.project.oditji.report.dao;

import java.util.List;
import java.util.Map;

import com.project.oditji.report.vo.ReportVO;

public interface ReportDAO {

    int insertReport(ReportVO report);

    /**
     * 회원이 특정 콘텐츠 리뷰 / 상품 리뷰를 이미 신고했는지 카운트.
     * param 에는 memberNo, reviewType, contentReviewNo, productReviewNo 가 담긴다.
     */
    int countReport(Map<String, Object> param);

    List<Integer> selectReportedProductReviewNoList(Long memberNo);
}
