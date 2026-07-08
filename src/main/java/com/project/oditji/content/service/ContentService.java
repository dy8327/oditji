package com.project.oditji.content.service;

import java.util.List;

import com.project.oditji.content.vo.ContentVO;

public interface ContentService {

    int prepareContentDetail(Long tmdbId, String contentType);

    ContentVO getContentDetail(int contentNo);

        // 메인 화면 콘텐츠 리스트 조회 (홈 화면 데이터 제공용)
    List<ContentVO> getMainContentList();
}