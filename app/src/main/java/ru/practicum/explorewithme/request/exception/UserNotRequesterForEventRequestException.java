package ru.practicum.explorewithme.request.exception;

public class UserNotRequesterForEventRequestException extends RuntimeException {
    public UserNotRequesterForEventRequestException(String message) {
        super(message);
    }
}
