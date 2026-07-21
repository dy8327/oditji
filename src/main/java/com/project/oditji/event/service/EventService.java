package com.project.oditji.event.service;

import java.util.List;

import com.project.oditji.event.vo.EventVO;

public interface EventService {

    /** 날짜 구분에 따른 사용자 이벤트 목록을 조회합니다. */
    List<EventVO> getEventList(String period);

    /** 기존 호출부에서는 진행 중 이벤트를 기본값으로 사용합니다. */
    default List<EventVO> getEventList() {
        return getEventList("ongoing");
    }

    /** 이벤트 상세를 조회합니다. */
    EventVO getEventDetail(Long eventNo);
}
