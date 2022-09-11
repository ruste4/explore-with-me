package ru.practicum.explorewithme.request.exception;

public class RequestUnpublishedEventException extends RuntimeException {
    public RequestUnpublishedEventException(String message) {
        super(message);
    }
}
