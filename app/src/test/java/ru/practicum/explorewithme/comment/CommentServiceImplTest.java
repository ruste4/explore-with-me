package ru.practicum.explorewithme.comment;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.explorewithme.comment.exception.UserNotAuthorOfCommentException;
import ru.practicum.explorewithme.event.Event;
import ru.practicum.explorewithme.event.EventState;
import ru.practicum.explorewithme.event.Location;
import ru.practicum.explorewithme.event.category.Category;
import ru.practicum.explorewithme.user.User;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CommentServiceImplTest {

    @Autowired
    private final CommentService commentService;

    @Autowired
    private final TestEntityManager testEntityManager;

    private static AtomicLong userIdHolder;

    @BeforeAll
    public static void init() {
        userIdHolder = new AtomicLong();
    }

    @BeforeEach
    public void beforeEachCommentServiceTests() {
        testEntityManager.clear();
    }

    @Test
    public void addCommentSuccess() {
        Event event = generateAndPersistEvent();
        User commentator = generateAndPersistUser();
        Comment comment = Comment.builder()
                .event(event)
                .user(commentator)
                .text("Comment from addCommentSuccess test")
                .build();

        Comment addedComment = commentService.addComment(comment);

        Comment foundComment = testEntityManager.find(Comment.class, addedComment.getId());

        assertEquals(addedComment.getText(), foundComment.getText());
    }

    @Test
    public void updateCommentSuccess() {
        Event event = generateAndPersistEvent();
        User commentator = generateAndPersistUser();

        Comment comment = Comment.builder()
                .event(event)
                .user(commentator)
                .text("Comment from addCommentSuccess test")
                .build();

        Comment addedComment = commentService.addComment(comment);

        Comment updatedComment = Comment.builder()
                .id(addedComment.getId())
                .event(event)
                .user(commentator)
                .text("Updated text")
                .build();

        commentService.updateComment(updatedComment);

        Comment found = testEntityManager.find(Comment.class, addedComment.getId());

        assertEquals(updatedComment.getText(), found.getText());
    }

    @Test
    public void updateCommentFailUserNotAuthor() {
        Comment comment = generateAndPersistComment();
        User otherUser = generateAndPersistUser();

        Comment updatedComment = Comment.builder()
                .id(comment.getId())
                .event(comment.getEvent())
                .user(otherUser)
                .text("Updated text")
                .build();

        assertThrows(UserNotAuthorOfCommentException.class,
                () -> commentService.updateComment(updatedComment)
        );
    }

    @Test
    public void deleteCommentByCurrentUserSuccess() {
        Comment comment = generateAndPersistComment();

        commentService.deleteCommentByCurrentUser(comment.getUser().getId(), comment.getId());

        assertNull(testEntityManager.find(Comment.class, comment.getId()));
    }

    @Test
    public void getAllCommentsByEvent() {
        int commentQuantity = 5;
        Event event = generateAndPersistEvent();
        List<Comment> comments = generateAndPersistCommentsForEvent(commentQuantity, event);

        List<Comment> foundComments = commentService.getAllCommentsByEvent(event.getId(), 0, 100);

        assertEquals(comments, foundComments);
    }

    private User generateAndPersistUser() {
        String email = String.format("email-%s@mail.ru", userIdHolder.incrementAndGet());
        String name = String.format("user-%s", userIdHolder.get());

        User user = new User(null, email, name, true);

        return testEntityManager.persist(user);
    }

    private Event generateAndPersistEvent() {
        long stamp = System.nanoTime();
        String annotation = "annotation-" + stamp;
        Category category = generateAndPersistEventCategory("Category-" + stamp);
        LocalDateTime createOn = LocalDateTime.now();
        String description = "description-" + stamp;
        LocalDateTime eventDate = LocalDateTime.now().plusMonths(1);
        User initiator = generateAndPersistUser();
        LocalDateTime publishedOn = LocalDateTime.now().plusHours(1);
        String title = "title-" + stamp;
        Location location = new Location(12.3, 45.6);

        Event event = Event.builder()
                .annotation(annotation)
                .category(category)
                .createdOn(createOn)
                .description(description)
                .eventDate(eventDate)
                .initiator(initiator)
                .paid(false)
                .participantLimit(100L)
                .publishedOn(publishedOn)
                .requestModeration(false)
                .state(EventState.PUBLISHED)
                .title(title)
                .location(location)
                .build();

        return testEntityManager.persist(event);
    }

    private Category generateAndPersistEventCategory(String name) {
        Category category = new Category(null, name);
        return testEntityManager.persistAndFlush(category);
    }

    private Comment generateAndPersistComment() {
        Comment comment = Comment.builder()
                .user(generateAndPersistUser())
                .event(generateAndPersistEvent())
                .createdOn(LocalDateTime.now())
                .text("Comment " + System.nanoTime())
                .build();

        return testEntityManager.persist(comment);
    }

    private List<Comment> generateAndPersistCommentsForEvent(int commentQuantity, Event event) {
        List<Comment> res = new ArrayList<>();

        for (int i = 0; i < commentQuantity; i++) {
            Comment comment = Comment.builder()
                    .user(generateAndPersistUser())
                    .event(event)
                    .createdOn(LocalDateTime.now())
                    .text("Comment " + System.nanoTime())
                    .build();

            testEntityManager.persist(comment);
            res.add(comment);
        }

        return res;
    }

}