package com.project.oditji.event.dao;

import java.util.List;

import com.project.oditji.event.vo.EventVO;

public interface EventDAO {

    /* 사용자 이벤트 목록 조회 */
    List<EventVO> selectEventList();

    /* 이벤트 상세 조회 */
    EventVO selectEventDetail(Long eventNo);
}