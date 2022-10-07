package ru.practicum.explorewithme.request.exception;

import ru.practicum.explorewithme.exception.ConditionsNotMetException;

public class RequestUnpublishedEventException extends ConditionsNotMetException {
    public RequestUnpublishedEventException() {
        super("Request unpublished event forbidden", "Request to an unpublished event is prohibited");
    }
}
