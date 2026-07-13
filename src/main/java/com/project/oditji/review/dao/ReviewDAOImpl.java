package com.project.oditji.review.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.project.oditji.review.vo.MyReviewVO;
import com.project.oditji.review.vo.ProductReviewVO;
import com.project.oditji.review.vo.ReviewVO;

@Repository
public class ReviewDAOImpl implements ReviewDAO {

    private final SqlSession sqlSession;

    private static final String NAMESPACE = "com.project.oditji.review.dao.ReviewDAO.";

    public ReviewDAOImpl(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public List<MyReviewVO> selectMyContentReviewList(Long memberNo) {
        return sqlSession.selectList(
                NAMESPACE + "selectMyContentReviewList",
                memberNo);
    }

    @Override
    public List<MyReviewVO> selectMyProductReviewList(Long memberNo) {
        return sqlSession.selectList(
                NAMESPACE + "selectMyProductReviewList",
                memberNo);
    }

    @Override
    public int countMyContentReview(Long memberNo) {
        return sqlSession.selectOne(
                NAMESPACE + "countMyContentReview",
                memberNo);
    }

    @Override
    public int countMyProductReview(Long memberNo) {
        return sqlSession.selectOne(
                NAMESPACE + "countMyProductReview",
                memberNo);
    }

    @Override
    public int insertContentReview(ReviewVO review) {
        return sqlSession.insert(NAMESPACE + "insertContentReview", review);
    }

    @Override
    public int updateContentReview(ReviewVO review) {
        return sqlSession.update(NAMESPACE + "updateContentReview", review);
    }

    @Override
    public ReviewVO selectContentReviewByReviewNo(int reviewNo) {
        return sqlSession.selectOne(
                NAMESPACE + "selectContentReviewByReviewNo",
                reviewNo);
    }

    @Override
    public int insertProductReview(ProductReviewVO productReview) {
        return sqlSession.insert(NAMESPACE + "insertProductReview", productReview);
    }

    @Override
    public ReviewVO selectContentReviewByMemberAndContent(Map<String, Object> param) {
        return sqlSession.selectOne(
                NAMESPACE + "selectContentReviewByMemberAndContent",
                param);
    }

    @Override
    public ProductReviewVO selectProductReviewByOrderItem(int orderItemNo) {
        return sqlSession.selectOne(
                NAMESPACE + "selectProductReviewByOrderItem",
                orderItemNo);
    }
}
