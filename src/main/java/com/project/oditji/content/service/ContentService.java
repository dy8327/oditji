package com.project.oditji.content.service;

import java.util.List;

import com.project.oditji.content.vo.ContentListPageVO;
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.content.vo.PersonFilmographyVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

public interface ContentService {

    int prepareContentDetail(Long tmdbId, String contentType);

    ContentVO getContentDetail(int contentNo);

    List<ActorVO> getActorListByContentNo(int contentNo);

    List<DirectorVO> getDirectorListByContentNo(int contentNo);

    List<OttPlatformVO> getOttPlatformListByContentNo(int contentNo);

    /**
     * 콘텐츠 상세 페이지의 관련 콘텐츠를
     * JSONL 공용 캐시에서 조회합니다.
     */
    List<SearchResultVO> getRelatedContentList(int contentNo);

    PersonFilmographyVO getPersonFilmography(
            Long tmdbPersonId,
            String role);

    List<ContentVO> getMainContentList();

    /**
     * JSONL 공용 콘텐츠 저장소를 기준으로
     * 영화·시리즈, 인기, 신규 목록을 조회합니다.
     */
    ContentListPageVO getContentListByType(
            String type,
            int page,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds);

    /**
     * 현재 목록의 카테고리, 장르, OTT 조건을 반영하여
     * 우측 추천 콘텐츠를 JSONL에서 조회합니다.
     */
    List<SearchResultVO> getContentRecommendedList(
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds);
}