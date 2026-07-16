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

    List<ContentVO> getRelatedContentList(int contentNo);

    PersonFilmographyVO getPersonFilmography(
            Long tmdbPersonId,
            String role);

    List<ContentVO> getMainContentList();

    ContentListPageVO getContentListByType(
            String type,
            int page,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds);

    List<SearchResultVO> getContentRecommendedList(
            List<String> providerIds);
}