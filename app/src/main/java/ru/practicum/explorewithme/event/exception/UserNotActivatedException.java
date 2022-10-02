package ru.practicum.explorewithme.event.exception;

import ru.practicum.explorewithme.exception.ConditionsNotMetException;

public class UserNotActivatedException extends ConditionsNotMetException {
    public UserNotActivatedException(String reason) {
        super("User not acrivated", reason);
    }
}
