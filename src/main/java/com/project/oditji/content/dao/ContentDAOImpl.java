package com.project.oditji.content.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

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
