package ru.practicum.explorewithme.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.event.EventService;
import ru.practicum.explorewithme.event.dto.EventCreateDto;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.EventUpdateDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

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

    @PatchMapping("/{userId}/events")
    public EventFullDto updateEvent(@PathVariable long userId, @RequestBody @Valid EventUpdateDto updateDto) {
        return eventService.updateEventByInitiatorId(userId, updateDto);
    }

    @GetMapping("/{userId}/events")
    public List<EventShortDto> getAllEventByInitiator(
            @PathVariable long userId,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size
    ) {
        return eventService.getEventsByInitiatorId(userId, from, size);
    }
}
