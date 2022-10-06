package ru.practicum.explorewithme.request.exception;

import ru.practicum.explorewithme.exception.AlreadyExistException;

public class RequestAlreadyExistException extends AlreadyExistException {
    public RequestAlreadyExistException(long eventId, long userId) {
        super(
                "Request already exist",
                String.format("Request for event with id:%s on user with id:%s already exist", eventId, userId)
        );
    }
}
