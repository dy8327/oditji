package com.project.oditji.event.vo;

import java.time.LocalDate;
import java.util.List;

import com.project.oditji.common.vo.EventBaseVO;

/**
 * 사용자 이벤트 목록 및 상세 조회 VO입니다.
 */
public class EventVO extends EventBaseVO {

    private LocalDate startDate;
    private LocalDate endDate;
    private List<EventProductVO> products;

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<EventProductVO> getProducts() {
        return products;
    }

    public void setProducts(List<EventProductVO> products) {
        this.products = products;
    }
}
