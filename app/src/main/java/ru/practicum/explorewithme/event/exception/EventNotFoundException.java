package ru.practicum.explorewithme.event.exception;

import ru.practicum.explorewithme.exception.NotFoundException;

public class EventNotFoundException extends NotFoundException {
    public EventNotFoundException(long eventId) {
        super("Event not found", String.format("Event with id:%s not found", eventId));
    }
}
