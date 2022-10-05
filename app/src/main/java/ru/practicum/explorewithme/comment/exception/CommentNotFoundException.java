package ru.practicum.explorewithme.comment.exception;

import ru.practicum.explorewithme.exception.NotFoundException;

public class CommentNotFoundException extends NotFoundException {
    public CommentNotFoundException(String reason) {
        super("Comment not found", reason);
    }
}
