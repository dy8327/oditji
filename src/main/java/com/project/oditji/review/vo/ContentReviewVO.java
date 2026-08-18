package com.project.oditji.review.vo;

/**
 * 콘텐츠 상세 페이지 리뷰 목록 표시용 VO입니다.
 * 작성자 닉네임과 화면용 리뷰 번호를 추가로 보관합니다.
 */
public class ContentReviewVO extends ReviewBaseVO {

    private int reviewNo;
    private String writer;
    private String profileImage;

    public int getReviewNo() {
        return reviewNo;
    }

    public void setReviewNo(int reviewNo) {
        this.reviewNo = reviewNo;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
