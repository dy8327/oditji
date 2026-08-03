package com.project.oditji.tmdb.vo;

import java.time.LocalDate;

import com.project.oditji.common.vo.ContentMetadataVO;

/**
 * TMDB API에서 조회한 콘텐츠 정보를 보관합니다.
 */
public class TmdbVO extends ContentMetadataVO {

    private LocalDate releaseDate;

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }
}
