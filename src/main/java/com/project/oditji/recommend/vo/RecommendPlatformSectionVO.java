package com.project.oditji.recommend.vo;

import java.util.ArrayList;
import java.util.List;

import com.project.oditji.common.vo.PlatformBaseVO;
import com.project.oditji.search.vo.SearchResultVO;

/**
 * OTT별 추천 콘텐츠 묶음입니다.
 */
public class RecommendPlatformSectionVO extends PlatformBaseVO {

    private Long platformNo;
    private List<SearchResultVO> contentList;

    public RecommendPlatformSectionVO() {
        this.contentList = new ArrayList<SearchResultVO>();
    }

    public Long getPlatformNo() {
        return platformNo;
    }

    public void setPlatformNo(Long platformNo) {
        this.platformNo = platformNo;
    }

    public List<SearchResultVO> getContentList() {
        return contentList;
    }

    public void setContentList(List<SearchResultVO> contentList) {
        this.contentList = contentList == null
                ? new ArrayList<SearchResultVO>()
                : contentList;
    }
}
