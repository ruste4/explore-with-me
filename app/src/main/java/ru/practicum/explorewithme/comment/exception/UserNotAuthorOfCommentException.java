package ru.practicum.explorewithme.comment.exception;

import ru.practicum.explorewithme.exception.ConditionsNotMetException;

public class UserNotAuthorOfCommentException extends ConditionsNotMetException {
    public UserNotAuthorOfCommentException(String reason) {
        super("User not author of comment", reason);
    }
}
