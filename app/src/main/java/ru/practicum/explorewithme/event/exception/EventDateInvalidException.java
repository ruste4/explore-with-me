package ru.practicum.explorewithme.event.exception;

import ru.practicum.explorewithme.exception.ConditionsNotMetException;

public class EventDateInvalidException extends ConditionsNotMetException {
    public EventDateInvalidException(int banHoursBeforeEvent) {
        super(
                "Event date invalid",
                String.format(
                        "It is forbidden to update events date no earlier than %s hours before the event",
                        banHoursBeforeEvent
                )
        );
    }
}
