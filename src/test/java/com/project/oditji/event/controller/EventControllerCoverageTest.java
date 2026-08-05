package com.project.oditji.event.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.server.ResponseStatusException;

import com.project.oditji.event.service.EventService;
import com.project.oditji.event.vo.EventVO;

/** 이벤트 기간 필터 문구와 상세 페이지의 실제 진행 상태 계산을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class EventControllerCoverageTest {

    @Mock
    private EventService eventService;

    private EventController controller;

    @BeforeEach
    void setUp() {
        controller = new EventController(eventService);
    }

    @Test
    void listShouldExposeUpcomingLabels() {
        List<EventVO> events = List.of(new EventVO());
        when(eventService.getEventList("upcoming")).thenReturn(events);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.eventList(" UPCOMING ", model);

        assertEquals("event/eventList", view);
        assertEquals(events, model.get("eventList"));
        assertEquals("upcoming", model.get("period"));
        assertEquals("예정된 할인 이벤트", model.get("periodTitle"));
        assertEquals("곧 시작될 상품 할인 이벤트입니다.", model.get("periodDescription"));
        assertEquals("예정", model.get("periodBadge"));
        assertEquals("예정된 이벤트가 없습니다.", model.get("emptyMessage"));
    }

    @Test
    void listShouldExposeEndedLabels() {
        when(eventService.getEventList("ended")).thenReturn(List.of());
        ExtendedModelMap model = new ExtendedModelMap();

        controller.eventList("ended", model);

        assertEquals("ended", model.get("period"));
        assertEquals("종료된 할인 이벤트", model.get("periodTitle"));
        assertEquals("판매 기간이 종료된 상품 할인 이벤트입니다.", model.get("periodDescription"));
        assertEquals("종료", model.get("periodBadge"));
        assertEquals("종료된 이벤트가 없습니다.", model.get("emptyMessage"));
    }

    @Test
    void listShouldNormalizeNullAndUnsupportedPeriodToOngoing() {
        when(eventService.getEventList("ongoing")).thenReturn(List.of());
        ExtendedModelMap nullModel = new ExtendedModelMap();
        ExtendedModelMap invalidModel = new ExtendedModelMap();

        controller.eventList(null, nullModel);
        controller.eventList("unknown", invalidModel);

        assertEquals("ongoing", nullModel.get("period"));
        assertEquals("진행 중인 할인 이벤트", nullModel.get("periodTitle"));
        assertEquals("현재 참여할 수 있는 상품 할인 이벤트입니다.", nullModel.get("periodDescription"));
        assertEquals("진행 중", invalidModel.get("periodBadge"));
        assertEquals("진행 중인 이벤트가 없습니다.", invalidModel.get("emptyMessage"));
    }

    @Test
    void detailShouldReturnNotFoundForMissingEvent() {
        when(eventService.getEventDetail(10L)).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.eventDetail(10L, new ExtendedModelMap()));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void detailShouldPreferExplicitEndStatus() {
        EventVO event = event("END", LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        when(eventService.getEventDetail(11L)).thenReturn(event);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.eventDetail(11L, model);

        assertEquals("event/eventDetail", view);
        assertEquals(event, model.get("event"));
        assertEquals("ended", model.get("period"));
        assertEquals("종료", model.get("periodBadge"));
    }

    @Test
    void detailShouldResolvePastFutureAndOngoingDates() {
        EventVO ended = event("APPROVED", LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));
        EventVO upcoming = event("APPROVED", LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        EventVO ongoing = event("APPROVED", null, null);
        when(eventService.getEventDetail(20L)).thenReturn(ended);
        when(eventService.getEventDetail(21L)).thenReturn(upcoming);
        when(eventService.getEventDetail(22L)).thenReturn(ongoing);

        ExtendedModelMap endedModel = new ExtendedModelMap();
        ExtendedModelMap upcomingModel = new ExtendedModelMap();
        ExtendedModelMap ongoingModel = new ExtendedModelMap();
        controller.eventDetail(20L, endedModel);
        controller.eventDetail(21L, upcomingModel);
        controller.eventDetail(22L, ongoingModel);

        assertEquals("ended", endedModel.get("period"));
        assertEquals("upcoming", upcomingModel.get("period"));
        assertEquals("ongoing", ongoingModel.get("period"));
    }

    private EventVO event(String status, LocalDate startDate, LocalDate endDate) {
        EventVO event = new EventVO();
        event.setStatus(status);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        return event;
    }

}
