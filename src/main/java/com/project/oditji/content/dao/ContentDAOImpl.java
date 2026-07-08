package com.project.oditji.content.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.project.oditji.content.vo.ContentVO;

@Repository
public class ContentDAOImpl implements ContentDAO {

    private final SqlSession sqlSession;

    private static final String NAMESPACE = "com.project.oditji.content.dao.ContentDAO.";

    public ContentDAOImpl(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public ContentVO selectContentByTmdbId(Long tmdbId, String contentType) {

        ContentVO param = new ContentVO();
        param.setTmdbId(tmdbId);
        param.setContentType(contentType);

        return sqlSession.selectOne(NAMESPACE + "selectContentByTmdbId", param);
    }

    @Override
    public ContentVO selectContentByContentNo(int contentNo) {
        return sqlSession.selectOne(NAMESPACE + "selectContentByContentNo", contentNo);
    }

    @Override
    public int insertContent(ContentVO content) {
        return sqlSession.insert(NAMESPACE + "insertContent", content);
    }

    @Override
    public int increaseViewCount(int contentNo) {
        return sqlSession.update(NAMESPACE + "increaseViewCount", contentNo);
    }

    // MyBatis를 통한 메인 콘텐츠 리스트 조회 (200개 제한, 정렬 포함)
    @Override
    public List<ContentVO> selectMainContentList() {
        return sqlSession.selectList(
            NAMESPACE + "selectMainContentList"
        );
    }
}