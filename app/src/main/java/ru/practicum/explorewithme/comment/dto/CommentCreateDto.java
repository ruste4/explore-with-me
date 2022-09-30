package ru.practicum.explorewithme.comment.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;

@Data
@Builder
public class CommentCreateDto {

    @NonNull
    private Long event;

    @NotBlank
    private String text;

}
