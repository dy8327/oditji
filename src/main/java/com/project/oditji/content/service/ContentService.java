package com.project.oditji.content.service;

import java.util.List;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.content.vo.PersonFilmographyVO;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;

public interface ContentService {

    int prepareContentDetail(Long tmdbId, String contentType);

    ContentVO getContentDetail(int contentNo);

    List<ActorVO> getActorListByContentNo(int contentNo);

    List<DirectorVO> getDirectorListByContentNo(int contentNo);

    PersonFilmographyVO getPersonFilmography(Long tmdbPersonId, String role);

    // 메인 화면 콘텐츠 리스트 조회 (홈 화면 데이터 제공용)
    List<ContentVO> getMainContentList();

    List<ContentVO> getContentListByType(String type, int page);
}
