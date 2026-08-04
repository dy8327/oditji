package com.project.oditji.order.vo;

import com.project.oditji.common.vo.DeliveryBaseVO;

/**
 * 사용자(구매자)의 배송 조회 화면에서 사용하는 VO입니다.
 *
 * 공통 배송·주문상품 필드는 DeliveryBaseVO에서 상속받고,
 * 구매자 화면에 필요한 상품 대표 이미지만 추가로 보관합니다.
 */
public class DeliveryVO extends DeliveryBaseVO {

    private String mainImage;

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }
}
