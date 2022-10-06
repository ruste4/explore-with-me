package ru.practicum.explorewithme.event.exception;

import ru.practicum.explorewithme.exception.ConflictException;

public class UserIsNotInitiatorOfEventException extends ConflictException {
    public UserIsNotInitiatorOfEventException(long userId, long eventId) {
        super(
                "User is not initiator",
                String.format("User with id:%s is not the initiator of the event with id:%s", userId, eventId)
        );
    }
}
