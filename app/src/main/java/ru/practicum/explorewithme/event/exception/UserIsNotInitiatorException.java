package ru.practicum.explorewithme.event.exception;

import ru.practicum.explorewithme.exception.ConflictException;

public class UserIsNotInitiatorException extends ConflictException {
    public UserIsNotInitiatorException(String reason) {
        super("User is not initiator", reason);
    }
}
