package ru.practicum.explorewithme.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.event.EventService;
import ru.practicum.explorewithme.event.EventSort;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class EventController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventsWithFiltering(
            @RequestParam String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(required = false) Boolean onlyAvailable,
            @RequestParam(required = false) String sort,
            @PositiveOrZero @RequestParam(defaultValue = "0") int from,
            @Positive @RequestParam(defaultValue = "100") int size
    ) {
        LocalDateTime start = LocalDateTime.parse(rangeStart);
        LocalDateTime end = LocalDateTime.parse(rangeEnd);
        EventSort eventSort = EventSort.valueOf(sort);

        return eventService.getEvents(text, categories, paid, start, end, onlyAvailable, eventSort, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEvent(@PathVariable long eventId) {
        return eventService.getEventById(eventId);
    }
}
