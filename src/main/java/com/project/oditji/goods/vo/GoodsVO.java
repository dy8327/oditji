package com.project.oditji.goods.vo;

import com.project.oditji.common.vo.ProductSaleInfoVO;

/**
 * 상품 검색, 추천, 상세, 찜 목록에서 사용하는 상품 VO입니다.
 */
public class GoodsVO extends ProductSaleInfoVO {

    private static final long serialVersionUID = 1L;

    private Integer businessNo;
    private Integer contentNo;
    private Integer actorNo;
    private String description;

    public Integer getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(Integer businessNo) {
        this.businessNo = businessNo;
    }

    public Integer getContentNo() {
        return contentNo;
    }

    public void setContentNo(Integer contentNo) {
        this.contentNo = contentNo;
    }

    public Integer getActorNo() {
        return actorNo;
    }

    public void setActorNo(Integer actorNo) {
        this.actorNo = actorNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
