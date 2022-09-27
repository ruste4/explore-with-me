package ru.practicum.explorewithme.request.exception;

import ru.practicum.explorewithme.exception.AlreadyExistException;

public class RequestAlreadyExistException extends AlreadyExistException {
    public RequestAlreadyExistException(String reason) {
        super("Request already exist", reason);
    }
}
