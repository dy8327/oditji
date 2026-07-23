package com.project.oditji.recommend.vo;

/**
 * 추천 OTT 카드 아래에 표시할 콘텐츠의 최소 정보를 담는 VO입니다.
 *
 * 화면에서는 콘텐츠 제목과 상세페이지 이동에 필요한 콘텐츠 번호만 사용합니다.
 */
public class RecommendOttContentVO {

    private Integer contentNo;
    private String title;

    public Integer getContentNo() {
        return contentNo;
    }

    public void setContentNo(
            Integer contentNo) {

        this.contentNo = contentNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(
            String title) {

        this.title = title;
    }
}
