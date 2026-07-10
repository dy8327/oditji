package com.project.oditji.content.dao;

import java.util.List;
import java.util.Map;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;

public interface ContentDAO {

    ContentVO selectContentByTmdbId(Long tmdbId, String contentType);

    ContentVO selectContentByContentNo(int contentNo);

    int insertContent(ContentVO content);

    int increaseViewCount(int contentNo);

    List<ActorVO> selectActorListByContentNo(int contentNo);

    List<DirectorVO> selectDirectorListByContentNo(int contentNo);

    List<ContentVO> selectMainContentList();

    List<ContentVO> selectContentListByType(Map<String, Object> param);
}
