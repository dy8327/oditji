package com.project.oditji.subscription.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 마이페이지 '구독 계산 결과' 모달에서 회원이 저장한 결과 목록을 보여줄 때 쓰는 응답 전용 VO입니다.
 *
 * SubscriptionShareVO(SUBSCRIPTION_RESULT 테이블 매핑)를 그대로 내려주는 대신,
 * SELECTED_SERVICES JSON을 서버에서 미리 플랫폼 이름 목록으로 풀어서 내려준다.
 * 화면(모달)이 필요로 하지 않는 원본 JSON 문자열은 이 VO에 담지 않는다.
 */
public class SubscriptionSavedResultVO {

    private String resultId;

    private LocalDateTime createdAt;

    private int totalPrice;

    private int discountPrice;

    private int finalPrice;

    /** SELECTED_SERVICES JSON에서 뽑아낸 선택 플랫폼 명칭 목록 */
    private List<String> platformNameList = new ArrayList<String>();

    /**
     * SELECTED_SERVICES JSON에서 뽑아낸, 계산에 사용된 콘텐츠(담은 작품) 목록입니다.
     * 마이페이지 '구독 계산 결과' 모달에서 어떤 콘텐츠를 골라 계산했는지 보여줄 때 씁니다.
     */
    private List<ContentWishItemVO> contentList = new ArrayList<ContentWishItemVO>();

    /**
     * 선택 플랫폼별로 그 플랫폼에서 볼 수 있는 콘텐츠를 묶어 담은 목록입니다.
     * (각 PlatformPriceVO.contentList에 해당 플랫폼의 콘텐츠가 채워져 있음)
     * 마이페이지 모달에서 "어떤 콘텐츠가 어떤 OTT에 있는지" OTT별로 묶어 보여줄 때 씁니다.
     */
    private List<PlatformPriceVO> platformGroupList = new ArrayList<PlatformPriceVO>();

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(int discountPrice) {
        this.discountPrice = discountPrice;
    }

    public int getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(int finalPrice) {
        this.finalPrice = finalPrice;
    }

    public List<String> getPlatformNameList() {
        return platformNameList;
    }

    public void setPlatformNameList(List<String> platformNameList) {
        this.platformNameList =
                platformNameList == null
                        ? new ArrayList<String>()
                        : platformNameList;
    }

    public List<ContentWishItemVO> getContentList() {
        return contentList;
    }

    public void setContentList(List<ContentWishItemVO> contentList) {
        this.contentList =
                contentList == null
                        ? new ArrayList<ContentWishItemVO>()
                        : contentList;
    }

    public List<PlatformPriceVO> getPlatformGroupList() {
        return platformGroupList;
    }

    public void setPlatformGroupList(List<PlatformPriceVO> platformGroupList) {
        this.platformGroupList =
                platformGroupList == null
                        ? new ArrayList<PlatformPriceVO>()
                        : platformGroupList;
    }
}
