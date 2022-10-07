package ru.practicum.explorewithme.comment;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.comment.dto.CommentCreateDto;
import ru.practicum.explorewithme.comment.dto.CommentFullDto;
import ru.practicum.explorewithme.comment.dto.CommentUpdateDto;

@Component
@AllArgsConstructor
public class CommentMapper {

    public static Comment toComment(CommentCreateDto createDto) {
        return Comment.builder().text(createDto.getText()).build();
    }

    public static Comment toComment(CommentUpdateDto updateDto) {
        return Comment.builder()
                .id(updateDto.getId())
                .text(updateDto.getText())
                .build();
    }

    public static CommentFullDto toCommentFullDto(Comment comment) {
        return CommentFullDto.builder()
                .id(comment.getId())
                .userId(comment.getUser().getId())
                .eventId(comment.getEvent().getId())
                .createdOn(comment.getCreatedOn())
                .text(comment.getText())
                .build();
    }

}
