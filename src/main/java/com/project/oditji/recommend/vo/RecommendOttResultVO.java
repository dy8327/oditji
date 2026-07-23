package com.project.oditji.recommend.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 회원에게 제공할 최종 OTT 추천 결과를 담는 VO입니다.
 *
 * 화면 위치가 아직 확정되지 않았더라도
 * 이 VO 하나를 Controller의 Model에 전달하면
 * 추천 페이지와 메인페이지에서 공통으로 사용할 수 있습니다.
 */
public class RecommendOttResultVO {

    /*
     * 추천 활성화 조건을 모두 만족했는지 여부입니다.
     */
    private boolean recommendationAvailable;

    /*
     * 두 OTT를 함께 추천하는 결과인지 여부입니다.
     */
    private boolean jointRecommendation;

    /*
     * 최근 조회 콘텐츠와 현재 찜 콘텐츠를 합친
     * 회원의 서로 다른 관심 콘텐츠 수입니다.
     */
    private int totalInterestContentCount;

    /*
     * 데이터 부족 또는 추천 결과를 설명하는 기본 문구입니다.
     */
    private String statusMessage;

    /*
     * 실제 추천 대상으로 선택된 OTT 목록입니다.
     *
     * 단독 추천이면 한 개,
     * 공동 추천이면 두 개가 담깁니다.
     */
    private List<RecommendOttScoreVO>
            recommendedPlatformList =
            new ArrayList<RecommendOttScoreVO>();

    /*
     * 점수가 계산된 전체 OTT 순위입니다.
     *
     * 관리자 확인이나 추후 비교 화면에 사용할 수 있습니다.
     */
    private List<RecommendOttScoreVO>
            rankedPlatformList =
            new ArrayList<RecommendOttScoreVO>();

    /*
     * 추천 화면에서 보여줄 추천 근거 문구입니다.
     */
    private List<String> recommendationReasons =
            new ArrayList<String>();

    public boolean isRecommendationAvailable() {
        return recommendationAvailable;
    }

    public void setRecommendationAvailable(
            boolean recommendationAvailable) {

        this.recommendationAvailable =
                recommendationAvailable;
    }

    public boolean isJointRecommendation() {
        return jointRecommendation;
    }

    public void setJointRecommendation(
            boolean jointRecommendation) {

        this.jointRecommendation =
                jointRecommendation;
    }

    public int getTotalInterestContentCount() {
        return totalInterestContentCount;
    }

    public void setTotalInterestContentCount(
            int totalInterestContentCount) {

        this.totalInterestContentCount =
                totalInterestContentCount;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(
            String statusMessage) {

        this.statusMessage = statusMessage;
    }

    public List<RecommendOttScoreVO>
            getRecommendedPlatformList() {

        return recommendedPlatformList;
    }

    public void setRecommendedPlatformList(
            List<RecommendOttScoreVO>
                    recommendedPlatformList) {

        this.recommendedPlatformList =
                recommendedPlatformList == null
                        ? new ArrayList<RecommendOttScoreVO>()
                        : recommendedPlatformList;
    }

    public List<RecommendOttScoreVO>
            getRankedPlatformList() {

        return rankedPlatformList;
    }

    public void setRankedPlatformList(
            List<RecommendOttScoreVO>
                    rankedPlatformList) {

        this.rankedPlatformList =
                rankedPlatformList == null
                        ? new ArrayList<RecommendOttScoreVO>()
                        : rankedPlatformList;
    }

    public List<String>
            getRecommendationReasons() {

        return recommendationReasons;
    }

    public void setRecommendationReasons(
            List<String> recommendationReasons) {

        this.recommendationReasons =
                recommendationReasons == null
                        ? new ArrayList<String>()
                        : recommendationReasons;
    }
}