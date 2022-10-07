package ru.practicum.explorewithme.request.exception;

import ru.practicum.explorewithme.exception.NotFoundException;

public class RequestNotFoundException extends NotFoundException {
    public RequestNotFoundException(long requestId) {
        super(
                "Request not found",
                String.format("Request with id:%s not found", requestId)
        );
    }
}
