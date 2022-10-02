package ru.practicum.explorewithme.event.exception;

import ru.practicum.explorewithme.exception.ConditionsNotMetException;

public class EventUpdatingIsProhibitedException extends ConditionsNotMetException {
    public EventUpdatingIsProhibitedException(String reason) {
        super("Event updating is prohibited", reason);
    }
}
