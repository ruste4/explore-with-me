package ru.practicum.explorewithme.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.comment.CommentApiManager;
import ru.practicum.explorewithme.comment.dto.CommentCreateDto;
import ru.practicum.explorewithme.comment.dto.CommentFullDto;
import ru.practicum.explorewithme.comment.dto.CommentUpdateDto;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class CommentControllerForUser {

    private final CommentApiManager commentApiManager;

    @PostMapping("/{userId}/comments")
    public CommentFullDto addComment(
            @PathVariable long userId,
            @RequestBody @Valid CommentCreateDto createDto
    ) {
        return commentApiManager.addCommentByCurrentUser(userId, createDto);
    }

    @PutMapping("/{userId}/comments")
    public CommentFullDto updateComment(
            @PathVariable long userId,
            @RequestBody @Valid CommentUpdateDto updateDto
    ) {
        return commentApiManager.updateCommentByCurrentUser(userId, updateDto);
    }

    @DeleteMapping("/{userId}/comments/{commentId}")
    public void deleteUser(@PathVariable long userId, @PathVariable long commentId) {
        commentApiManager.deleteCommentByCurrentUser(userId, commentId);
    }
}
