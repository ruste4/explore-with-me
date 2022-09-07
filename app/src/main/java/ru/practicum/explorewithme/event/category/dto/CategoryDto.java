package ru.practicum.explorewithme.event.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryDto {

    private Long id;

    private String name;

}
