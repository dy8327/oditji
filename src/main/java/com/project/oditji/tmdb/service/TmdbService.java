package com.project.oditji.tmdb.service;

import java.util.List;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.content.vo.PersonFilmographyVO;
import com.project.oditji.search.vo.SearchResultVO;

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

    List<SearchResultVO> searchMulti(String keyword, int page);

    List<SearchResultVO> searchMulti(
            String keyword,
            int page,
            List<String> platformList,
            List<String> categoryList,
            List<String> genreList);

    List<SearchResultVO> getPopularKrOttContent(
            int page,
            List<String> platformList,
            List<String> categoryList,
            List<String> genreList);

    List<SearchResultVO> getMainPopularContent();

    List<SearchResultVO> getMainTodayContent();

    List<SearchResultVO> getMainRecommendedContent();

    List<SearchResultVO> getMainRecommendedContent(
            List<String> platformList);

    ContentVO getDetailForSave(Long tmdbId, String contentType);

    void saveContentPlatform(ContentVO content);

    void saveContentPeople(ContentVO content);

    PersonFilmographyVO getPersonFilmography(Long tmdbPersonId, String role);
}
