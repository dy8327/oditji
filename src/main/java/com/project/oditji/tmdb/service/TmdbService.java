package com.project.oditji.tmdb.service;

import java.util.List;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.content.vo.PersonFilmographyVO;
import com.project.oditji.tmdb.vo.ActorVO;

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

    /**
     * 기존 초기 적재 로직과 호환되는 OTT 저장 메서드입니다.
     * 플랫폼 키가 별도로 없으면 TMDB watch/providers API를 사용합니다.
     */
    void saveContentPlatform(
            ContentVO content);

    /**
     * 검색 JSON 캐시의 플랫폼 키를 우선 사용하여
     * CONTENT_PLATFORM 관계를 저장합니다.
     *
     * 플랫폼 키가 비어 있을 때만 기존 TMDB API 조회 방식으로
     * 대체하여 기존 기능과의 호환성을 유지합니다.
     */
    void saveContentPlatform(
            ContentVO content,
            List<String> platformKeys);

    void saveContentPeople(
            ContentVO content);

    /**
     * 상품 등록 화면에서 DB 저장 전에 표시할 TMDB 출연 배우 목록을 조회합니다.
     */
    List<ActorVO> getContentActorPreview(
            Long tmdbId,
            String contentType);

    PersonFilmographyVO getPersonFilmography(
            Long tmdbPersonId,
            String role);
}
