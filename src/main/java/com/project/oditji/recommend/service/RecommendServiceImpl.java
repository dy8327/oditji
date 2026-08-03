package com.project.oditji.recommend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.recommend.dao.RecommendDAO;
import com.project.oditji.recommend.vo.RecommendOttResultVO;
import com.project.oditji.recommend.vo.RecommendOttScoreVO;

/**
 * 회원의 콘텐츠 조회 이력과 찜 목록을 분석해
 * 맞춤 OTT 추천 결과를 생성하는 서비스 구현체입니다.
 */
@Service
public class RecommendServiceImpl
        implements RecommendService {

    /*
     * OTT 추천을 시작하기 위한
     * 최소 서로 다른 관심 콘텐츠 수입니다.
     */
    private static final int
            MIN_TOTAL_INTEREST_CONTENT_COUNT = 5;

    /*
     * 1위 OTT가 넘어야 하는 최소 관심도 점수입니다.
     */
    private static final int
            MIN_TOP_PLATFORM_SCORE = 15;

    /*
     * 1위 OTT가 제공해야 하는
     * 최소 서로 다른 관심 콘텐츠 수입니다.
     */
    private static final int
            MIN_TOP_PLATFORM_CONTENT_COUNT = 4;

    /*
     * 1위와 2위 점수 차이가 이 값보다 작으면
     * 공동 추천 여부를 검사합니다.
     */
    private static final int
            JOINT_RECOMMENDATION_SCORE_GAP = 5;

    private final RecommendDAO recommendDAO;

    public RecommendServiceImpl(
            RecommendDAO recommendDAO) {

        this.recommendDAO = recommendDAO;
    }

    /**
     * 회원의 OTT 추천 결과를 계산합니다.
     *
     * 처리 순서:
     * 1. 최근 조회와 찜의 전체 관심 콘텐츠 수 확인
     * 2. OTT별 조회 점수와 찜 점수 계산
     * 3. 최소 추천 조건 검사
     * 4. 단독 추천 또는 공동 추천 판정
     * 5. 최종 추천 OTT 전체의 콘텐츠 제공 범위를 합집합으로 계산
     * 6. 화면에서 사용할 추천 이유 생성
     */
    @Override
    @Transactional(readOnly = true)
    public RecommendOttResultVO getOttRecommendation(
            Long memberNo) {

        RecommendOttResultVO result =
                new RecommendOttResultVO();

        /*
         * 로그인하지 않은 사용자는
         * 개인 조회 이력과 찜 정보가 없으므로 추천하지 않습니다.
         */
        if (memberNo == null
                || memberNo <= 0) {

            result.setRecommendationAvailable(
                    false);

            result.setStatusMessage(
                    "로그인 후 맞춤 OTT 추천을 확인할 수 있어요.");

            return result;
        }

        int totalInterestContentCount =
                recommendDAO
                        .countDistinctInterestContent(
                                memberNo);

        result.setTotalInterestContentCount(
                totalInterestContentCount);

        List<RecommendOttScoreVO> scoreList =
                recommendDAO
                        .selectOttInterestScoreList(
                                memberNo);

        if (scoreList == null) {

            scoreList =
                    new ArrayList<RecommendOttScoreVO>();
        }

        /*
         * Mapper에서도 정렬하지만
         * 향후 SQL이 변경되더라도 추천 순위가 유지되도록
         * Service에서 한 번 더 명확하게 정렬합니다.
         */
        scoreList.sort(
                Comparator
                        .comparingInt(
                                RecommendOttScoreVO
                                        ::getTotalScore)
                        .reversed()
                        .thenComparing(
                                Comparator.comparingInt(
                                        RecommendOttScoreVO
                                                ::getInterestContentCount)
                                        .reversed())
                        .thenComparing(
                                Comparator.comparingInt(
                                        RecommendOttScoreVO
                                                ::getFavoriteContentCount)
                                        .reversed())
        );

        result.setRankedPlatformList(
                new ArrayList<RecommendOttScoreVO>(
                        scoreList));

        /*
         * 전체 관심 콘텐츠가 5개 미만이면
         * 특정 OTT를 추천하기에는 기록이 부족한 상태입니다.
         */
        if (totalInterestContentCount
                < MIN_TOTAL_INTEREST_CONTENT_COUNT) {

            result.setRecommendationAvailable(
                    false);

            result.setStatusMessage(
                    "아직 OTT를 추천하기 위한 관심 기록이 부족해요. "
                    + "콘텐츠를 조금 더 둘러보거나 찜해 주세요.");

            return result;
        }

        if (scoreList.isEmpty()) {

            result.setRecommendationAvailable(
                    false);

            result.setStatusMessage(
                    "관심 콘텐츠를 제공하는 OTT 정보를 찾지 못했어요.");

            return result;
        }

        RecommendOttScoreVO firstPlatform =
                scoreList.get(0);

        /*
         * 1위 OTT 점수가 15점 미만이면
         * 관심이 충분히 집중되지 않은 것으로 판단합니다.
         */
        if (firstPlatform.getTotalScore()
                < MIN_TOP_PLATFORM_SCORE) {

            result.setRecommendationAvailable(
                    false);

            result.setStatusMessage(
                    "아직 한 OTT를 추천할 만큼 관심도가 충분히 쌓이지 않았어요.");

            return result;
        }

        /*
         * 1위 OTT가 제공하는 관심 콘텐츠가 4개 미만이면
         * 일부 콘텐츠에 의한 과도한 추천일 수 있으므로 보류합니다.
         */
        if (firstPlatform.getInterestContentCount()
                < MIN_TOP_PLATFORM_CONTENT_COUNT) {

            result.setRecommendationAvailable(
                    false);

            result.setStatusMessage(
                    "관심 콘텐츠의 종류가 조금 더 쌓이면 "
                    + "더 정확한 OTT 추천을 받을 수 있어요.");

            return result;
        }

        result.setRecommendationAvailable(
                true);

        List<RecommendOttScoreVO>
                recommendedPlatformList =
                new ArrayList<RecommendOttScoreVO>();

        recommendedPlatformList.add(
                firstPlatform);

        /*
         * 공동 추천 조건:
         * - 2위 OTT가 존재
         * - 2위도 최소 15점 이상
         * - 1위와 2위 점수 차이가 5점 미만
         */
        if (scoreList.size() >= 2) {

            RecommendOttScoreVO secondPlatform =
                    scoreList.get(1);

            int scoreGap =
                    firstPlatform.getTotalScore()
                    - secondPlatform.getTotalScore();

            boolean jointRecommendation =
                    secondPlatform.getTotalScore()
                            >= MIN_TOP_PLATFORM_SCORE
                    && scoreGap
                            < JOINT_RECOMMENDATION_SCORE_GAP;

            if (jointRecommendation) {

                recommendedPlatformList.add(
                        secondPlatform);

                result.setJointRecommendation(
                        true);

                result.setStatusMessage(
                        firstPlatform.getPlatformName()
                        + "와 "
                        + secondPlatform.getPlatformName()
                        + "가 회원님의 관심 콘텐츠에 "
                        + "비슷하게 잘 맞아요.");
            }
        }

        if (!result.isJointRecommendation()) {

            result.setStatusMessage(
                    firstPlatform.getPlatformName()
                    + "가 회원님의 최근 관심 콘텐츠에 "
                    + "가장 잘 맞아요.");
        }

        /*
         * 추천 대상으로 확정된 OTT 카드에 표시할 콘텐츠를 조회합니다.
         * 각 목록은 Mapper에서 최대 2개로 제한됩니다.
         */
        for (RecommendOttScoreVO platform : recommendedPlatformList) {

            platform.setFavoriteContentList(
                    recommendDAO.selectFavoriteContentList(
                            memberNo,
                            platform.getPlatformNo()));

            platform.setViewedContentList(
                    recommendDAO.selectRecentViewedContentList(
                            memberNo,
                            platform.getPlatformNo()));
        }

        result.setRecommendedPlatformList(
                recommendedPlatformList);

        /*
         * 단독 추천이면 1개 OTT, 공동 추천이면 2개 OTT의 번호를 모읍니다.
         * 이후 SQL의 IN 조건에서 사용해 추천 OTT 전체가 제공하는
         * 관심 콘텐츠와 찜 콘텐츠를 합집합 기준으로 계산합니다.
         */
        List<Integer> recommendedPlatformNoList =
                extractPlatformNoList(
                        recommendedPlatformList);

        int providedInterestContentCount =
                firstPlatform.getInterestContentCount();

        int providedFavoriteContentCount =
                firstPlatform.getFavoriteContentCount();

        if (!recommendedPlatformNoList.isEmpty()) {

            providedInterestContentCount =
                    recommendDAO
                            .countDistinctInterestContentByPlatforms(
                                    memberNo,
                                    recommendedPlatformNoList);

            providedFavoriteContentCount =
                    recommendDAO
                            .countDistinctFavoriteContentByPlatforms(
                                    memberNo,
                                    recommendedPlatformNoList);
        }

        result.setRecommendationReasons(
                createRecommendationReasons(
                        recommendedPlatformList,
                        totalInterestContentCount,
                        providedInterestContentCount,
                        providedFavoriteContentCount,
                        result.isJointRecommendation()));

        return result;
    }

    /**
     * 최종 추천 OTT 목록에서 플랫폼 번호만 추출합니다.
     *
     * 공동 추천 콘텐츠 수를 계산하는 Mapper의 IN 조건에
     * 안전하게 전달하기 위해 null 플랫폼 번호는 제외합니다.
     */
    private List<Integer> extractPlatformNoList(
            List<RecommendOttScoreVO>
                    recommendedPlatformList) {

        if (recommendedPlatformList == null
                || recommendedPlatformList.isEmpty()) {

            return Collections.emptyList();
        }

        List<Integer> platformNoList =
                new ArrayList<Integer>();

        for (RecommendOttScoreVO platform
                : recommendedPlatformList) {

            if (platform != null
                    && platform.getPlatformNo() != null) {

                platformNoList.add(
                        platform.getPlatformNo());
            }
        }

        return platformNoList;
    }

    /**
     * 추천 이유 문구에 사용할 OTT 이름을 생성합니다.
     *
     * 단독 추천이면 "Netflix",
     * 공동 추천이면 "Netflix와 TVING" 형태로 반환합니다.
     */
    private String createRecommendedPlatformNameText(
            List<RecommendOttScoreVO>
                    recommendedPlatformList) {

        if (recommendedPlatformList == null
                || recommendedPlatformList.isEmpty()) {

            return "추천 OTT";
        }

        StringBuilder platformNameText =
                new StringBuilder();

        for (RecommendOttScoreVO platform
                : recommendedPlatformList) {

            if (platform == null
                    || platform.getPlatformName() == null
                    || platform.getPlatformName().isBlank()) {

                continue;
            }

            if (!platformNameText.isEmpty()) {
                platformNameText.append("와 ");
            }

            platformNameText.append(
                    platform.getPlatformName());
        }

        if (platformNameText.isEmpty()) {
            return "추천 OTT";
        }

        return platformNameText.toString();
    }

    /**
     * 최종 추천 OTT 전체의 계산 결과를 바탕으로
     * 화면에 표시할 추천 이유를 생성합니다.
     *
     * 공동 추천일 때는 1위 OTT의 개수만 사용하지 않고,
     * 1위와 2위가 제공하는 콘텐츠를 합친 뒤
     * 중복 콘텐츠를 제거한 개수를 사용합니다.
     */
    private List<String> createRecommendationReasons(
            List<RecommendOttScoreVO>
                    recommendedPlatformList,
            int totalInterestContentCount,
            int providedInterestContentCount,
            int providedFavoriteContentCount,
            boolean jointRecommendation) {

        if (recommendedPlatformList == null
                || recommendedPlatformList.isEmpty()) {

            return Collections.emptyList();
        }

        List<String> reasonList =
                new ArrayList<String>();

        String recommendedPlatformNameText =
                createRecommendedPlatformNameText(
                        recommendedPlatformList);

        /*
         * 전체 관심 콘텐츠 수와 추천 OTT 제공 수를 함께 표시해
         * 화면의 숫자가 무엇을 의미하는지 명확하게 보여줍니다.
         *
         * 공동 추천이면 "Netflix와 TVING에서"처럼 두 OTT 이름을
         * 함께 표시하고, 제공 개수는 중복을 제거한 합집합 개수입니다.
         */
        reasonList.add(
                recommendedPlatformNameText
                + "에서 최근 관심 콘텐츠 "
                + totalInterestContentCount
                + "개 중 "
                + providedInterestContentCount
                + "개를 볼 수 있어요.");

        if (providedFavoriteContentCount > 0) {

            reasonList.add(
                    recommendedPlatformNameText
                    + "에서 찜한 콘텐츠 "
                    + providedFavoriteContentCount
                    + "개를 볼 수 있어요.");
        }

        int combinedViewScore = 0;
        int combinedFavoriteScore = 0;

        /*
         * 추천 이유가 공동 추천된 두 OTT 모두를 설명하도록
         * 최종 추천 OTT들의 조회 점수와 찜 점수를 합산합니다.
         */
        for (RecommendOttScoreVO platform
                : recommendedPlatformList) {

            if (platform == null) {
                continue;
            }

            combinedViewScore +=
                    platform.getViewScore();

            combinedFavoriteScore +=
                    platform.getFavoriteScore();
        }

        if (combinedViewScore
                > combinedFavoriteScore) {

            reasonList.add(
                    "최근 자주 확인한 콘텐츠의 조회 기록이 "
                    + "추천에 가장 크게 반영됐어요.");

        } else if (combinedFavoriteScore > 0) {

            reasonList.add(
                    "현재 찜한 콘텐츠가 추천 결과에 반영됐어요.");
        }

        if (jointRecommendation) {

            reasonList.add(
                    "상위 두 OTT의 관심도 점수 차이가 작아 "
                    + "함께 추천했어요.");
        }

        return reasonList;
    }
}