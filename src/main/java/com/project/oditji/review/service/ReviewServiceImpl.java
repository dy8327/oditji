package com.project.oditji.review.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.review.dao.ReviewDAO;
import com.project.oditji.review.vo.ContentReviewVO;
import com.project.oditji.review.vo.ContentSpoilerSourceVO;
import com.project.oditji.review.vo.MyReviewVO;
import com.project.oditji.review.vo.ProductReviewVO;
import com.project.oditji.review.vo.ReviewVO;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewDAO reviewDAO;

    public ReviewServiceImpl(ReviewDAO reviewDAO) {
        this.reviewDAO = reviewDAO;
    }

    @Override
    public List<MyReviewVO> getMyReviewList(Long memberNo) {

        List<MyReviewVO> contentReviewList = reviewDAO.selectMyContentReviewList(memberNo);

        List<MyReviewVO> productReviewList = reviewDAO.selectMyProductReviewList(memberNo);

        List<MyReviewVO> myReviewList = new ArrayList<>();

        if (contentReviewList != null) {
            myReviewList.addAll(contentReviewList);
        }

        if (productReviewList != null) {
            myReviewList.addAll(productReviewList);
        }

        myReviewList.sort(
                Comparator.comparing(MyReviewVO::getCreatedAt).reversed());

        return myReviewList;
    }

    @Override
    public int getMyReviewCount(Long memberNo) {

        int contentReviewCount = reviewDAO.countMyContentReview(memberNo);
        int productReviewCount = reviewDAO.countMyProductReview(memberNo);

        return contentReviewCount + productReviewCount;
    }

    @Override
    @Transactional
    public void writeContentReview(
            Long memberNo, int contentNo, double rating, String reviewText,
            String spoilerYn) {

        if (memberNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("별점은 0~5점 사이여야 합니다.");
        }

        if (reviewText == null || reviewText.trim().isEmpty()) {
            throw new IllegalArgumentException("리뷰 내용을 입력해주세요.");
        }

        // [추가] 사용자 체크, 결말 표현, OVERVIEW 주요 단어 기준으로 판별한다.
        String finalSpoilerYn = determineSpoilerYn(spoilerYn, reviewText, contentNo);
        Map<String, Object> checkParam = new HashMap<>();
        checkParam.put("memberNo", memberNo);
        checkParam.put("contentNo", contentNo);

        // 상태(ACTIVE/DELETED) 무관하고 조회 - 삭제했던 리뷰도 잡아냄
        ReviewVO existingReview = reviewDAO.selectContentReviewByMemberAndContentAnyStatus(checkParam);

        if (existingReview != null) {

            if (!"DELETED".equals(existingReview.getStatus())) {
                // ACTIVE 리뷰가 이미 있으면 차단
                throw new IllegalStateException("이미 작성한 리뷰가 있습니다.");
            }

            // 삭제했던 리뷰라면 새로 INSERT 하지 않고 기존 row를 재활용
            ReviewVO reactivated = new ReviewVO();
            reactivated.setReviewNo(existingReview.getReviewNo());
            reactivated.setRating(rating);
            reactivated.setReviewText(reviewText);
            // [추가] 재작성된 리뷰에도 판별 결과를 저장한다.
            reactivated.setSpoilerYn(finalSpoilerYn);

            reviewDAO.reactivateContentReview(reactivated);
            return;
        }

        ReviewVO review = new ReviewVO();
        review.setMemberNo(memberNo);
        review.setContentNo(contentNo);
        review.setRating(rating);
        review.setReviewText(reviewText);
        // [추가] 최종 스포일러 판별 결과 저장
        review.setSpoilerYn(finalSpoilerYn);

        reviewDAO.insertContentReview(review);
    }

    @Override
    @Transactional
    public void updateContentReview(
            Long memberNo, Long reviewNo, int contentNo, double rating,
            String reviewText, String spoilerYn) {
        if (memberNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("별점은 0~5점 사이여야 합니다.");
        }

        if (reviewText == null || reviewText.trim().isEmpty()) {
            throw new IllegalArgumentException("리뷰 내용을 입력해주세요.");
        }

        ReviewVO existingReview = reviewDAO.selectContentReviewByReviewNo(reviewNo);

        if (existingReview == null) {
            throw new IllegalArgumentException("존재하지 않는 리뷰입니다.");
        }

        if (!memberNo.equals(existingReview.getMemberNo())) {
            throw new IllegalStateException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        // [추가] 수정된 리뷰도 같은 기준으로 다시 판별한다.
        String finalSpoilerYn = determineSpoilerYn(spoilerYn, reviewText, contentNo);

        ReviewVO review = new ReviewVO();
        review.setReviewNo(reviewNo);
        review.setRating(rating);
        review.setReviewText(reviewText);
        // [추가] 수정된 판별 결과 저장
        review.setSpoilerYn(finalSpoilerYn);

        reviewDAO.updateContentReview(review);
    }

    @Override
    @Transactional
    public void writeProductReview(
            Long memberNo, int productNo, int orderItemNo, double rating, String content) {

        System.out.println("===== 상품 리뷰 작성 =====");
        System.out.println("memberNo = " + memberNo);
        System.out.println("productNo = " + productNo);
        System.out.println("orderItemNo = " + orderItemNo);
        System.out.println("rating = " + rating);
        System.out.println("content = " + content);

        if (memberNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("별점은 1~5점 사이여야 합니다.");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("리뷰 내용을 입력해주세요.");
        }

        Map<String, Object> param = new HashMap<>();
        param.put("memberNo", memberNo);
        param.put("productNo", productNo);
        param.put("orderItemNo", orderItemNo);

        int count = reviewDAO.countMyOrderItem(param);
        System.out.println("countMyOrderItem = " + count);

        if (count == 0) {
            throw new IllegalStateException("구매한 상품만 리뷰를 작성할 수 있습니다.");
        }

        ProductReviewVO existingReview = reviewDAO.selectProductReviewByOrderItem(orderItemNo);

        System.out.println("existingReview = " + existingReview);

        if (existingReview != null) {
            throw new IllegalStateException("이미 작성한 리뷰가 있습니다.");
        }

        ProductReviewVO productReview = new ProductReviewVO();
        productReview.setProductNo(productNo);
        productReview.setOrderItemNo(orderItemNo);
        productReview.setMemberNo(memberNo);
        productReview.setRating(rating);
        productReview.setContent(content);

        System.out.println("insert 시작");
        reviewDAO.insertProductReview(productReview);
        System.out.println("insert 완료");
    }

    @Override
    public List<ContentReviewVO> getContentReviewList(int contentNo) {

        List<ContentReviewVO> reviewList = reviewDAO.selectContentReviewListByContentNo(contentNo);

        return reviewList == null
                ? Collections.emptyList()
                : reviewList;
    }

    @Override
    public Double getAvgRating(int contentNo) {
        return reviewDAO.selectAvgRatingByContentNo(contentNo);
    }

    @Override
    public int getReviewCount(int contentNo) {
        return reviewDAO.selectReviewCountByContentNo(contentNo);
    }

    @Override
    public ReviewVO getMyReview(Long memberNo, int contentNo) {

        if (memberNo == null) {
            return null;
        }

        Map<String, Object> param = new HashMap<>();
        param.put("memberNo", memberNo);
        param.put("contentNo", contentNo);

        return reviewDAO.selectContentReviewByMemberAndContent(param);
    }

    @Override
    public Set<Integer> getReportedReviewSet(Long memberNo) {

        if (memberNo == null) {
            return Collections.emptySet();
        }

        List<Integer> reportedList = reviewDAO.selectReportedContentReviewNoList(memberNo);

        return reportedList == null
                ? Collections.emptySet()
                : new HashSet<>(reportedList);
    }

    @Override
    @Transactional
    public void deleteContentReview(Long memberNo, Long reviewNo) {

        if (memberNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        ReviewVO existingReview = reviewDAO.selectContentReviewByReviewNo(reviewNo);

        if (existingReview == null) {
            throw new IllegalArgumentException("존재하지 않는 리뷰입니다.");
        }

        if (!memberNo.equals(existingReview.getMemberNo())) {
            throw new IllegalStateException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        reviewDAO.deleteContentReview(reviewNo);
    }

    @Override
    @Transactional
    public void deleteProductReview(Long memberNo, Long reviewNo) {

        if (memberNo == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        ProductReviewVO existingReview = reviewDAO.selectProductReviewByReviewNo(reviewNo);

        if (existingReview == null) {
            throw new IllegalArgumentException("존재하지 않는 리뷰입니다.");
        }

        if (!memberNo.equals(existingReview.getMemberNo())) {
            throw new IllegalStateException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        reviewDAO.deleteProductReview(reviewNo);
    }

    @Override
    public List<ProductReviewVO> getProductReviewList(
            int productNo) {

        if (productNo <= 0) {
            return Collections.emptyList();
        }

        List<ProductReviewVO> reviewList = reviewDAO.selectProductReviewListByProductNo(
                productNo);

        return reviewList == null
                ? Collections.emptyList()
                : reviewList;
    }

    @Override
    public Double getProductAvgRating(
            int productNo) {

        if (productNo <= 0) {
            return null;
        }

        return reviewDAO.selectProductAvgRatingByProductNo(
                productNo);
    }

    @Override
    public int getProductReviewCount(
            int productNo) {

        if (productNo <= 0) {
            return 0;
        }

        return reviewDAO.selectProductReviewCountByProductNo(
                productNo);
    }

    /**
     * [추가] 스포일러 판별 기준
     * 1) 사용자가 직접 체크하면 Y
     * 2) 결말 관련 표현이 포함되면 Y
     * 3) CONTENT.OVERVIEW의 주요 단어가 2개 이상 포함되면 Y
     * 제목, 배우명, 감독명, 장르명은 조회하지 않으므로 자동 판별에서 제외된다.
     */
    private String determineSpoilerYn(String requestedSpoilerYn, String reviewText, int contentNo) {
        if ("Y".equalsIgnoreCase(requestedSpoilerYn))
            return "Y";
        if (reviewText == null || reviewText.trim().isEmpty())
            return "N";

        String normalizedReview = normalizeSpoilerText(reviewText);
        String[] endingKeywords = {
                "결말", "엔딩", "마지막장면", "마지막에", "최후", "반전",
                "범인은", "범인이", "살인범", "흑막", "정체는", "정체가",
                "알고보니", "알고보면", "사실은", "진실은", "죽는다",
                "죽었다", "죽었", "사망한다", "살아남", "자살", "살해",
                "배신한다", "배신했", "헤어진다", "결혼한다",
                "범인을잡", "사건을해결", "기억을되찾", "꿈이었다", "환상이었다"
        };

        for (String keyword : endingKeywords) {
            if (normalizedReview.contains(normalizeSpoilerText(keyword)))
                return "Y";
        }

        ContentSpoilerSourceVO source = reviewDAO.selectContentSpoilerSource(contentNo);
        if (source == null || source.getOverview() == null || source.getOverview().trim().isEmpty())
            return "N";

        return countOverviewKeywordMatches(source.getOverview(), reviewText) >= 2 ? "Y" : "N";
    }

    // [추가] 비교를 위해 공백·특수문자를 제거한다.
    private String normalizeSpoilerText(String text) {
        if (text == null)
            return "";
        return text.toLowerCase().replaceAll("[^가-힣a-z0-9]", "");
    }

    // [추가] OVERVIEW의 의미 있는 단어가 리뷰에 포함된 개수를 계산한다.
    private int countOverviewKeywordMatches(String overview, String reviewText) {
        String cleanedOverview = overview.toLowerCase()
                .replaceAll("[^가-힣a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ").trim();
        String normalizedReview = normalizeSpoilerText(reviewText);

        Set<String> stopWords = new HashSet<>();
        Collections.addAll(stopWords,
                "그리고", "그러나", "하지만", "그래서", "통해", "대한", "위해",
                "하게", "한다", "되는", "되어", "있는", "없는", "그는", "그녀는",
                "그들이", "자신의", "서로", "영화", "작품", "이야기", "내용",
                "장면", "배우", "감독", "연기", "장르", "재미", "정말", "너무",
                "조금", "보고", "보는", "봤다", "좋다", "좋았다", "최고",
                "추천", "사람", "주인공");

        Set<String> checked = new HashSet<>();
        int matched = 0;
        for (String word : cleanedOverview.split("\\s+")) {
            if (word.length() < 3 || stopWords.contains(word) || !checked.add(word))
                continue;
            if (normalizedReview.contains(normalizeSpoilerText(word)))
                matched++;
        }
        return matched;
    }

}