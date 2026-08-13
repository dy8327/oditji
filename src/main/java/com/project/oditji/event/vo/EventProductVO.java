package com.project.oditji.event.vo;

import com.project.oditji.common.vo.EventProductBaseVO;

/**
 * 사용자 이벤트 상세 화면에 표시할 연결 상품입니다.
 */
public class EventProductVO extends EventProductBaseVO {

    private Integer eventDiscountRate;
    private String imagePath;

    public Integer getEventDiscountRate() {
        return eventDiscountRate;
    }

    public void setEventDiscountRate(Integer eventDiscountRate) {
        this.eventDiscountRate = eventDiscountRate;
    }

    public Long getDiscountPrice() {
        Long price = getPrice();

        if (price == null || eventDiscountRate == null) {
            return null;
        }

        return Math.round(price * (100 - eventDiscountRate) / 100.0);
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
