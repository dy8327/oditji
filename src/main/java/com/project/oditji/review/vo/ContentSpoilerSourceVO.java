package com.project.oditji.review.vo;

/**
 * [추가] 콘텐츠 리뷰 스포일러 자동 판별용 VO.
 * 제목, 배우, 감독, 장르는 제외하고 OVERVIEW만 조회한다.
 */
public class ContentSpoilerSourceVO {
    private int contentNo;
    private String overview;

    public int getContentNo() {
        return contentNo;
    }

    public void setContentNo(int contentNo) {
        this.contentNo = contentNo;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }
}