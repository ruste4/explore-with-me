package ru.practicum.explorewithme.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.comment.exception.CommentNotFoundException;
import ru.practicum.explorewithme.comment.exception.UserNotAuthorOfCommentException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public Comment addComment(Comment comment) {
        log.debug("Add {}", comment);

        return commentRepository.save(comment);
    }

    public Comment updateComment(Comment updatedComment) {
        Comment commentFromDb = findCommentByIdOrThrow(updatedComment.getId());
        log.debug("Update {} at {}", commentFromDb, updatedComment);

        boolean isAuthorOfComment = Objects.equals(commentFromDb.getUser().getId(), updatedComment.getUser().getId());
        if (!isAuthorOfComment) {
            throw new UserNotAuthorOfCommentException(updatedComment.getUser().getId(), updatedComment.getId());
        }

        commentFromDb.setText(updatedComment.getText());

        return commentRepository.save(commentFromDb);
    }

    public void deleteCommentByCurrentUser(long userId, long commentId) {
        Comment comment = findCommentByIdOrThrow(commentId);
        log.debug("Delete {} by user with id: {}", comment, userId);

        boolean isAuthorComment = comment.getUser().getId() == userId;
        if (!isAuthorComment) {
            throw new UserNotAuthorOfCommentException(userId, commentId);
        }

        commentRepository.deleteById(commentId);
    }

    public List<Comment> getAllCommentsByEvent(long eventId, int pageFrom, int pageSize) {
        log.debug("Get comments by event with id: {}. PageFrom: {}, PageSize: {}", eventId, pageFrom, pageSize);
        PageRequest request = PageRequest.of(pageFrom, pageSize);
        return commentRepository.findByEventId(eventId, request);
    }

    public void deleteComment(long commentId) {
        log.debug("Delete comment with id: {}", commentId);
        commentRepository.deleteById(commentId);
    }

    private Comment findCommentByIdOrThrow(long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new CommentNotFoundException(commentId));
    }
}
