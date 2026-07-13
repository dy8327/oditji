package com.project.oditji.recommend.vo;

import java.util.ArrayList;
import java.util.List;

import com.project.oditji.search.vo.SearchResultVO;

public class RecommendPlatformSectionVO {

    private Long platformNo;
    private String platformName;
    private String logoImage;
    private List<SearchResultVO> contentList;

    public RecommendPlatformSectionVO() {
        this.contentList =
                new ArrayList<SearchResultVO>();
    }

    public Long getPlatformNo() {
        return platformNo;
    }

    public void setPlatformNo(Long platformNo) {
        this.platformNo = platformNo;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getLogoImage() {
        return logoImage;
    }

    public void setLogoImage(String logoImage) {
        this.logoImage = logoImage;
    }

    public List<SearchResultVO> getContentList() {
        return contentList;
    }

    public void setContentList(
            List<SearchResultVO> contentList) {

        this.contentList =
                contentList == null
                        ? new ArrayList<SearchResultVO>()
                        : contentList;
    }
}
