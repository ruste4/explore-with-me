package ru.practicum.explorewithme.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.comment.dto.CommentCreateDto;
import ru.practicum.explorewithme.comment.dto.CommentFullDto;
import ru.practicum.explorewithme.comment.dto.CommentUpdateDto;
import ru.practicum.explorewithme.comment.exception.CommentNotFoundException;
import ru.practicum.explorewithme.comment.exception.UserNotAuthorOfCommentException;
import ru.practicum.explorewithme.user.User;
import ru.practicum.explorewithme.user.UserRepository;
import ru.practicum.explorewithme.user.exception.UserNotActivatedException;
import ru.practicum.explorewithme.user.exception.UserNotFoundException;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final UserRepository userRepository;

    private final CommentMapper commentMapper;

    @Override
    public CommentFullDto addCommentByCurrentUser(long userId, CommentCreateDto createDto) {
        log.info("Add comment from user with id:{} by event with id:{}", userId, createDto.getEvent());
        User user = findUserById(userId);

        if (!user.isActivated()) {
            throw new UserNotActivatedException(userId);
        }

        Comment comment = commentMapper.toComment(createDto);
        comment.setUser(user);
        Comment createdComment = commentRepository.save(comment);

        return commentMapper.toCommentFullDto(createdComment);
    }

    @Override
    public CommentFullDto updateCommentByCurrentUser(long userId, CommentUpdateDto updateDto) {

        User user = findUserById(userId);
        Comment comment = findCommentById(updateDto.getId());

        boolean isAuthorOfComment = comment.getUser().equals(user);
        if (!isAuthorOfComment) {
            throw new UserNotAuthorOfCommentException(userId, comment.getId());
        }

        if (!user.isActivated()) {
            throw new UserNotActivatedException(userId);
        }

        comment.setText(updateDto.getText());

        Comment updatedComment = commentRepository.save(comment);

        return commentMapper.toCommentFullDto(updatedComment);
    }

    @Override
    public void deleteCommentByCurrentUser(long userId, long commentId) {
        User user = findUserById(userId);
        Comment comment = findCommentById(commentId);
        boolean isAuthorComment = comment.getUser().equals(user);

        if (!isAuthorComment) {
            throw new UserNotAuthorOfCommentException(userId, commentId);
        }

        commentRepository.delete(comment);
    }

    @Override
    public List<Comment> getAllCommentsByEvent(long eventId, int from, int size) {
        PageRequest request = PageRequest.of(from, size);
        return commentRepository.findByEventId(eventId, request);
    }

    @Override
    public void deleteComment(long commentId) {
        commentRepository.deleteById(commentId);
    }

    private User findUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Comment findCommentById(long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));
    }
}
