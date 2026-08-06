package com.project.oditji.business.vo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * 이벤트 등록·수정 form의 공통 요청 값을 바인딩하는 객체입니다.
 */
public class EventFormVO {

    private String eventTitle;
    private String description;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    private List<Long> productNoList;
    private List<Integer> discountRateList;

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    public List<Long> getProductNoList() {
        return productNoList;
    }

    public void setProductNoList(List<Long> productNoList) {
        this.productNoList = productNoList;
    }

    public List<Integer> getDiscountRateList() {
        return discountRateList;
    }

    public void setDiscountRateList(List<Integer> discountRateList) {
        this.discountRateList = discountRateList;
    }
}
