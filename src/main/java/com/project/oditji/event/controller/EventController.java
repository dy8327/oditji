package com.project.oditji.event.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.project.oditji.event.service.EventService;
import com.project.oditji.event.vo.EventVO;

@Controller
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * 날짜 기준으로 진행 중, 예정, 종료 이벤트를 나누어 조회합니다.
     * DB의 STATUS는 관리자 승인 상태로 사용하고,
     * period는 시작일과 종료일을 기준으로 계산합니다.
     */
    @GetMapping("/event/list")
    public String eventList(
            @RequestParam(defaultValue = "ongoing") String period,
            Model model) {

        String normalizedPeriod = normalizePeriod(period);

        model.addAttribute("eventList", eventService.getEventList(normalizedPeriod));
        model.addAttribute("period", normalizedPeriod);
        model.addAttribute("periodTitle", createPeriodTitle(normalizedPeriod));
        model.addAttribute("periodDescription", createPeriodDescription(normalizedPeriod));
        model.addAttribute("periodBadge", createPeriodBadge(normalizedPeriod));
        model.addAttribute("emptyMessage", createEmptyMessage(normalizedPeriod));

        return "event/eventList";
    }

    @GetMapping("/event/detail/{eventNo}")
    public String eventDetail(
            @PathVariable Long eventNo,
            Model model) {

        EventVO event = eventService.getEventDetail(eventNo);

        if (event == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "존재하지 않는 이벤트입니다."
            );
        }

        /*
         * [수정] 상세 페이지 뱃지는 EVENT.STATUS(관리자 승인 상태)만 보고
         * "진행 중"으로 고정 표시되던 문제가 있었다.
         * 목록 조회(selectEventList)와 동일하게 시작일/종료일을 기준으로
         * 실제 진행 상태(예정/진행 중/종료)를 계산해서 뱃지에 반영한다.
         */
        String actualPeriod = resolveActualPeriod(event);

        model.addAttribute("event", event);
        model.addAttribute("period", actualPeriod);
        model.addAttribute("periodBadge", createPeriodBadge(actualPeriod));

        return "event/eventDetail";
    }

    /**
     * EVENT.STATUS가 'END'이거나 종료일이 지났으면 종료,
     * 시작일이 아직 오지 않았으면 예정, 그 외는 진행 중으로 판단한다.
     *
     * selectEventList의 TRUNC(START_DATE) / TRUNC(END_DATE) 비교와
     * 동일한 기준(날짜 단위 비교)을 애플리케이션 레벨에서 맞춘다.
     */
    private String resolveActualPeriod(EventVO event) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = toLocalDate(event.getStartDate());
        LocalDate endDate = toLocalDate(event.getEndDate());

        if ("END".equals(event.getStatus())) {
            return "ended";
        }

        if (endDate != null && endDate.isBefore(today)) {
            return "ended";
        }

        if (startDate != null && startDate.isAfter(today)) {
            return "upcoming";
        }

        return "ongoing";
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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

    private String createPeriodTitle(String period) {
        if ("upcoming".equals(period)) return "예정된 할인 이벤트";
        if ("ended".equals(period)) return "종료된 할인 이벤트";
        return "진행 중인 할인 이벤트";
    }

    private String createPeriodDescription(String period) {
        if ("upcoming".equals(period)) return "곧 시작될 상품 할인 이벤트입니다.";
        if ("ended".equals(period)) return "판매 기간이 종료된 상품 할인 이벤트입니다.";
        return "현재 참여할 수 있는 상품 할인 이벤트입니다.";
    }

    private String createPeriodBadge(String period) {
        if ("upcoming".equals(period)) return "예정";
        if ("ended".equals(period)) return "종료";
        return "진행 중";
    }

    private String createEmptyMessage(String period) {
        if ("upcoming".equals(period)) return "예정된 이벤트가 없습니다.";
        if ("ended".equals(period)) return "종료된 이벤트가 없습니다.";
        return "진행 중인 이벤트가 없습니다.";
    }
}
