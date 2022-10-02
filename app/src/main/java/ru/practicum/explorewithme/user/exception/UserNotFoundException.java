package ru.practicum.explorewithme.user.exception;

import ru.practicum.explorewithme.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String reason) {
        super("User not found", reason);
    }

}
