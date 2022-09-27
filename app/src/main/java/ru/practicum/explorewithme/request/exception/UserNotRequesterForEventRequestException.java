package ru.practicum.explorewithme.request.exception;

import ru.practicum.explorewithme.exception.ConflictException;

public class UserNotRequesterForEventRequestException extends ConflictException {
    public UserNotRequesterForEventRequestException(String reason) {
        super("User not requester for request", reason);
    }
}
