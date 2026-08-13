package com.project.oditji.business.vo;

import java.time.LocalDate;

import com.project.oditji.common.vo.ContentMetadataVO;

/**
 * 사업자 상품 등록 화면의 콘텐츠 검색 결과입니다.
 */
public class ContentSearchVO extends ContentMetadataVO {

    private Long contentNo;
    private LocalDate releaseDate;

    public Long getContentNo() {
        return contentNo;
    }

    public void setContentNo(Long contentNo) {
        this.contentNo = contentNo;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    @Override
    public String toString() {
        return "ContentSearchVO{" +
                "contentNo=" + contentNo +
                ", tmdbId=" + getTmdbId() +
                ", contentType='" + getContentType() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", originalTitle='" + getOriginalTitle() + '\'' +
                ", posterPath='" + getPosterPath() + '\'' +
                ", releaseDate=" + releaseDate +
                ", genreText='" + getGenreText() + '\'' +
                ", ageRating='" + getAgeRating() + '\'' +
                '}';
    }
}
