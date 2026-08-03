package com.project.oditji.event.vo;

import java.util.Date;
import java.util.List;

import com.project.oditji.common.vo.EventBaseVO;

/**
 * 사용자 이벤트 목록 및 상세 조회 VO입니다.
 */
public class EventVO extends EventBaseVO {

    private Date startDate;
    private Date endDate;
    private List<EventProductVO> products;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<EventProductVO> getProducts() {
        return products;
    }

    public void setProducts(List<EventProductVO> products) {
        this.products = products;
    }
}
