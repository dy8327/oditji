package com.project.oditji.review.vo;

/**
 * 마이페이지의 콘텐츠 리뷰와 상품 리뷰를 하나의 목록으로 표시하는 통합 VO입니다.
 */
public class MyReviewVO extends ProductReviewBaseVO {

    private String reviewType;
    private int targetNo;
    private String title;
    private String thumbnail;

    public String getReviewType() {
        return reviewType;
    }

    public void setReviewType(String reviewType) {
        this.reviewType = reviewType;
    }

    public int getTargetNo() {
        return targetNo;
    }

    public void setTargetNo(int targetNo) {
        this.targetNo = targetNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
