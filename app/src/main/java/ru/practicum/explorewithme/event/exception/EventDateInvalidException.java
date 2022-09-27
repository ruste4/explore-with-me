package ru.practicum.explorewithme.event.exception;

import ru.practicum.explorewithme.exception.ConditionsNotMetException;

public class EventDateInvalidException extends ConditionsNotMetException {
    public EventDateInvalidException(String reason) {
        super("Event date invalid", reason);
    }
}
