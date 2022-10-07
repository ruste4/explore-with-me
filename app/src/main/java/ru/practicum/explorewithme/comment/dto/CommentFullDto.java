package ru.practicum.explorewithme.comment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentFullDto {

    private Long id;

    private Long userId;

    private Long eventId;

    private LocalDateTime createdOn;

    private String text;

}
