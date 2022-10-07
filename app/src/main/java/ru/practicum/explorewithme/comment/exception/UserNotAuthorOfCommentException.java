package ru.practicum.explorewithme.comment.exception;

import ru.practicum.explorewithme.exception.ConditionsNotMetException;

public class UserNotAuthorOfCommentException extends ConditionsNotMetException {
    public UserNotAuthorOfCommentException(long userId, long commentId) {
        super(
                "User not author of comment",
                String.format("User with id%s not author of comment with id:%s", userId, commentId)
        );
    }
}
