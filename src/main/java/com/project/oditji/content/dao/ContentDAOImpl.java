package com.project.oditji.content.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.content.vo.ContentViewHistoryVO;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

/**
 * ContentDAO 구현체입니다.
 *
 * MyBatis SqlSession을 이용해
 * 콘텐츠 정보와 콘텐츠 조회 이력을 처리합니다.
 */
@Repository
public class ContentDAOImpl implements ContentDAO {

    private final SqlSession sqlSession;

    private static final String NAMESPACE =
            "com.project.oditji.content.dao.ContentDAO.";

    public ContentDAOImpl(
            SqlSession sqlSession) {

        this.sqlSession = sqlSession;
    }

    @Override
    public ContentVO selectContentByTmdbId(
            Long tmdbId,
            String contentType) {

        ContentVO param =
                new ContentVO();

        param.setTmdbId(tmdbId);
        param.setContentType(contentType);

        return sqlSession.selectOne(
                NAMESPACE + "selectContentByTmdbId",
                param
        );
    }

    @Override
    public ContentVO selectContentByContentNo(
            int contentNo) {

        return sqlSession.selectOne(
                NAMESPACE + "selectContentByContentNo",
                contentNo
        );
    }

    @Override
    public int insertContent(
            ContentVO content) {

        return sqlSession.insert(
                NAMESPACE + "insertContent",
                content
        );
    }

    @Override
    public int updateContentFromSearchCache(
            ContentVO content) {

        return sqlSession.update(
                NAMESPACE + "updateContentFromSearchCache",
                content
        );
    }

    @Override
    public int increaseViewCount(
            int contentNo) {

        return sqlSession.update(
                NAMESPACE + "increaseViewCount",
                contentNo
        );
    }

    /**
     * 회원의 오늘 상세페이지 조회 이력을
     * Oracle MERGE 문으로 등록 또는 갱신합니다.
     */
    @Override
    public int mergeContentViewHistory(
            ContentViewHistoryVO historyVO) {

        return sqlSession.update(
                NAMESPACE + "mergeContentViewHistory",
                historyVO
        );
    }

    /**
     * 추천 및 관리자 통계의 분석 범위를 벗어난
     * 30일 초과 조회 이력을 삭제합니다.
     */
    @Override
    public int deleteExpiredContentViewHistory() {

        return sqlSession.delete(
                NAMESPACE
                        + "deleteExpiredContentViewHistory"
        );
    }

    @Override
    public List<ActorVO> selectActorListByContentNo(
            int contentNo) {

        return sqlSession.selectList(
                NAMESPACE + "selectActorListByContentNo",
                contentNo
        );
    }

    @Override
    public List<DirectorVO> selectDirectorListByContentNo(
            int contentNo) {

        return sqlSession.selectList(
                NAMESPACE + "selectDirectorListByContentNo",
                contentNo
        );
    }

    @Override
    public List<OttPlatformVO> selectOttPlatformListByContentNo(
            int contentNo) {

        return sqlSession.selectList(
                NAMESPACE + "selectOttPlatformListByContentNo",
                contentNo
        );
    }

    @Override
    public List<ContentVO> selectMainContentList() {

        return sqlSession.selectList(
                NAMESPACE + "selectMainContentList"
        );
    }

    @Override
    public List<ContentVO> selectContentListByType(
            Map<String, Object> param) {

        return sqlSession.selectList(
                NAMESPACE + "selectContentListByType",
                param
        );
    }
}