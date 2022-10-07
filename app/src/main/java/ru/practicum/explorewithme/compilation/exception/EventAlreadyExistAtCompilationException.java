package ru.practicum.explorewithme.compilation.exception;

import ru.practicum.explorewithme.exception.AlreadyExistException;

public class EventAlreadyExistAtCompilationException extends AlreadyExistException {
    public EventAlreadyExistAtCompilationException(long eventId, long compilationId) {
        super(
                "Event already exist at compilation",
                String.format("Event with id:%s already exist to compilation with id:%s", eventId, compilationId)
        );
    }
}
