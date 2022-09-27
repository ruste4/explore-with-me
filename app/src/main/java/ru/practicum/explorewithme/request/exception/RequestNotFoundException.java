package ru.practicum.explorewithme.request.exception;

import ru.practicum.explorewithme.exception.NotFoundException;

public class RequestNotFoundException extends NotFoundException {
    public RequestNotFoundException(String reason) {
        super("Request not found", reason);
    }
}
