package com.project.oditji.event.service;

import java.util.List;
import java.util.Locale;

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

    /** 승인된 이벤트를 날짜 구분에 따라 조회합니다. */
    @Override
    public List<EventVO> getEventList(String period) {
        String normalizedPeriod = normalizePeriod(period);
        List<EventVO> eventList = eventDAO.selectEventList(normalizedPeriod);
        return eventList == null ? List.of() : eventList;
    }

    @Override
    public EventVO getEventDetail(Long eventNo) {
        if (eventNo == null || eventNo <= 0) return null;
        return eventDAO.selectEventDetail(eventNo);
    }

    private String normalizePeriod(String period) {
        String normalized = period == null
                ? "ongoing"
                : period.trim().toLowerCase(Locale.ROOT);

        if ("upcoming".equals(normalized) || "ended".equals(normalized)) {
            return normalized;
        }

        return "ongoing";
    }
}
