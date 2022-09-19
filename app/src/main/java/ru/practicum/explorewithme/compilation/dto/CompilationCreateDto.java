package ru.practicum.explorewithme.compilation.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CompilationCreateDto {

    private List<Long> events;

    private Boolean pinned;

    private String title;

}
