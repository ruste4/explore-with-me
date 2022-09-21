package ru.practicum.explorewithme.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.event.EventService;
import ru.practicum.explorewithme.event.dto.EventCreateDto;
import ru.practicum.explorewithme.event.dto.EventFullDto;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class EventControllerForUser {

    private final EventService eventService;

    @PostMapping("/{userId}/events")
    public EventFullDto addEvent(@PathVariable long userId, @RequestBody @Valid EventCreateDto createDto) {
        return eventService.addEvent(userId, createDto);
    }

}
