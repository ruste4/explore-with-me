package ru.practicum.explorewithme.request.exception;

import ru.practicum.explorewithme.exception.ConflictException;

public class RequesterIsInitiatorEventException extends ConflictException {
    public RequesterIsInitiatorEventException(long requesterId, long eventId) {
        super(
                "Requester is initiator for event",
                String.format("Requester with id:%s cannot be initiator of event with id:%s", requesterId, eventId)
        );
    }
}
