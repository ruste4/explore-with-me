package ru.practicum.explorewithme.event.exception;

public class EventUpdatingIsProhibitedException extends RuntimeException {
    public EventUpdatingIsProhibitedException(String message) {
        super(message);
    }
}
