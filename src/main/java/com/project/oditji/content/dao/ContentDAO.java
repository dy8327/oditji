package com.project.oditji.content.dao;

import java.util.List;
import java.util.Map;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.content.vo.ContentViewHistoryVO;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

/**
 * 콘텐츠 및 콘텐츠 조회 이력 관련 DB 접근 인터페이스입니다.
 *
 * 기존 CONTENT 조회·저장 기능과 함께
 * 로그인 회원의 상세페이지 방문 기록을 관리합니다.
 */
public interface ContentDAO {

    ContentVO selectContentByTmdbId(
            Long tmdbId,
            String contentType);

    ContentVO selectContentByContentNo(
            int contentNo);

    int insertContent(
            ContentVO content);

    /**
     * 검색 JSON 캐시에서 가져온 값을
     * 기존 CONTENT 행에 반영합니다.
     */
    int updateContentFromSearchCache(
            ContentVO content);

    /**
     * 콘텐츠 전체 누적 조회수를 증가시킵니다.
     *
     * CONTENT_VIEW_HISTORY와는 별개의 기존 통계값입니다.
     */
    int increaseViewCount(
            int contentNo);

    /**
     * 로그인 회원의 오늘 콘텐츠 조회 이력을 저장합니다.
     *
     * 오늘 기록이 없으면 INSERT하고,
     * 이미 있으면 VIEW_COUNT와 LAST_VIEWED_AT을 갱신합니다.
     */
    int mergeContentViewHistory(
            ContentViewHistoryVO historyVO);

    /**
     * 오늘을 포함한 최근 30일보다 오래된
     * 콘텐츠 조회 이력을 삭제합니다.
     */
    int deleteExpiredContentViewHistory();

    List<ActorVO> selectActorListByContentNo(
            int contentNo);

    List<DirectorVO> selectDirectorListByContentNo(
            int contentNo);

    List<OttPlatformVO> selectOttPlatformListByContentNo(
            int contentNo);

    List<ContentVO> selectMainContentList();

    List<ContentVO> selectContentListByType(
            Map<String, Object> param);

    /**
     * 로그인 회원이 최근 조회한 콘텐츠를
     * 마지막 조회 시각(LAST_VIEWED_AT) 내림차순으로 조회합니다.
     *
     * CONTENT_VIEW_HISTORY는 콘텐츠·날짜별로 여러 행이 있을 수 있으므로
     * 콘텐츠 기준으로 그룹화한 뒤 가장 최근 조회 시각으로 정렬합니다.
     *
     * param: memberNo(Long), limit(int)
     */
    List<ContentVO> selectRecentViewedContentList(
            Map<String, Object> param);
}