package com.project.oditji.tmdb.service;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.content.vo.PersonFilmographyVO;

public interface TmdbService {

    int loadMovieData();

    int updateMovieDetailData();

    int loadMoviePlatformData();

    int loadMovieFullData();

    int loadTvData();

    int updateTvDetailData();

    int loadTvPlatformData();

    int loadTvFullData();

    int loadAllData();

    ContentVO getDetailForSave(
            Long tmdbId,
            String contentType);

    void saveContentPlatform(
            ContentVO content);

    void saveContentPeople(
            ContentVO content);

    PersonFilmographyVO getPersonFilmography(
            Long tmdbPersonId,
            String role);
}
