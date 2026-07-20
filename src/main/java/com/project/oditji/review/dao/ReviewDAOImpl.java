package com.project.oditji.review.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.project.oditji.review.vo.ContentReviewVO;
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
        return sqlSession.selectList(NAMESPACE + "selectMyContentReviewList", memberNo);
    }

    @Override
    public List<MyReviewVO> selectMyProductReviewList(Long memberNo) {
        return sqlSession.selectList(NAMESPACE + "selectMyProductReviewList", memberNo);
    }

    @Override
    public int countMyContentReview(Long memberNo) {
        return sqlSession.selectOne(NAMESPACE + "countMyContentReview", memberNo);
    }

    @Override
    public int countMyProductReview(Long memberNo) {
        return sqlSession.selectOne(NAMESPACE + "countMyProductReview", memberNo);
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
        return sqlSession.selectOne(NAMESPACE + "selectContentReviewByReviewNo", reviewNo);
    }

    @Override
    public int insertProductReview(ProductReviewVO productReview) {
        return sqlSession.insert(NAMESPACE + "insertProductReview", productReview);
    }

    @Override
    public ReviewVO selectContentReviewByMemberAndContent(Map<String, Object> param) {
        return sqlSession.selectOne(NAMESPACE + "selectContentReviewByMemberAndContent", param);
    }

    @Override
    public ProductReviewVO selectProductReviewByOrderItem(int orderItemNo) {
        return sqlSession.selectOne(NAMESPACE + "selectProductReviewByOrderItem", orderItemNo);
    }

    @Override
    public List<ContentReviewVO> selectContentReviewListByContentNo(int contentNo) {
        return sqlSession.selectList(NAMESPACE + "selectContentReviewListByContentNo", contentNo);
    }

    @Override
    public Double selectAvgRatingByContentNo(int contentNo) {
        return sqlSession.selectOne(NAMESPACE + "selectAvgRatingByContentNo", contentNo);
    }

    @Override
    public int selectReviewCountByContentNo(int contentNo) {
        return sqlSession.selectOne(NAMESPACE + "selectReviewCountByContentNo", contentNo);
    }

    @Override
    public List<Integer> selectReportedContentReviewNoList(Long memberNo) {
        return sqlSession.selectList(NAMESPACE + "selectReportedContentReviewNoList", memberNo);
    }

    @Override
    public int deleteContentReview(int reviewNo) {
        return sqlSession.update(NAMESPACE + "deleteContentReview", reviewNo);
    }

    @Override
    public ProductReviewVO selectProductReviewByReviewNo(int reviewNo) {
        return sqlSession.selectOne(NAMESPACE + "selectProductReviewByReviewNo", reviewNo);
    }

    @Override
    public int deleteProductReview(int reviewNo) {
        return sqlSession.delete(NAMESPACE + "deleteProductReview", reviewNo);
    }

    @Override
    public ReviewVO selectContentReviewByMemberAndContentAnyStatus(Map<String, Object> param) {
        return sqlSession.selectOne(NAMESPACE + "selectContentReviewByMemberAndContentAnyStatus", param);
    }

    @Override
    public int reactivateContentReview(ReviewVO review) {
        return sqlSession.update(NAMESPACE + "reactivateContentReview", review);
    }

    @Override
    public List<ProductReviewVO> selectProductReviewListByProductNo(
            int productNo) {

        return sqlSession.selectList(
                NAMESPACE + "selectProductReviewListByProductNo",
                productNo
        );
    }

    @Override
    public Double selectProductAvgRatingByProductNo(
            int productNo) {

        return sqlSession.selectOne(
                NAMESPACE + "selectProductAvgRatingByProductNo",
                productNo
        );
    }

    @Override
    public int selectProductReviewCountByProductNo(
            int productNo) {

        return sqlSession.selectOne(
                NAMESPACE + "selectProductReviewCountByProductNo",
                productNo
        );
    }

    @Override
    public int countMyOrderItem(Map<String, Object> param) {
        return sqlSession.selectOne(
                NAMESPACE + "countMyOrderItem",
                param
        );
    }

}