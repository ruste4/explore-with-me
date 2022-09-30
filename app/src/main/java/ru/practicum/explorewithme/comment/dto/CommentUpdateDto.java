package ru.practicum.explorewithme.comment.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class CommentUpdateDto {

    @NonNull
    private Long id;

    @NonNull
    private String text;

}
