package ru.practicum.explorewithme.event.exception;

public class UserIsNotInitiatorException extends RuntimeException {
    public UserIsNotInitiatorException(String message) {
        super(message);
    }
}
