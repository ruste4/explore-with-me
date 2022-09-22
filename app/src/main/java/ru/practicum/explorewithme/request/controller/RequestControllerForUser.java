package ru.practicum.explorewithme.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.request.RequestService;
import ru.practicum.explorewithme.request.dto.RequestFullDto;

import java.util.List;

@RestController
@RequestMapping(path = "users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class RequestControllerForUser {

    private final RequestService requestService;

    @GetMapping("/{userId}/requests")
    public List<RequestFullDto> getRequestFromCurrentUserToEventOtherUsers(@PathVariable long userId) {
        return  requestService.getEventRequestsFromCurrentUser(userId);
    }

    @PostMapping("/{userId}/requests")
    public RequestFullDto addRequestFromCurrentUser(@PathVariable long userId, @RequestParam long eventId) {
        return requestService.addEventRequest(userId, eventId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public RequestFullDto cancelRequestCurrentUser(@PathVariable long userId, @PathVariable long requestId) {
        return requestService.cancelEventRequestCurrentUser(userId, requestId);
    }
}
