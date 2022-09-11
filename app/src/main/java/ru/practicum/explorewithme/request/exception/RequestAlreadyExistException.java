package ru.practicum.explorewithme.request.exception;

public class RequestAlreadyExistException extends RuntimeException {
    public RequestAlreadyExistException(String message) {
        super(message);
    }
}
