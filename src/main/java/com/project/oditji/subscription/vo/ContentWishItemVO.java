package com.project.oditji.subscription.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * OTT 구독 조합 계산기에서 사용자가 "보고 싶은 작품"으로 담은 콘텐츠 한 건입니다.
 *
 * 검색 단계에서 이미 콘텐츠의 플랫폼 목록(platformNameList)을 함께 내려주므로,
 * 계산 단계에서는 콘텐츠를 다시 조회하지 않고 이 값을 그대로 사용합니다.
 */
public class ContentWishItemVO {

    private Long tmdbId;

    private String contentType;

    private String title;

    private String posterPath;

    /** 이 콘텐츠를 볼 수 있는 플랫폼명 목록(OTT_PLATFORM.PLATFORM_NAME 기준, 영문) */
    private List<String> platformNameList = new ArrayList<String>();

    /**
     * [OTT 구독 조합 계산기 - 담은 작품 OTT 로고 표시 추가]
     * platformNameList와 같은 순서로 인덱스가 맞는 로고 이미지 URL 목록.
     * 프런트가 담은 작품 옆에 어떤 OTT에서 볼 수 있는지 아이콘으로 보여주는 용도이며,
     * 계산 로직에서는 사용하지 않는다(가격 계산은 platformNameList만 사용).
     */
    private List<String> platformLogoList = new ArrayList<String>();

    public Long getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(Long tmdbId) {
        this.tmdbId = tmdbId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
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

    public List<String> getPlatformLogoList() {
        return platformLogoList;
    }

    public void setPlatformLogoList(List<String> platformLogoList) {
        this.platformLogoList =
                platformLogoList == null
                        ? new ArrayList<String>()
                        : platformLogoList;
    }
}
