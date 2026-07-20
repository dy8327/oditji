package com.project.oditji.event.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import com.project.oditji.event.service.EventService;
import com.project.oditji.event.vo.EventVO;

import org.springframework.http.HttpStatus;

@Controller
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {

        this.eventService = eventService;

    }

    /* 이벤트 목록 */
    @GetMapping("/event/list")
    public String eventList(Model model) {

        model.addAttribute(
                "eventList",
                eventService.getEventList());

        return "event/eventList";

    }

    /* 이벤트 상세 */
    @GetMapping("/event/detail/{eventNo}")
    public String eventDetail(
            @PathVariable Long eventNo,
            Model model) {

        EventVO event = eventService.getEventDetail(eventNo);

        if (event == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "존재하지 않는 이벤트입니다.");

        }

        model.addAttribute(
                "event",
                event);

        return "event/eventDetail";

    }

}