package com.project.oditji.content.vo;

import com.project.oditji.common.vo.ContentMetadataVO;

/**
 * 배우·감독의 필모그래피 카드 정보입니다.
 * 콘텐츠 공통 메타데이터는 ContentMetadataVO에서 상속합니다.
 */
public class FilmographyVO extends ContentMetadataVO {

    private String releaseDate;
    private String participationName;
    private String participationCategory;
    private Double popularity;

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getParticipationName() {
        return participationName;
    }

    public void setParticipationName(String participationName) {
        this.participationName = participationName;
    }

    public String getParticipationCategory() {
        return participationCategory;
    }

    public void setParticipationCategory(String participationCategory) {
        this.participationCategory = participationCategory;
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
    }
}
