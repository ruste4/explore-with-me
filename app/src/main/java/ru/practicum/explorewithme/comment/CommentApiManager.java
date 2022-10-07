package ru.practicum.explorewithme.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.comment.dto.CommentCreateDto;
import ru.practicum.explorewithme.comment.dto.CommentFullDto;
import ru.practicum.explorewithme.comment.dto.CommentUpdateDto;
import ru.practicum.explorewithme.event.Event;
import ru.practicum.explorewithme.event.EventRepository;
import ru.practicum.explorewithme.event.exception.EventNotFoundException;
import ru.practicum.explorewithme.user.User;
import ru.practicum.explorewithme.user.UserRepository;
import ru.practicum.explorewithme.user.exception.UserNotActivatedException;
import ru.practicum.explorewithme.user.exception.UserNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CommentApiManager {

    private final CommentService commentService;

    private final EventRepository eventRepository;

    private final UserRepository userRepository;

    public CommentFullDto addCommentByCurrentUser(long userId, CommentCreateDto createDto) {
        User user = findUserByIdOrThrow(userId);
        Event event = findEventByIdThrow(createDto.getEventId());

        Comment comment = CommentMapper.toComment(createDto);
        comment.setUser(user);
        comment.setEvent(event);

        return CommentMapper.toCommentFullDto(commentService.addComment(comment));
    }

    public CommentFullDto updateCommentByCurrentUser(long userId, CommentUpdateDto updateDto) {
        User user = findUserByIdOrThrow(userId);

        Comment updatedComment = CommentMapper.toComment(updateDto);
        updatedComment.setUser(user);

        return CommentMapper.toCommentFullDto(commentService.updateComment(updatedComment));
    }

    public List<CommentFullDto> getAllCommentsByEvent(long eventId, int pageFrom, int pageSize) {
        return commentService.getAllCommentsByEvent(eventId, pageFrom, pageSize).stream()
                .map(CommentMapper::toCommentFullDto)
                .collect(Collectors.toList());
    }

    public void deleteCommentByCurrentUser(long userId, long commentId) {
        commentService.deleteCommentByCurrentUser(userId, commentId);
    }

    public void deleteComment(long comment) {
        commentService.deleteComment(comment);
    }


    private User findUserByIdOrThrow(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        if (!user.isActivated()) {
            throw new UserNotActivatedException(userId);
        }

        return user;
    }

    private Event findEventByIdThrow(long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
    }
}
