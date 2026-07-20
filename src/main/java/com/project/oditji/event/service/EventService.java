package com.project.oditji.event.service;

import java.util.List;

import com.project.oditji.event.vo.EventVO;

public interface EventService {

    /* 진행중 이벤트 목록 */
    List<EventVO> getEventList();

    /* 이벤트 상세 */
    EventVO getEventDetail(Long eventNo);
}