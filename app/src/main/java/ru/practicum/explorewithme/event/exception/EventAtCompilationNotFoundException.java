package ru.practicum.explorewithme.event.exception;

import ru.practicum.explorewithme.exception.NotFoundException;

public class EventAtCompilationNotFoundException extends NotFoundException {
    public EventAtCompilationNotFoundException(long eventId, long compilationId) {
        super(
                "Event at compilation not found",
                String.format(
                        "Event with id:%s not found to event list from compilation with id:%s",
                        eventId,
                        compilationId
                )
        );
    }
}
