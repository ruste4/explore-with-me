package ru.practicum.explorewithme.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.comment.CommentApiManager;

@RestController
@RequestMapping(path = "/admin/comments")
@RequiredArgsConstructor
public class CommentControllerForAdmin {

    private final CommentApiManager commentApiManager;

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable long commentId) {
        commentApiManager.deleteComment(commentId);
    }
}
