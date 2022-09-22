package ru.practicum.explorewithme.event.category.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CategoryCreateDto {
    @NotBlank
    private String name;

}
