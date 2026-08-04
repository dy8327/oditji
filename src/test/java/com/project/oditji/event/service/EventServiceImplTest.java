package com.project.oditji.event.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.event.dao.EventDAO;
import com.project.oditji.event.vo.EventVO;

/** 승인 이벤트의 기간 정규화와 상세 조회 입력값을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventDAO eventDAO;

    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(eventDAO);
    }

    @Test
    void eventListShouldNormalizePeriodAndReturnDaoList() {
        List<EventVO> list = List.of(new EventVO());
        when(eventDAO.selectEventList("ongoing")).thenReturn(list);
        when(eventDAO.selectEventList("upcoming")).thenReturn(list);
        when(eventDAO.selectEventList("ended")).thenReturn(list);

        assertSame(list, eventService.getEventList(null));
        assertSame(list, eventService.getEventList("invalid"));
        assertSame(list, eventService.getEventList(" UPCOMING "));
        assertSame(list, eventService.getEventList("ENDED"));

        verify(eventDAO, org.mockito.Mockito.times(2))
                .selectEventList("ongoing");
    }

    @Test
    void eventListShouldReturnEmptyListWhenDaoReturnsNull() {
        when(eventDAO.selectEventList("ongoing")).thenReturn(null);
        assertTrue(eventService.getEventList("ongoing").isEmpty());
    }

    @Test
    void eventDetailShouldValidateNumberAndDelegate() {
        EventVO event = new EventVO();
        when(eventDAO.selectEventDetail(1L)).thenReturn(event);

        assertSame(event, eventService.getEventDetail(1L));
        assertNull(eventService.getEventDetail(null));
        assertNull(eventService.getEventDetail(0L));
        assertNull(eventService.getEventDetail(-1L));

        verify(eventDAO, never()).selectEventDetail(0L);
    }
}
