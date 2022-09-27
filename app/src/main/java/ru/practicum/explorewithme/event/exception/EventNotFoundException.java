package ru.practicum.explorewithme.event.exception;

import ru.practicum.explorewithme.exception.NotFoundException;

public class EventNotFoundException extends NotFoundException {
    public EventNotFoundException(String reason) {
        super("Event not found", reason);
    }
}
