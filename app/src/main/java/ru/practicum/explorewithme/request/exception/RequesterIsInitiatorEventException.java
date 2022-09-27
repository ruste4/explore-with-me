package ru.practicum.explorewithme.request.exception;

import ru.practicum.explorewithme.exception.ConflictException;

public class RequesterIsInitiatorEventException extends ConflictException {
    public RequesterIsInitiatorEventException(String reason) {
        super("Requester is initiator for event", reason);
    }
}
