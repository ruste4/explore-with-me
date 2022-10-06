package ru.practicum.explorewithme.user.exception;

import ru.practicum.explorewithme.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(long userId) {
        super("User not found", String.format("User with id:%s not found", userId));
    }

}
