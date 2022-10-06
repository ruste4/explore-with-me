package ru.practicum.explorewithme.request.exception;

import ru.practicum.explorewithme.exception.ConflictException;

public class UserNotRequesterForEventRequestException extends ConflictException {
    public UserNotRequesterForEventRequestException(long userId, long requestId) {
        super(
                "User not requester for request",
                String.format("User with id:%s does not requester for request with id:%s", userId, requestId)
        );
    }
}
