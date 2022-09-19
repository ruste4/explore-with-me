package ru.practicum.explorewithme.event.exception;

public class EventAlreadyExistException extends RuntimeException {
    public EventAlreadyExistException(String message) {
        super(message);
    }
}
