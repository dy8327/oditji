package com.project.oditji.content.dao;

import java.util.List;
import java.util.Map;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

public interface ContentDAO {

    ContentVO selectContentByTmdbId(
            Long tmdbId,
            String contentType);

    ContentVO selectContentByContentNo(
            int contentNo);

    int insertContent(
            ContentVO content);

    /**
     * 검색 JSON 캐시에서 가져온 값을 기존 CONTENT 행에 반영합니다.
     */
    int updateContentFromSearchCache(
            ContentVO content);

    int increaseViewCount(
            int contentNo);

    List<ActorVO> selectActorListByContentNo(
            int contentNo);

    List<DirectorVO> selectDirectorListByContentNo(
            int contentNo);

    List<OttPlatformVO> selectOttPlatformListByContentNo(
            int contentNo);

    List<ContentVO> selectMainContentList();

    List<ContentVO> selectContentListByType(
            Map<String, Object> param);
}
