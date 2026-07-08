package com.project.oditji.content.dao;

import java.util.List;

import com.project.oditji.content.vo.ContentVO;

public interface ContentDAO {

    ContentVO selectContentByTmdbId(Long tmdbId, String contentType);

    ContentVO selectContentByContentNo(int contentNo);

    int insertContent(ContentVO content);

    int increaseViewCount(int contentNo);

        // 메인 페이지 콘텐츠 리스트 조회 (홈 화면 노출용)
    List<ContentVO> selectMainContentList();
}