package ru.practicum.explorewithme.request.exception;

import ru.practicum.explorewithme.exception.ConditionsNotMetException;

public class RequestUnpublishedEventException extends ConditionsNotMetException {
    public RequestUnpublishedEventException(String reason) {
        super("Request unpublished event forbidden", reason);
    }
}
