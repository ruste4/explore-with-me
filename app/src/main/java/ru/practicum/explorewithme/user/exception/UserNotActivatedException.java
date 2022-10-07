package ru.practicum.explorewithme.user.exception;

import ru.practicum.explorewithme.exception.ConditionsNotMetException;

public class UserNotActivatedException extends ConditionsNotMetException {
    public UserNotActivatedException(long userId) {
        super("User not activated", String.format("User with id:%s not activated", userId));
    }
}
