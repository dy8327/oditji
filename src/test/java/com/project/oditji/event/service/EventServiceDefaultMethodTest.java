package com.project.oditji.event.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.project.oditji.event.vo.EventVO;

/** 이벤트 서비스의 진행 중 목록 기본 호출을 검증합니다. */
class EventServiceDefaultMethodTest {

    @Test
    void defaultListShouldDelegateWithOngoingPeriod() {
        EventService service = mock(EventService.class, CALLS_REAL_METHODS);
        List<EventVO> expected = List.of(new EventVO());
        when(service.getEventList("ongoing")).thenReturn(expected);

        List<EventVO> result = service.getEventList();

        assertSame(expected, result);
        verify(service).getEventList("ongoing");
    }
}
