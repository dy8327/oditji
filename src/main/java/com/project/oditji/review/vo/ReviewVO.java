package com.project.oditji.review.vo;

import java.util.Date;

/**
 * REVIEW 테이블(콘텐츠 리뷰) 원본 매핑 VO입니다.
 * 리뷰 작성과 수정에 사용합니다.
 */
public class ReviewVO extends ReviewBaseVO {

    private Long reviewNo;
    private int contentNo;
    private Date updatedAt;

    public Long getReviewNo() {
        return reviewNo;
    }

    public void setReviewNo(Long reviewNo) {
        this.reviewNo = reviewNo;
    }

    public int getContentNo() {
        return contentNo;
    }

    public void setContentNo(int contentNo) {
        this.contentNo = contentNo;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
