package com.project.oditji.event.dao;

import java.util.List;

import com.project.oditji.event.vo.EventVO;

public interface EventDAO {

    /** 승인된 이벤트를 날짜 구분에 따라 조회합니다. */
    List<EventVO> selectEventList(String period);

    /** 이벤트 상세를 조회합니다. */
    EventVO selectEventDetail(Long eventNo);
}
