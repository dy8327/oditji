package com.project.oditji.event.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.event.dao.EventDAO;
import com.project.oditji.event.vo.EventVO;

@Service
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventDAO eventDAO;

    public EventServiceImpl(EventDAO eventDAO) {

        this.eventDAO = eventDAO;

    }

    /* 사용자 이벤트 목록 */
    @Override
    public List<EventVO> getEventList() {

        List<EventVO> eventList = eventDAO.selectEventList();

        if(eventList == null) {
            return List.of();
        }

        return eventList;
    }

    /* 이벤트 상세 */
    @Override
    public EventVO getEventDetail(Long eventNo) {

        if (eventNo == null || eventNo <= 0) {

            return null;

        }

        return eventDAO.selectEventDetail(eventNo);

    }

}