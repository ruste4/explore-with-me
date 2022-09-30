package ru.practicum.explorewithme.comment;

import ru.practicum.explorewithme.comment.dto.CommentCreateDto;
import ru.practicum.explorewithme.comment.dto.CommentFullDto;
import ru.practicum.explorewithme.comment.dto.CommentUpdateDto;

import java.util.List;

public interface CommentService {

    /**
     * Добавить комментарий от текущего пользователя
     */
    CommentFullDto addCommentByCurrentUser(long userId, CommentCreateDto createDto);

    /**
     * Обновить комментарий текущего пользователя
     */
    CommentFullDto updateCommentByCurrentUser(long userId, CommentUpdateDto updateDto);

    /**
     * Удалить комментарий текущего пользователя
     */
    void deleteCommentByCurrentUser(long userId, long commentId);

    /**
     * Получить все комментарий по событию
     */
    List<Comment> getAllCommentsByEvent(long eventId, int from, int size);

    /**
     * Удалить комментарий
     */
    void deleteComment(long commentId);

}
