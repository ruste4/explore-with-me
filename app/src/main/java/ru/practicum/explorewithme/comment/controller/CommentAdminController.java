package ru.practicum.explorewithme.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.comment.CommentApiManager;

@RestController
@RequestMapping(path = "/admin/comments/{commentId}")
@RequiredArgsConstructor
public class CommentAdminController {

    private final CommentApiManager commentApiManager;

    @DeleteMapping
    public void deleteComment(@PathVariable long commentId) {
        commentApiManager.deleteComment(commentId);
    }
}
