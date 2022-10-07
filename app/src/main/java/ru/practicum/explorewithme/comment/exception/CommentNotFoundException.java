package ru.practicum.explorewithme.comment.exception;

import ru.practicum.explorewithme.exception.NotFoundException;

public class CommentNotFoundException extends NotFoundException {
    public CommentNotFoundException(long commentId) {
        super("Comment not found", String.format("Comment with id:%s not found", commentId));
    }
}
