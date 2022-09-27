package ru.practicum.explorewithme.event.exception;

import ru.practicum.explorewithme.exception.AlreadyExistException;

public class EventAlreadyExistException extends AlreadyExistException {
    public EventAlreadyExistException(String reason) {
        super("Event already exist", reason);
    }
}
