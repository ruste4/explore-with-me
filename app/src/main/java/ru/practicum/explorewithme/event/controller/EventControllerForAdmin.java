package ru.practicum.explorewithme.event.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.event.EventService;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.requestparams.SearchEventParams;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class EventControllerForAdmin {

    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> searchEvents(@RequestParam Map<String, String> dataQuery, HttpServletRequest request) {
        log.info("client ip: {}", request.getRemoteAddr());
        log.info("endpoint path: {}", request.getRequestURI());
        log.info("data query: {}", dataQuery);
        return null;
    }
}
